package chat_file_transfer;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class ChatFileDlg extends JFrame implements BaseLayer {

	public int upperLayerCount = 0;
	public String layerName = null;
	public BaseLayer underLayer = null;
	public ArrayList<BaseLayer> upperLayerList = new ArrayList<BaseLayer>();

	private static LayerManager layerManager = new LayerManager();

	private JTextField chattingWrite;

	private Container contentPane;

	private JTextArea chattingArea;
	private JTextArea srcMacArea;
	private JTextArea dstMacArea;

	private JLabel networkInterfaceLabel;
	private JLabel srcMacLabel;
	private JLabel dstMacLabel;

	private JButton settingButton;
	private JButton sendChatButton;

	private JComboBox<String> networkInterfaceComboBox;

	static {
		String a = System.getProperty("sun.arch.data.model");
		System.out.println(a);
		try {
			System.load(new File("jnetpcap.dll").getAbsolutePath());
			System.out.println(new File("jnetpcap.dll").getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		layerManager.AddLayer(new NILayer("NI"));
		layerManager.AddLayer(new EthernetLayer("Ethernet"));
		layerManager.AddLayer(new ChatAppLayer("Chat"));
		layerManager.AddLayer(new ChatFileDlg("GUI"));

		layerManager.ConnectLayers(" NI ( *Ethernet ( *Chat ( *GUI ) ) )");
	}

	public ChatFileDlg(String pName) {
		layerName = pName;

		setTitle("Stop & Wait Protocol");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		chattingArea = new JTextArea();
		chattingArea.setEditable(false);
		chattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(chattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		chattingWrite = new JTextField();
		chattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(chattingWrite);
		chattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		networkInterfaceComboBox = new JComboBox<String>();
		NILayer nil = (NILayer) layerManager.GetLayer("NI");
		for (int i = 0; i < nil.adapterList.size(); i++) {
			networkInterfaceComboBox.addItem(nil.adapterList.get(i).getDescription());
		}
		networkInterfaceComboBox.setSelectedIndex(0);
		networkInterfaceComboBox.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		networkInterfaceComboBox.setBounds(10, 46, 170, 30);
		networkInterfaceComboBox.addActionListener(new setAddressListener());
		settingPanel.add(networkInterfaceComboBox);
		networkInterfaceComboBox.setLayout(null);

		networkInterfaceLabel = new JLabel("Network Interface Option");
		networkInterfaceLabel.setBounds(10, 25, 170, 20);
		settingPanel.add(networkInterfaceLabel);

		JPanel srcAddrPanel = new JPanel();
		srcAddrPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		srcAddrPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(srcAddrPanel);
		srcAddrPanel.setLayout(null);

		srcMacLabel = new JLabel("Source Mac Address");
		srcMacLabel.setBounds(10, 75, 170, 20);
		settingPanel.add(srcMacLabel);

		srcMacArea = new JTextArea();
		byte[] mac = getMacAddress();
		srcMacArea.setText(getStringFromByteMacAddress(mac));
		srcMacArea.setBounds(2, 2, 170, 20);
		srcAddrPanel.add(srcMacArea);

		JPanel dstAddrPanel = new JPanel();
		dstAddrPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		dstAddrPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(dstAddrPanel);
		dstAddrPanel.setLayout(null);

		dstMacLabel = new JLabel("Destination Mac Address");
		dstMacLabel.setBounds(10, 187, 170, 20);
		settingPanel.add(dstMacLabel);

		dstMacArea = new JTextArea();
		dstMacArea.setBounds(2, 2, 170, 20);
		dstAddrPanel.add(dstMacArea);

		settingButton = new JButton("Setting");
		settingButton.setBounds(80, 270, 100, 20);
		settingButton.addActionListener(new setAddressListener());
		settingPanel.add(settingButton);

		sendChatButton = new JButton("Send");
		sendChatButton.setBounds(270, 230, 80, 20);
		sendChatButton.addActionListener(new sendMessageListener());
		chattingPanel.add(sendChatButton);// chatting send button

		setVisible(true);
	}

	private class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			///////////////////////////////////////////
			Object o = e.getSource();
			byte[] byteSrcMac = null;
			if (o == networkInterfaceComboBox) {
				byteSrcMac = getMacAddress();
				srcMacArea.setText(getStringFromByteMacAddress(byteSrcMac));
				///////////////////////////////////////////
			} else if (o == settingButton) {
				if (settingButton.getText() == "Reset") {
					srcMacArea.setText("");
					dstMacArea.setText("");
					settingButton.setText("Setting");
					srcMacArea.setEditable(true);
					dstMacArea.setEditable(true);
					///////////// 진행중인 리씨브 쓰레드 중단시키기/////

					////////////////////////////////////
				} else {
					///////////// 소스 주소/////////////////
					EthernetLayer ethernetLayer = (EthernetLayer) layerManager.GetLayer("Ethernet");
					byteSrcMac = getMacAddress();
					ethernetLayer.setSrcAddr(byteSrcMac);
					///////////// 목적 주소/////////////////
					String strDstMac = dstMacArea.getText();
					byte[] byteDstMac = getByteFromStringMacAddress(strDstMac);
					ethernetLayer.setDstAddr(byteDstMac);
					////////////// 어뎁터 설정///////////////
					NILayer networkInterfaceLayer = (NILayer) layerManager.GetLayer("NI");
					int index = networkInterfaceComboBox.getSelectedIndex();
					networkInterfaceLayer.SetAdapterNumber(index);
					
					settingButton.setText("Reset");
					srcMacArea.setEditable(false);
					dstMacArea.setEditable(false);
				}
			}
		}
	}
	
	private class sendMessageListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!settingButton.getText().equals("Reset")) {
				JOptionPane.showMessageDialog(null, "You must set mac address first.\n");
				return;
			}
			System.out.println("send_dlg");
			String sendingText = chattingWrite.getText();
			chattingWrite.setText("");
			chattingArea.append("[SEND]:" + sendingText + "\n");
			byte[] data = sendingText.getBytes();
			underLayer.send(data, data.length);
		}
	}
	
	public void receive(byte[] input) {
		System.out.println("receive_dlg");
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < input.length; i++) {
			if (input[i] < 0) { // 한글이면 3바이트
				byte[] temp = new byte[3];
				temp[0] = input[i];
				temp[1] = input[++i];
				temp[2] = input[++i];
				buf.append(new String(temp));
			} else { // 기타 영문, 문자, 기호이면 1바이트
				buf.append((char) input[i]);
			}
		}
		chattingArea.append("[RECV]:" + buf + "\n");
	}
	
	private byte[] getMacAddress() {
		byte[] mac = null;
		try {
			NILayer nil = (NILayer) layerManager.GetLayer("NI");
			int index = networkInterfaceComboBox.getSelectedIndex();
			mac = nil.adapterList.get(index).getHardwareAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac;
	}

	private String getStringFromByteMacAddress(byte[] mac) {
		mac = getMacAddress();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}

	private byte TokenToByte(String token) {
		byte result = 0x00;
		for (int i = 0; i < 2; i++) {
			char c = token.charAt(i);
			if (c >= '0' && c <= '9')
				result += c - '0';
			else if (c >= 'A' && c <= 'F')
				result += c - 'A' + 0xA;
			result = (byte) ((i == 0) ? (result << 4) : result);
		}

		return result;
	}

	private byte[] getByteFromStringMacAddress(String mac) {
		byte[] result = new byte[6];
		try {
			StringTokenizer st = new StringTokenizer(mac, "-");
			// AA-BB-CC-DD-EE-FF
			int i = 0;
			String token;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				result[i++] = TokenToByte(token);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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
	public void setUpperUnderLayer(BaseLayer pUULayer) {
		this.setUpperLayer(pUULayer);
		pUULayer.setUnderLayer(this);
	}
}
