package chat_file_transfer;

import java.util.ArrayList;

public class FillAppLayer implements BaseLayer {
	public int upperLayerCount = 0;
	public String layerName = null;
	public BaseLayer underLayer = null;
	public ArrayList<BaseLayer> upperLayerList = new ArrayList<BaseLayer>();
	
	private FileApp fileApp = new FileApp();
	private static final int FRAGMENTATION_SIZE = 1448;
	private int bufferCount;

	private class FileApp {
		private byte[] totlen;
		private byte[] type;
		private byte msgType;
		private byte unused;
		private byte[] seqNum;
		private byte[] buffer;

		public FileApp() {
			bufferCount = 0;
			totlen = new byte[2];
			type = new byte[2];
			msgType = 0x00;
			unused = 0x00;
			seqNum = new byte[4];
			buffer = null;
		}
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
	public void setUpperUnderLayer(BaseLayer layer) {
		this.setUpperLayer(layer);
		layer.setUnderLayer(this);
	}
}
