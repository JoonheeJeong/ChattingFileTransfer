package chat_file_transfer;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	private int upperLayerCount = 0;
	private String layerName = null;
	private BaseLayer p_UnderLayer = null;
	private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private Frame frame = new Frame();
	private boolean isAckReceived = true;

	private class Frame {
		byte[] dstMacAddress;
		byte[] srcMacAddress;
		byte[] type;
		//byte[] enet_data;
		
		//Chat type: 0x2080, File type: 0x2090, ACK type: 0x1004
		public Frame() {
			this.dstMacAddress = new byte[6];
			this.srcMacAddress = new byte[6];
			this.type = new byte[2];
			this.type[0] = 0x20;
			this.type[1] = (byte) 0x80;
			//this.enet_data = null;
		}
	}
	
	private enum TransmissionType{
		CHAT,
		FILE,
		ACK,
		NONE;
	}
	
	public EthernetLayer(String layerName) {
		this.layerName = layerName;
	}
	
	public void setSrcAddr(byte[] srcAddr) {
		System.arraycopy(srcAddr, 0, this.frame.srcMacAddress, 0, 6);
	}
	
	public void setDstAddr(byte[] dstAddr) {
		System.arraycopy(dstAddr, 0, this.frame.dstMacAddress, 0, 6);
	}
	
	public void send(byte[] input, int length) {
		System.out.println("send_ethernet_start");
		TransmissionType transmissionType = getTransmissionType(input);
		byte[] data = addHeader(transmissionType, input, length);
		getUnderLayer().send(data, length+14);
		
		this.isAckReceived = false; // 송신 후 ACK 수신 대기
		synchronized(this) {
			while (!isAckReceived) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
		System.out.println("send_ethernet_end");
	}

	public synchronized void receive(byte[] input) {
		System.out.println("receive_ethernet_start");
		if ( !receivable(input) ) {
			System.out.println("receive_ethernet_end");
			return;
		}
		TransmissionType transmissionType = getTransmissionType(input);
		switch (transmissionType) {
		case CHAT: case FILE:
			byte[] data = removeHeader(input, input.length);
			getUpperLayer(0).receive(data);
			sendAck(generateAck());
			break;
		case ACK: // Wake up waiting send thread
			this.isAckReceived = true;
			notify();
			break;
		default: // DATA 타입과 ACK 타입이 아닌 경우: NONE (noise)
			break;
		}
		System.out.println("receive_ethernet_end");
	}
	
	private byte[] addHeader(TransmissionType transmissionType, byte[] input, int length) {
		byte[] buf = new byte[length+14];
		System.arraycopy(this.frame.dstMacAddress, 0, buf, 0, 6);
		System.arraycopy(this.frame.srcMacAddress, 0, buf, 6, 6);
		switch (transmissionType) {
		case CHAT:
			System.arraycopy(this.frame.type, 0, buf, 12, 2);
			break;
		case FILE:
			//
			break;
		case ACK:
			buf[12] = 0x10;
			buf[13] = 0x04;
			break;
		default: // error			
			break;
		}
		System.arraycopy(input, 0, buf, 14, length);
		return buf;
	}
	
	private byte[] removeHeader(byte[] input, int length) {
		int realLength = length;
		if (realLength == 60) { // 수신 패킷 최소 크기(60)에 대한 보정
			int lastIndex = 59;
			for (; lastIndex > 17; lastIndex--) {
				if (input[lastIndex] != (byte) 0x00)
					break;
			}
			realLength = lastIndex + 1;
		}
		final int reducedLength = realLength - 14;
		byte[] data = new byte[reducedLength];
		System.arraycopy(input, 14, data, 0, reducedLength);
		return data;
	}
	
	private boolean receivable(byte[] input) {
		if (isMyPacket(input))
			return false;
		if (isBroadCast(input))
			return true;
		if (sentToMe(input))
			return true;
		return false;
	}
	
	private TransmissionType getTransmissionType(byte[] input) {
		if (input[12] == 0x20 && input[13] == 0x80)
			return TransmissionType.CHAT;
		if (input[12] == 0x20 && input[13] == 0x90)
			return TransmissionType.FILE;
		if (input[12] == 0x10 && input[13] == 0x04)
			return TransmissionType.ACK;
		return TransmissionType.NONE;
	}
	
	private byte[] generateAck() {
		return addHeader(TransmissionType.ACK, new byte[0], 0);
	}
	
	private void sendAck(byte[] ACK) {
		getUnderLayer().send(ACK, 14);
	}
	
	private boolean isBroadCast(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i] != (byte) 0xff)
				return false;
		}
		return true;
	}
	
	private boolean sentToMe(byte[] input) {
		byte[] temp_src = this.frame.srcMacAddress;
		for (int i = 0; i < 6; i++) {
			if (temp_src[i] != input[i])
				return false;
		}
		return true;
	}
	
	private boolean isMyPacket(byte[] input) {
		byte[] temp_src = this.frame.srcMacAddress;
		for (int i = 0; i < 6; i++) {
			if (temp_src[i] != input[i+6])
				return false;
		}
		return true;
	}
	
	@Override
	public String getLayerName() {
		return layerName;
	}

	@Override
	public BaseLayer getUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer getUpperLayer(int index) {
		if (index < 0 || index > upperLayerCount || upperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(index);
	}

	@Override
	public void setUnderLayer(BaseLayer underLayer) {
		if (underLayer == null)
			return;
		this.p_UnderLayer = underLayer;
	}

	@Override
	public void setUpperLayer(BaseLayer upperLayer) {
		if (upperLayer == null)
			return;
		this.p_aUpperLayer.add(upperLayerCount++, upperLayer);
	}

	@Override
	public void setUpperUnderLayer(BaseLayer layer) {
		this.setUpperLayer(layer);
		layer.setUnderLayer(this);
	}
}
