package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.GuiControllerAbstract;
import irt.controller.SoftReleaseChecker;
import irt.controller.file.ProfileScannerFT;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.interfaces.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.Transformer;
import irt.tools.fx.update.UpdateMessageFx;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@SuppressWarnings("serial")
public class InfoPanel extends JPanel implements Refresh, PacketListener {

	private final static Logger logger = (Logger) LogManager.getLogger();

	private static final int WINDOW_MIN_HEIGHT = 105;
	private static final int WINDOW_MAX_HEIGHT = 135;
	private JLabel lblDeviceId;
	private JLabel lblSn;
	private JLabel lblVersion;
	private JLabel lblBuiltDate;
	private JLabel lblCount;
	private JLabel lblError;

	private DeviceInfo deviceInfo;
	private SecondsCount secondsCount = new SecondsCount();
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

	private final Timer timer = new Timer(true);

	private ProfileScannerFT profileScannerFT;

	public InfoPanel(DeviceInfo deviceInfo) {

		setForeground(Color.WHITE);
		setBackground(new Color(0,0x33,0x33));
		setSize(286, WINDOW_MIN_HEIGHT);

		if(	// If main class is irtGui add pop up menu with 'Open' and 'Open file location'
				((LoggerContext) LogManager.getContext(false)).getLoggers().stream()
				.map(Logger::getName)
				.filter(n->n.equals("irt.irt_gui.UserIrtGui")).findAny()
				.map(n->false)
				.orElse(true)){

			JPopupMenu popup = new JPopupMenu();
			setComponentPopupMenu(popup);
			JMenuItem openMenuItem = new JMenuItem("Open");
			openMenuItem.setEnabled(false);
			popup.add(openMenuItem);

			JMenuItem locationMenuItem = new JMenuItem("Open file location");
			locationMenuItem.setEnabled(false);
			popup.add(locationMenuItem);

			Optional.ofNullable(deviceInfo).flatMap(DeviceInfo::getSerialNumber)
			.ifPresent(
					sn->{

						addOpenWebBrowserMenuItem(popup, sn);
						addCalibrateMenuItem(popup, sn);

						if(IrtGui.isProduction())
							addLogInMenuItem(popup, sn);
					});

			JMenuItem updateMenuItem = new JMenuItem("Update");
			popup.add(updateMenuItem);
			updateMenuItem.addActionListener(
					e->{
						if(NetworkPanel.updateButton==null) {
							Platform.runLater(
									()->{
										Alert alert = new Alert(AlertType.WARNING);
										alert.setTitle("Update");
										alert.setHeaderText("It is not possible to update.");
										alert.setContentText("The update is not supported for this unit.");
										alert.showAndWait();
									});
							return;
						}

						NetworkPanel.updateButton.fire();
					});

			new ThreadWorker("Popup Menu Worker").newThread(()->{

				profileScannerFT = new ProfileScannerFT(deviceInfo);
				new ThreadWorker("Profile Scaner").newThread(profileScannerFT).start();
				try {

					profileScannerFT.get().ifPresent(path->{
						openMenuItem.setEnabled(true);
						locationMenuItem.setEnabled(true);
						openMenuItem.addActionListener(
								e->{
									try {
										Desktop.getDesktop().open(path.toFile());
									} catch (IOException e1) {
										logger.catching(e1);
									}
								});
						locationMenuItem.addActionListener(
								e->{
									try {
										Runtime.getRuntime().exec("explorer.exe /select," + path);
									} catch (IOException e1) {
										logger.catching(e1);
									}
								});
						UpdateMessageFx.setProfilePath(path);
					});

				} catch (CancellationException | InterruptedException e) {
					logger.catching(Level.INFO, e);
				} catch (Exception e) {
					logger.catching(e);
				}
			}).start();
		}

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(InfoPanel.this);
							if(secondsCount!=null)
								secondsCount.stop();
							timer.cancel();
							profileScannerFT.cancel(true);
						}));

		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(InfoPanel.this);
				if(secondsCount!=null)
					secondsCount.start();
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(InfoPanel.this);
				if(secondsCount!=null)
					secondsCount.stop();
				timer.cancel();
			}
			public void ancestorMoved(AncestorEvent arg0) {	}
		});

		this.deviceInfo = deviceInfo;

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
				lblError.setText("Firmware Update is Available.");
				lblError.setVisible(false);
				lblError.setBackground(new Color(0,0x33,0x33));
				lblError.setOpaque(true);
				lblError.setForeground(Color.RED);
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

				try {
					Transformer transformer = new Transformer();
					transformer.setComponent(InfoPanel.this);
					transformer.addProcessingComponent(Transformer.ACTION_SHOW, lblVersion);
					transformer.addProcessingComponent(Transformer.ACTION_SHOW, lblVersionTxt);

					if (getHeight() > WINDOW_MIN_HEIGHT)
						transformer.setHeight(WINDOW_MIN_HEIGHT);
					else
						transformer.setHeight(WINDOW_MAX_HEIGHT);

					new ThreadWorker(transformer, "InfoPanel.actionPerformed()");
				} catch (Exception ex) {
					logger.catching(ex);
				}
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

		setInfo(deviceInfo);
	}

	private void addLogInMenuItem(JPopupMenu popup, String serialNumber) {
		JMenuItem httpMenuItem = new JMenuItem("Log In and Open Browser");
		popup.add(httpMenuItem);
		httpMenuItem.addActionListener(
				e->{
					login(serialNumber);
					openBrowser(serialNumber);
				});
	}

	private void login(String serialNumber) {
		HttpURLConnection connection = null;

		try {

			final URL url = new URL("http", serialNumber, "/hidden.cgi");
//			logger.error(url);
			connection = (HttpURLConnection) url.openConnection();	
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			try(	OutputStream outputStream = connection.getOutputStream();
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);){

				outputStreamWriter.write("pwd=jopa");
				outputStreamWriter.flush();

				try(	final InputStream inputStream = connection.getInputStream();
						final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
						final BufferedReader reader = new BufferedReader(inputStreamReader);){

					String line;
					while ((line = reader.readLine()) != null)
						logger.debug(line);
					
				}
			}

		} catch (ConnectException e) {
			logger.catching(Level.DEBUG, e);

		} catch (IOException e) {
			logger.catching(e);
		}

		Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
	}

	private void addCalibrateMenuItem(JPopupMenu popup, String serialNumber) {
		JMenuItem httpMenuItem = new JMenuItem("Calibrate");
		popup.add(httpMenuItem);
		httpMenuItem.addActionListener(e->openBrowser("irttechnologies:8089/calibration?sn=" + serialNumber));
	}

	private void addOpenWebBrowserMenuItem(JPopupMenu popup, String serialNumber) {
		JMenuItem httpMenuItem = new JMenuItem("Open in Web Browser");
		popup.add(httpMenuItem);
		httpMenuItem.addActionListener(e->openBrowser(serialNumber));
	}

	private void openBrowser(String rerialNumber) {
		try {
			Desktop.getDesktop().browse(new URI("http://" + rerialNumber));
		} catch (IOException | URISyntaxException e2) {
			logger.catching(e2);
		}
	}

	public void setInfo(DeviceInfo deviceInfo) {
		if(deviceInfo!=null){

			final String deviceId = deviceInfo.getTypeId()+"."+deviceInfo.getRevision()+"."+deviceInfo.getSubtype();
			if(!deviceId.equals(lblDeviceId.getText()))
				lblDeviceId.setText(deviceId);

			final String version = deviceInfo.getFirmwareVersion().orElse("N/A");
			if(!version.equals(lblVersion.getText()))
				lblVersion.setText(version);

			final String builtDate = deviceInfo.getFirmwareBuildDate().orElse("N/A");
			if(!builtDate.equals(lblBuiltDate.getText()))
				lblBuiltDate.setText(builtDate);

			final String serialNumber = deviceInfo.getSerialNumber().orElse("N/A");
			if(!serialNumber.equals(lblSn.getText()))
				lblSn.setText(serialNumber);

			final String unitName = deviceInfo.getUnitName().orElse("N/A");
			if(!unitName.equals(lblUnitName.getText())) {
				String string = unitName.toString();
				lblUnitName.setText(string);
				lblUnitName.setToolTipText(string);
			}

			final String unitPartNumber = deviceInfo.getUnitPartNumber().orElse("N/A");
			if(!unitPartNumber.equals(lblUnitPartNumber.getText()))
				lblUnitPartNumber.setText(unitPartNumber);

			int uptimeCounter = deviceInfo.getUptimeCounter();
			secondsCount.setUptimeCounter(uptimeCounter);
		}
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

		private volatile int uptimeCounter;

		private ScheduledFuture<?> scheduledFuture;
		private ScheduledExecutorService service;

		public void start() {

			if(Optional.ofNullable(scheduledFuture).filter(s->!s.isDone()).isPresent())
				return;

			if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
				service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("InfoPanel"));

			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(InfoPanel.this);

			scheduledFuture = service.scheduleAtFixedRate(this, 1, 10, TimeUnit.SECONDS);
		}

		public void stop() {
			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(InfoPanel.this);
			Optional.ofNullable(scheduledFuture).filter(sf->!sf.isCancelled()).ifPresent(sf->sf.cancel(true));
			Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
		}

		@Override
		public void run() {
			lblCount.setText(calculateTime(++uptimeCounter));
		}

		public void setUptimeCounter(int uptimeCounter) {
			this.uptimeCounter = uptimeCounter;
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

	private int softCheckerDeley;
	@Override
	public void onPacketReceived(Packet packet) {
		if(deviceInfo == null)
			return;

		new ThreadWorker(()->{

			DeviceInfo
			.parsePacket(packet)
			.filter(di->di.getSerialNumber().equals(deviceInfo.getSerialNumber()))
			.ifPresent(di->{

				setInfo(di);
				deviceInfo.set(di);

				if(secondsCount!=null)
					secondsCount.setUptimeCounter(di.getUptimeCounter());

				if(--softCheckerDeley<0){
					softCheckerDeley = 250;

					final SoftReleaseChecker instance = SoftReleaseChecker.getInstance();
					lblError.setVisible(instance.check(di).orElse(false));
				}
			});
		}, "InfoPanel.onPacketReceived()");
	}
}
