package irt.tools.panel.subpanel;

import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.StringData;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.tools.Transformer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel implements Refresh {

	private final static Logger logger = (Logger) LogManager.getLogger();

	private static final int WINDOW_MIN_HEIGHT = 105;
	private static final int WINDOW_MAX_HEIGHT = 135;
	private JLabel lblDeviceId;
	private JLabel lblSn;
	private JLabel lblVersion;
	private JLabel lblBuiltDate;
	private JLabel lblCount;
	private JLabel lblError;

	private LinkHeader linkHeader;
	private SecondsCount secondsCount;
	private TitledBorder titledBorder;
	private JLabel lblCountTxt;
	private JLabel lblBuiltDateTxt;
	private JLabel lblVersionTxt;
	private JLabel lblDeviceTxt;
	private JLabel lblSnTxt;
	private JLabel lblUnitName;
	private JLabel lblUnitPartNumber;
	private JLabel lblUnitPartNumberTxt;
	private JButton btnPanelSize;

	public InfoPanel(LinkHeader linkHeader) {
		setForeground(Color.WHITE);
		setBackground(new Color(0,0x33,0x33));
		setSize(286, WINDOW_MIN_HEIGHT);


		addAncestorListener(new AncestorListener() {

			private PacketListener packetListener = new PacketListener() {

				@Override
				public void packetRecived(Packet packet) {
					if(packet!=null){
						LinkHeader lh = null;

						if(packet instanceof LinkedPacket)
							lh = ((LinkedPacket)packet).getLinkHeader();

						PacketHeader h = packet.getHeader();
						if((lh==null || lh.equals(InfoPanel.this.linkHeader)) && h!=null && h.getPacketId()==PacketWork.PACKET_DEVICE_INFO && h.getPacketType()!=Packet.PACKET_TYPE_REQUEST){
							int firmwareBuildCounter = new DeviceInfo(packet).getUptimeCounter();
							secondsCount.setFirmwareBuildCounter(firmwareBuildCounter);
						}
					}
				}
			};

			public void ancestorAdded(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(packetListener);
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(packetListener);
				if(secondsCount!=null)
					secondsCount.setRunning(false);
				packetListener = null;
			}
			public void ancestorMoved(AncestorEvent arg0) {	}
		});

		this.linkHeader = linkHeader;

		Font font = Translation.getFont()
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.size", 18))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD));

		titledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "info", "Info"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font,
				Color.WHITE
		);
		setBorder(titledBorder);

		font = font.deriveFont(Translation.getValue(Float.class, "infoPanel.labels.font.size", 16f));
		
				lblError = new JLabel();
				lblError.setBounds(4, 0, 277, 21);
				lblError.setFont(new Font("Tahoma", Font.BOLD, 17));
				lblError.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblUnitName = new JLabel("");
		lblUnitName.setBounds(15, 19, 261, 14);
		lblUnitName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUnitName.setForeground(Color.YELLOW);
		lblUnitName.setFont(new Font("Tahoma", Font.BOLD, 14));
				
						lblSnTxt = new JLabel(Translation.getValue(String.class, "sn", "SN")+":");
						lblSnTxt.setBounds(4, 36, 76, 14);
						lblSnTxt.setHorizontalAlignment(SwingConstants.RIGHT);
						lblSnTxt.setForeground(new Color(153, 255, 255));
						lblSnTxt.setFont(font);
		
				lblSn = new JLabel("SN");
				lblSn.setHorizontalAlignment(SwingConstants.LEFT);
				lblSn.setBounds(84, 35, 198, 14);
				lblSn.setFont(new Font("Tahoma", Font.PLAIN, 14));
				lblSn.setForeground(Color.YELLOW);
		
		lblUnitPartNumberTxt = new JLabel(Translation.getValue(String.class, "part_number", "Part Number")+":");
		lblUnitPartNumberTxt.setBounds(4, 52, 76, 14);
		lblUnitPartNumberTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitPartNumberTxt.setForeground(new Color(153, 255, 255));
		lblUnitPartNumberTxt.setFont(font);
		
		lblUnitPartNumber = new JLabel("");
		lblUnitPartNumber.setHorizontalAlignment(SwingConstants.LEFT);
		lblUnitPartNumber.setBounds(84, 51, 198, 14);
		lblUnitPartNumber.setForeground(Color.YELLOW);
		lblUnitPartNumber.setFont(new Font("Tahoma", Font.BOLD, 14));
		
				lblCountTxt = new JLabel(Translation.getValue(String.class, "count", "Count")+":");
				lblCountTxt.setBounds(4, 68, 76, 14);
				lblCountTxt.setForeground(new Color(153, 255, 255));
				lblCountTxt.setHorizontalAlignment(SwingConstants.RIGHT);
				lblCountTxt.setFont(font);

		lblCount = new JLabel(":");
		lblCount.setHorizontalAlignment(SwingConstants.LEFT);
		lblCount.setBounds(84, 67, 198, 14);
		lblCount.setForeground(Color.WHITE);
		
				lblBuiltDateTxt = new JLabel(Translation.getValue(String.class, "built_date", "Built Date")+":");
				lblBuiltDateTxt.setBounds(4, 84, 76, 14);
				lblBuiltDateTxt.setForeground(new Color(153, 255, 255));
				lblBuiltDateTxt.setFont(font);
				lblBuiltDateTxt.setHorizontalAlignment(SwingConstants.RIGHT);

		lblBuiltDate = new JLabel("Oct  2 2012, 10:45:39");
		lblBuiltDate.setHorizontalAlignment(SwingConstants.LEFT);
		lblBuiltDate.setBounds(84, 83, 198, 14);
		lblBuiltDate.setForeground(Color.WHITE);
		lblBuiltDate.setFont(new Font("Tahoma", Font.PLAIN, 14));

		lblVersionTxt = new JLabel(Translation.getValue(String.class, "version", "Version")+":");
		lblVersionTxt.setVisible(false);
		lblVersionTxt.setBounds(4, 100, 76, 14);
		lblVersionTxt.setForeground(new Color(153, 255, 255));
		lblVersionTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersionTxt.setFont(font);

		lblVersion = new JLabel("0");
		lblVersion.setHorizontalAlignment(SwingConstants.LEFT);
		lblVersion.setVisible(false);
		lblVersion.setBounds(84, 99, 198, 14);
		lblVersion.setForeground(Color.WHITE);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 14));

		lblDeviceTxt = new JLabel(Translation.getValue(String.class, "device", "Device")+":");
		lblDeviceTxt.setBounds(4, 116, 76, 14);
		lblDeviceTxt.setForeground(new Color(153, 255, 255));
		lblDeviceTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDeviceTxt.setFont(font);

		lblDeviceId = new JLabel("0000.0.0");
		lblDeviceId.setHorizontalAlignment(SwingConstants.LEFT);
		lblDeviceId.setBounds(84, 115, 198, 14);
		lblDeviceId.setForeground(Color.WHITE);
		lblDeviceId.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		btnPanelSize = new JButton("");
		btnPanelSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				Transformer transformer = new Transformer();
				transformer.setComponent(InfoPanel.this);
				transformer.addProcessingComponent(Transformer.ACTION_SHOW, lblVersion);
				transformer.addProcessingComponent(Transformer.ACTION_SHOW, lblVersionTxt);

				if(getHeight()>WINDOW_MIN_HEIGHT)
					transformer.setHeight(WINDOW_MIN_HEIGHT);
				else
					transformer.setHeight(WINDOW_MAX_HEIGHT);

				Thread t = new Thread(transformer, "InfoPanel.Transformer-"+new RundomNumber());
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
		});
		btnPanelSize.setBounds(271, 91, 10, 10);
		btnPanelSize.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setLayout(null);
		add(lblError);
		add(lblUnitName);
		add(lblSnTxt);
		add(lblSn);
		add(lblUnitPartNumberTxt);
		add(lblUnitPartNumber);
		add(lblCountTxt);
		add(lblCount);
		add(lblBuiltDateTxt);
		add(lblBuiltDate);
		add(lblVersionTxt);
		add(lblVersion);
		add(lblDeviceTxt);
		add(lblDeviceId);
		add(btnPanelSize);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				InfoPanel ip = InfoPanel.this;
				int width = ip.getWidth()-btnPanelSize.getWidth()-1;
				int height = ip.getHeight()-btnPanelSize.getHeight()-1;
				btnPanelSize.setLocation(width, height);
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

			int firmwareBuildCounter = deviceInfo.getUptimeCounter();
			lblCount.setText(calculateTime(firmwareBuildCounter));
			secondsCount = new SecondsCount(firmwareBuildCounter);
		}
	}

	public void setError(String errorStr, Color errorColor) {
		lblError.setText(errorStr);
		lblError.setForeground(errorColor);
		lblError.setBackground(new Color(0, 51, 51));
		lblError.setOpaque(true);
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
			Thread t = new Thread(this, "InfoPanel.SecondsCount-"+new RundomNumber());
			int priority = t.getPriority();
			if(priority>Thread.MIN_PRIORITY)
				t.setPriority(priority-1);
			t.setDaemon(true);
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
					
				} catch (InterruptedException e) {
					logger.catching(e);
				}

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
		Font font = Translation.getFont()
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.size", 18))
				.deriveFont(Translation.getValue(Integer.class, "titledBorder.font.type", Font.BOLD));

		titledBorder.setTitle(Translation.getValue(String.class, "info", "Info"));
		titledBorder.setTitleFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "infoPanel.labels.font.size", 12f));
		
		lblCountTxt.setFont(font);
		lblCountTxt.setText(Translation.getValue(String.class, "count", "Count")+":");
		lblBuiltDateTxt.setFont(font);
		lblBuiltDateTxt.setText(Translation.getValue(String.class, "built_date", "Built Date")+":");
		lblVersionTxt.setFont(font);
		lblVersionTxt.setText(Translation.getValue(String.class, "version", "Version")+":");
		lblDeviceTxt.setFont(font);
		lblDeviceTxt.setText(Translation.getValue(String.class, "device", "Device")+":");
		lblSnTxt.setFont(font);
		lblSnTxt.setText(Translation.getValue(String.class, "sn", "SN")+":");
		lblUnitPartNumberTxt.setFont(font);
		lblUnitPartNumberTxt.setText(Translation.getValue(String.class, "part_number", "Part Number")+":");
	}
}
