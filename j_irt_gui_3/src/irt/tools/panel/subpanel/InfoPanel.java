package irt.tools.panel.subpanel;

import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.StringData;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.tools.panel.PicobucPanel;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.JButton;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Cursor;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

	private JLabel lblDeviceId;
	private JLabel lblSn;
	private JLabel lblVersion;
	private JLabel lblBuiltDate;
	private JLabel lblCount;
	private JLabel lblError;

	private LinkHeader linkHeader;
	private SecondsCount secondsCount;
	private TitledBorder titledBorder;
	private Properties properties = getProperties();
	private JLabel lblCountTxt;
	private JLabel lblBuiltDateTxt;
	private JLabel lblVertionTxt;
	private JLabel lblDeviceTxt;
	private JLabel lblSnTxt;
	private JLabel lblUnitName;
	private JLabel lblUnitPartNumber;
	private JLabel lblUnitPartNumberTxt;
	private JButton btnPanelSize;

	public InfoPanel(LinkHeader linkHeader) {
		setForeground(Color.WHITE);
		setBackground(new Color(0,0x33,0x33));


		addAncestorListener(new AncestorListener() {

			private PacketListener packetListener = new PacketListener() {

				@Override
				public void packetRecived(Packet packet) {
					if(packet!=null){
						LinkHeader lh = null;

						if(packet instanceof LinkedPacket)
							lh = ((LinkedPacket)packet).getLinkHeader();

						PacketHeader h = packet.getHeader();
						if((lh==null || lh.equals(InfoPanel.this.linkHeader)) && h!=null && h.getPacketId()==PacketWork.PACKET_DEVICE_INFO && h.getType()!=Packet.IRT_SLCP_PACKET_TYPE_REQUEST){
							int firmwareBuildCounter = new DeviceInfo(packet).getFirmwareBuildCounter();
							secondsCount.setFirmwareBuildCounter(firmwareBuildCounter);
						}
					}
				}
			};

			public void ancestorAdded(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(packetListener);
			}
			public void ancestorMoved(AncestorEvent arg0) {	}
			public void ancestorRemoved(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(packetListener);
				if(secondsCount!=null)
					secondsCount.setRunning(false);
				packetListener = null;
			}
		});

		this.linkHeader = linkHeader;

		Font font = Translation.getFont();
		titledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "info", "Info"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font,
				Color.WHITE
		);

		setBorder(titledBorder);
		setSize(286, 191);
		setLayout(null);

		String fontSize = properties.getProperty("infoPanel.labels.font.size");
		font = font.deriveFont(Float.parseFloat(fontSize));
		
				lblError = new JLabel();
				lblError.setFont(new Font("Tahoma", Font.BOLD, 17));
				lblError.setHorizontalAlignment(SwingConstants.CENTER);
				lblError.setBounds(10, 8, 266, 15);
				add(lblError);
		
		lblUnitName = new JLabel("");
		lblUnitName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnitName.setForeground(Color.YELLOW);
		lblUnitName.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblUnitName.setBounds(10, 31, 261, 14);
		add(lblUnitName);
				
						lblSnTxt = new JLabel(Translation.getValue(String.class, "sn", "SN")+":");
						lblSnTxt.setHorizontalAlignment(SwingConstants.RIGHT);
						lblSnTxt.setForeground(new Color(153, 255, 255));
						lblSnTxt.setFont(font);
						lblSnTxt.setBounds(10, 53, 48, 14);
						add(lblSnTxt);
		
				lblSn = new JLabel("SN");
				lblSn.setFont(new Font("Tahoma", Font.PLAIN, 14));
				lblSn.setBounds(61, 53, 103, 14);
				lblSn.setForeground(Color.YELLOW);
				add(lblSn);
		
		lblUnitPartNumberTxt = new JLabel(Translation.getValue(String.class, "part_number", "Part Number")+":");
		lblUnitPartNumberTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitPartNumberTxt.setForeground(new Color(153, 255, 255));
		lblUnitPartNumberTxt.setFont(font);
		lblUnitPartNumberTxt.setBounds(5, 75, 80, 14);
		add(lblUnitPartNumberTxt);
		
		lblUnitPartNumber = new JLabel("");
		lblUnitPartNumber.setForeground(Color.YELLOW);
		lblUnitPartNumber.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblUnitPartNumber.setBounds(95, 75, 176, 14);
		add(lblUnitPartNumber);
		
				lblCountTxt = new JLabel(Translation.getValue(String.class, "count", "Count")+":");
				lblCountTxt.setForeground(new Color(153, 255, 255));
				lblCountTxt.setHorizontalAlignment(SwingConstants.RIGHT);
				lblCountTxt.setFont(font);
				lblCountTxt.setBounds(15, 97, 58, 14);
				add(lblCountTxt);

		lblCount = new JLabel(":");
		lblCount.setForeground(Color.WHITE);
		lblCount.setBounds(84, 97, 141, 14);
		add(lblCount);
		
				lblBuiltDateTxt = new JLabel(Translation.getValue(String.class, "built_date", "Built Date")+":");
				lblBuiltDateTxt.setForeground(new Color(153, 255, 255));
				lblBuiltDateTxt.setFont(font);
				lblBuiltDateTxt.setHorizontalAlignment(SwingConstants.RIGHT);
				lblBuiltDateTxt.setBounds(5, 141, 58, 14);
				add(lblBuiltDateTxt);

		lblBuiltDate = new JLabel("Oct  2 2012, 10:45:39");
		lblBuiltDate.setForeground(Color.WHITE);
		lblBuiltDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblBuiltDate.setBounds(74, 141, 197, 14);
		add(lblBuiltDate);

		lblVertionTxt = new JLabel(Translation.getValue(String.class, "version", "Version")+":");
		lblVertionTxt.setForeground(new Color(153, 255, 255));
		lblVertionTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVertionTxt.setFont(font);
		lblVertionTxt.setBounds(5, 119, 58, 14);
		add(lblVertionTxt);

		lblVersion = new JLabel("0");
		lblVersion.setForeground(Color.WHITE);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblVersion.setBounds(74, 119, 155, 14);
		add(lblVersion);

		lblDeviceTxt = new JLabel(Translation.getValue(String.class, "device", "Device")+":");
		lblDeviceTxt.setForeground(new Color(153, 255, 255));
		lblDeviceTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDeviceTxt.setFont(font);
		lblDeviceTxt.setBounds(5, 163, 58, 14);
		add(lblDeviceTxt);

		lblDeviceId = new JLabel("0000.0.0");
		lblDeviceId.setForeground(Color.WHITE);
		lblDeviceId.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDeviceId.setBounds(74, 163, 85, 14);
		add(lblDeviceId);
		
		btnPanelSize = new JButton("");
		btnPanelSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnPanelSize.setBounds(266, 176, 20, 15);
		add(btnPanelSize);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				InfoPanel ip = InfoPanel.this;
				btnPanelSize.setLocation(ip.getWidth()-btnPanelSize.getWidth(), ip.getHeight()-btnPanelSize.getHeight());
			}
		});

	}

	public void setInfo(DeviceInfo deviceInfo) {
		if(deviceInfo!=null){
			lblDeviceId.setText(deviceInfo.getType()+"."+deviceInfo.getRevision()+"."+deviceInfo.getSubtype());
			lblVersion.setText(deviceInfo.getFirmwareVersion().toString());
			lblBuiltDate.setText(deviceInfo.getFirmwareBuildDate().toString());
			lblSn.setText(deviceInfo.getSerialNumber().toString());

			StringData unitName = deviceInfo.getUnitName();
			if(unitName!=null)
				lblUnitName.setText(unitName.toString());

			StringData unitPartNumber = deviceInfo.getUnitPartNumber();
			if(unitPartNumber!=null)
				lblUnitPartNumber.setText(unitPartNumber.toString());

			int firmwareBuildCounter = deviceInfo.getFirmwareBuildCounter();
			lblCount.setText(calculateTime(firmwareBuildCounter));
			secondsCount = new SecondsCount(firmwareBuildCounter);
		}
	}

	public void setError(String errorStr, Color errorColor) {
		lblError.setText(errorStr);
		lblError.setForeground(errorColor);
	}

	public static String calculateTime(long seconds) {

		int day = (int) TimeUnit.SECONDS.toDays(seconds);
	    long hours = TimeUnit.SECONDS.toHours(seconds) 	  - TimeUnit.DAYS.toHours(day);
	    long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds));
	    long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds));

	    return (day>0 ? day+" day"+(day==1 ? ", " : "s, ") : "")+hours+ ":"+minute+":"+second;
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		try {
			properties.load(PicobucPanel.class.getResourceAsStream("PicoBucPanel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

//********************************************************************************************************************
	private class SecondsCount implements Runnable{

		private volatile int firmwareBuildCounter;
		private boolean running;

		public SecondsCount(int firmwareBuildCounter){
			this.firmwareBuildCounter = firmwareBuildCounter;
			Thread t = new Thread(this);
			t.setPriority(t.getPriority()-1);
			t.start();
		}

		@Override
		public void run() {
			running = true;
			while(running){
				try {
					synchronized (this) {
						wait(1000);
					}
					
				} catch (InterruptedException e) {}

				lblCount.setText(calculateTime(firmwareBuildCounter++));
			}
			lblCount = null;
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		public void setFirmwareBuildCounter(int firmwareBuildCounter) {
			this.firmwareBuildCounter = firmwareBuildCounter;
		}
	}

	public void refresh() {
		getFont();

		titledBorder.setTitle(Translation.getValue(String.class, "info", "Info"));
		Font font = Translation.getFont();
		titledBorder.setTitleFont(font);

		String fontSize = properties.getProperty("infoPanel.labels.font.size");
		font = font.deriveFont(Float.parseFloat(fontSize));
		
		lblCountTxt.setFont(font);
		lblCountTxt.setText(Translation.getValue(String.class, "count", "Count")+":");
		lblBuiltDateTxt.setFont(font);
		lblBuiltDateTxt.setText(Translation.getValue(String.class, "built_date", "Built Date")+":");
		lblVertionTxt.setFont(font);
		lblVertionTxt.setText(Translation.getValue(String.class, "version", "Version")+":");
		lblDeviceTxt.setFont(font);
		lblDeviceTxt.setText(Translation.getValue(String.class, "device", "Device")+":");
		lblSnTxt.setFont(font);
		lblSnTxt.setText(Translation.getValue(String.class, "sn", "SN")+":");
		lblUnitPartNumberTxt.setFont(font);
		lblUnitPartNumberTxt.setText(Translation.getValue(String.class, "part_number", "Part Number")+":");
	}
}
