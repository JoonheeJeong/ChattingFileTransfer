package chat_file_transfer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer {
	public int upperLayerCount = 0;
	public String layerName = null;
	public BaseLayer underLayer = null;
	public ArrayList<BaseLayer> upperLayerList = new ArrayList<BaseLayer>();

	int adapterCount;
	public Pcap adapterObject;
	public PcapIf device;
	public List<PcapIf> adapterList;
	StringBuilder errbuf = new StringBuilder();

	public NILayer(String layerName) {
		this.layerName = layerName;
		adapterList = new ArrayList<PcapIf>();
		adapterCount = 0;
		SetAdapterList();
	}
	
	public void PacketStartDriver() {
		int snaplen = 64 * 1024;
		int flags = Pcap.MODE_PROMISCUOUS;
		int timeout = 10 * 1000;
		this.adapterObject = Pcap.openLive(
			adapterList.get(adapterCount).getName(),
			snaplen, 
			flags, 
			timeout, 
			this.errbuf
		);
	}
	
	public void SetAdapterNumber(int num) {
		this.adapterCount = num;
		PacketStartDriver();
		receive();
	}
	
	public void SetAdapterList() {
		int r = Pcap.findAllDevs(adapterList, errbuf);
		if (r == Pcap.NOT_OK || adapterList.isEmpty()) {
			System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
			return;
		}
	}
	
	public void send(byte[] input, int length) {
		System.out.println("send_nil");
		ByteBuffer buf = ByteBuffer.wrap(input);
		if (adapterObject.sendPacket(buf) != Pcap.OK) {
			System.err.println(adapterObject.getErr());
		}
	}

	public void receive() {
		ReceiveThread receiveThread = new ReceiveThread(adapterObject, this.getUpperLayer(0));
		Thread thread = new Thread(receiveThread, "ReceiveThread");
		thread.start();
	}

	@Override
	public void setUnderLayer(BaseLayer underLayer) {
		if (underLayer == null)
			return;
		this.underLayer = underLayer;
	}

	@Override
	public void setUpperLayer(BaseLayer upperLayer) {
		if (upperLayer == null)
			return;
		this.upperLayerList.add(upperLayerCount++, upperLayer);
	}

	@Override
	public String getLayerName() {
		return layerName;
	}

	@Override
	public BaseLayer getUnderLayer() {
		if (underLayer == null)
			return null;
		return underLayer;
	}

	@Override
	public BaseLayer getUpperLayer(int index) {
		if (index < 0 || index > upperLayerCount || upperLayerCount < 0)
			return null;
		return upperLayerList.get(index);
	}

	@Override
	public void setUpperUnderLayer(BaseLayer layer) {
		this.setUpperLayer(layer);
		layer.setUnderLayer(this);
	}

}

class ReceiveThread implements Runnable {
	byte[] data;
	Pcap adapterObject;
	BaseLayer upperLayer;

	public ReceiveThread(Pcap adapterObject, BaseLayer upperLayer) {
		this.adapterObject = adapterObject;
		this.upperLayer = upperLayer;
	}

	@Override
	public void run() {
		while (true) {
			PcapPacketHandler<String> jPacketHandler = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0, packet.size());
					System.out.println("Receive_nil");
					upperLayer.receive(data);
				}
			};
			
			adapterObject.loop(1000, jPacketHandler, "");
		}
	}

}
