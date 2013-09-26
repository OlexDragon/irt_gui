package irt.tools.panel.subpanel;

import irt.controller.GuiControllerAbstract;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import resources.Translation;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel {

	private static final int DEFAULT_FONT_SIZE = 14;
	private static final Font FONT = new Font("Tahoma", Font.PLAIN, DEFAULT_FONT_SIZE);
	private JLabel lblDeviceId;
	private JLabel lblSN;
	private JLabel lblVersion;
	private JLabel lblBuiltDate;
	private JLabel lblCount;
	private JLabel lblError;

	private LinkHeader linkHeader;
	private SecondsCount secondsCount;
	private JLabel lblDevice;
	private JLabel labelBuiltDate;
	private JLabel labelCount;
	private JLabel labelVersion;
	private TitledBorder titledBorder;

	public InfoPanel(LinkHeader linkHeader) {
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

		setOpaque(false);
		Font font = Translation.replaceFont("resource.font", "info_panel.font.size", FONT, DEFAULT_FONT_SIZE);
		titledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), Translation.getValue(String.class, "info", "Info"), TitledBorder.LEADING, TitledBorder.TOP,  font, Color.WHITE);
		setBorder(titledBorder);
		setSize(286, 104);
		setLayout(null);

		labelCount = new JLabel(Translation.getValue(String.class, "count", "Count")+":");
		labelCount.setForeground(new Color(153, 255, 255));
		labelCount.setHorizontalAlignment(SwingConstants.RIGHT);
		labelCount.setBounds(10, 75, 58, 14);
		add(labelCount);

		lblCount = new JLabel(":");
		lblCount.setForeground(Color.WHITE);
		lblCount.setBounds(79, 75, 141, 14);
		add(lblCount);

		lblBuiltDate = new JLabel("Oct  2 2012, 10:45:39");
		lblBuiltDate.setForeground(Color.WHITE);
		lblBuiltDate.setFont(FONT);
		lblBuiltDate.setBounds(79, 57, 197, 14);
		add(lblBuiltDate);

		labelBuiltDate = new JLabel(Translation.getValue(String.class, "built_date", "Built Date")+":");
		labelBuiltDate.setForeground(new Color(153, 255, 255));
		labelBuiltDate.setHorizontalAlignment(SwingConstants.RIGHT);
		labelBuiltDate.setBounds(10, 57, 58, 14);
		add(labelBuiltDate);

		labelVersion = new JLabel(Translation.getValue(String.class, "version", "Version")+":");
		labelVersion.setForeground(new Color(153, 255, 255));
		labelVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		labelVersion.setBounds(10, 39, 58, 14);
		add(labelVersion);

		lblVersion = new JLabel("0");
		lblVersion.setForeground(Color.WHITE);
		lblVersion.setFont(FONT);
		lblVersion.setBounds(79, 39, 155, 14);
		add(lblVersion);

		lblDevice = new JLabel(Translation.getValue(String.class, "device", "Device")+":");
		lblDevice.setForeground(new Color(153, 255, 255));
		lblDevice.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDevice.setBounds(10, 21, 58, 14);
		add(lblDevice);

		lblDeviceId = new JLabel("0000.0.0");
		lblDeviceId.setForeground(Color.WHITE);
		lblDeviceId.setFont(FONT);
		lblDeviceId.setBounds(79, 21, 85, 14);
		add(lblDeviceId);

		JLabel lblSn = new JLabel("SN:");
		lblSn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSn.setForeground(new Color(153, 255, 255));
		lblSn.setBounds(143, 21, 24, 14);
		add(lblSn);

		lblSN = new JLabel("SN");
		lblSN.setFont(FONT);
		lblSN.setBounds(173, 21, 103, 14);
		lblSN.setForeground(Color.YELLOW);
		add(lblSN);

		lblError = new JLabel();
		lblError.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblError.setHorizontalAlignment(SwingConstants.CENTER);
		lblError.setBounds(10, 85, 266, 15);
		add(lblError);
	}

	public void setInfo(DeviceInfo deviceInfo) {
		if(deviceInfo!=null){
			lblDeviceId.setText(deviceInfo.getType()+"."+deviceInfo.getRevision()+"."+deviceInfo.getSubtype());
			lblVersion.setText(deviceInfo.getFirmwareVersion().toString());
			lblBuiltDate.setText(deviceInfo.getFirmwareBuildDate().toString());
			lblSN.setText(deviceInfo.getSerialNumber().toString());

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
		lblDevice.setText(Translation.getValue(String.class, "device", "Device")+":");
		labelBuiltDate.setText(Translation.getValue(String.class, "built_date", "Built Date")+":");
		labelCount.setText(Translation.getValue(String.class, "count", "Count")+":");
		labelVersion.setText(Translation.getValue(String.class, "version", "Version")+":");

		Font font = Translation.replaceFont("resource.font", "info_panel.font.size", FONT, DEFAULT_FONT_SIZE);
		titledBorder.setTitle(Translation.getValue(String.class, "info", "Info"));
		titledBorder.setTitleFont(font);
	}
}
