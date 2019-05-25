package chat_file_transfer;

import java.util.ArrayList;

interface BaseLayer {
	public final int upperLayerCount = 0;
	public final String pLayerName = null;
	public final BaseLayer underLayer = null;
	public final ArrayList<BaseLayer> upperLayerList = new ArrayList<BaseLayer>();

	public String getLayerName();

	public BaseLayer getUnderLayer();

	public BaseLayer getUpperLayer(int index);

	public void setUnderLayer(BaseLayer underLayer);

	public void setUpperLayer(BaseLayer upperLayer);

	public void setUpperUnderLayer(BaseLayer layer);

	public default void send(byte[] input, int length) {
	}

	public default void send(String filename) {
	}

	public default void receive(byte[] input) {
	}

	public default void receive() {
	}
}
