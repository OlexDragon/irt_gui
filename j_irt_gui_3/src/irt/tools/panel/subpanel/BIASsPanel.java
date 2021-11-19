package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.script.ScriptException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.AdcWorker;
import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.DeviceInfo.HardwareType;
import irt.data.RegisterValue;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.InitializePacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork.DeviceDebugPacketIds;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.JSonValueMapper;
import irt.data.value.SonValue;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.button.ImageButton;
import irt.tools.button.Switch;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.textField.RegisterTextField;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@SuppressWarnings("serial")
public class BIASsPanel extends JPanel implements PacketListener, Runnable {

	protected final Logger logger = LogManager.getLogger();

	private static final int P1 = 13;
	private static final int P2 = 40;
	private static final int P3 = 67;
	private static final int P4 = 94;
	private static final int P5 = 121;
	private static final int P6 = 148;
	private static final int P7 = 175;
	private static final int P8 = 202;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private ScheduledExecutorService service;
	private final List<AdcWorker> adcWorkers = new ArrayList<>();

	public enum CalibrationMode{
		OFF,
		ON
	}

	private JSlider slider;
	private JTextField txtStep;
	private SwitchBox switch_1;
	private SwitchBox switch_2;
	private JLabel lblOutput_1;
	private JLabel lblLineUp;
	private SwitchBox switchNGlobal;
	private JCheckBox chckbxStep;
	private RegisterTextField activeTextField;
	private JLabel lblCurrent1_text;
	private JLabel lblCurrent1;
	private JLabel lblCurrent2_text;
	private JLabel lblCurrent2;
	private JLabel lblOutPower;
	private JLabel lblOPower;
	private JLabel lblTemp_1;
	private JLabel lblTemp;

	private final ChangeListener sliderChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			if(activeTextField!=null)
				activeTextField.setText(Integer.toString(slider.getValue()));
		}
	};

	private final FocusListener potentiometerfocusListener = new FocusListener() {
		@Override public void focusGained(FocusEvent e) {

			final RegisterTextField source = (RegisterTextField) e.getSource();
			setColors(source);
			setSliderValue(source);
		}
		private void setSliderValue(RegisterTextField registerTextField) {

			slider.removeChangeListener(sliderChangeListener);

			slider.setMaximum(registerTextField.MAX);
			registerTextField.getValue().ifPresent(slider::setValue);

			slider.addChangeListener(sliderChangeListener);
		}
		private void setColors(RegisterTextField registerTextField) {

			Optional.ofNullable(activeTextField)
			.ifPresent(
					active->{
						active.setBackground(Color.WHITE);
						active.start();
					});

			activeTextField = registerTextField;

			activeTextField.stop();
			activeTextField.setBackground(Color.YELLOW);
		}
		@Override public void focusLost(FocusEvent e) {}
	};

	private DeviceInfo deviceInfo;

	public BIASsPanel(final DeviceInfo deviceInfo, final LinkHeader linkHeader, final boolean isMainBoard) {

		this.deviceInfo = deviceInfo;

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->stop()));

		setLayout(null);

		final byte addr = Optional.ofNullable(linkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

		addAncestorListener(
				new AncestorListener() {
					public void ancestorAdded(AncestorEvent arg0)  { start(); }
					public void ancestorRemoved(AncestorEvent arg0){ stop();  }
					public void ancestorMoved(AncestorEvent arg0) {}
		});

		deviceInfo.getSerialNumber()
		.ifPresent(
				serialNumber->{

					try {

						popupMenu = new JPopupMenu();
						addPopup(this, popupMenu);

						URL url = new URL("http", serialNumber, "/diagnostics.asp?devices=1");

						JMenuItem browseMenuItem = new JMenuItem("Open Diagnostics Page");
						popupMenu.add(browseMenuItem);
						browseMenuItem.addActionListener(
								e-> {
									try {
										Desktop.getDesktop().browse(url.toURI());
									} catch (IOException | URISyntaxException e1) {
										logger.catching(e1);
									}
								});

						JMenuItem copyToClipboardMenuItem = new JMenuItem("Copy to Clipboard");
						copyToClipboardMenuItem.addActionListener(
								e->{
									final String toolTipText = getToolTipText();
									StringSelection stringSelection = new StringSelection(toolTipText);
									Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
								});

						JMenuItem fromWebMenuItem = new JMenuItem("Data frome the Web");
						popupMenu.add(fromWebMenuItem);
						fromWebMenuItem.addActionListener(
								e-> {
									if(browseData) {
										fromWebMenuItem.setText("Data frome the Web");
										browseData = false;
										popupMenu.remove(copyToClipboardMenuItem);
									}else {
										fromWebMenuItem.setText("Off Web");
										browseData = true;
										popupMenu.add(copyToClipboardMenuItem);
									}
								});

					} catch (MalformedURLException e1) {
						logger.catching(e1);
					}
				});

		switch_1 = new Switch(new DeviceDebugPacket(addr, isMainBoard ? DeviceDebugPacketIds.SWITCH_N1 : DeviceDebugPacketIds.SWITCH_N1_REMOTE_BIAS));
		switch_1.setName("Switch #1");
		switch_1.setBounds(61, 2, 55, 25);
		add(switch_1);

		switch_2= new Switch(new DeviceDebugPacket(addr, isMainBoard ? DeviceDebugPacketIds.SWITCH_N2 : DeviceDebugPacketIds.SWITCH_N2_REMOTE_BIAS));
		switch_2.setName("Switch #2");
		switch_2.setBounds(61, 30, 55, 25);
		add(switch_2);

		slider = new JSlider();
		slider.setMinimum(0);
		slider.setOpaque(false);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setBounds(249, 0, 22, 260);
		add(slider);
		slider.addChangeListener(sliderChangeListener);
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(activeTextField!=null)
					activeTextField.send();
			}
		});
		slider.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(activeTextField!=null)
					activeTextField.stop();
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(activeTextField!=null)
					activeTextField.start();
			}
		});

		final Font font = new Font("Tahoma", Font.PLAIN, 14);

		txtStep = new JTextField("1", 10);
		txtStep.setToolTipText("Step Size");
		txtStep.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				slider.setMinorTickSpacing(getStep());
			}
		});
		txtStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				slider.setMinorTickSpacing(getStep());
			}
		});
		txtStep.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStep.setBounds(205, 235, 34, 20);
		add(txtStep);
		txtStep.setFont(font);

		final List<RegisterTextField> allTextFields = new ArrayList<>();

		final RegisterValue registerValue1;
		final RegisterValue registerValue2;
		final RegisterValue registerValue3;
		final RegisterValue registerValue4;
		final RegisterValue registerValue5;
		final RegisterValue registerValue6;
		final RegisterValue registerValue7;
		final RegisterValue registerValue8;

		final String text1;
		final String text2;
		final String text3;
		final String text4;
		final String text5;
		final String text6;
		final String text7;
		final String text8;

		final int maxValue;
		final boolean hpBais = deviceInfo.getDeviceType().map(dt->dt.HARDWARE_TYPE).filter(ht->ht==HardwareType.HP_BAIS).isPresent();
		if(hpBais) {

			maxValue = 896;

			text1 = "OUTPUT1:";
			text2 = "OUTPUT2:";
			text3 = "DRIVER1:";
			text4 = "DRIVER2:";;
			text5 = "PREDRIV:";
			text6 = "PREDRIV:";
			text7 = "8 WATTS:";
			text8 = null;

			if(isMainBoard) {

				registerValue1 = new RegisterValue(1, 0, null);
				registerValue2 = new RegisterValue(1, 8, null);
				registerValue3 = new RegisterValue(2, 0, null);
				registerValue4 = new RegisterValue(2, 8, null);
				registerValue5 = new RegisterValue(3, 8, null);
				registerValue6 = new RegisterValue(3, 0, null);
				registerValue7 = new RegisterValue(7, 8, null);
				registerValue8 = null;

			}else{

				registerValue1 = new RegisterValue(4, 0, null);
				registerValue2 = new RegisterValue(4, 8, null);
				registerValue3 = new RegisterValue(5, 0, null);
				registerValue4 = new RegisterValue(5, 8, null);
				registerValue5 = new RegisterValue(6, 8, null);
				registerValue6 = new RegisterValue(6, 0, null);
				registerValue7 = new RegisterValue(7, 0, null);
				registerValue8 = null;

			}

		//New Bias board
		}else if(deviceInfo.getRevision()>10) {	

			maxValue = 255;

			text1 = "OUTPUT1:";
			text2 = "OUTPUT2:";
			text3 = "DRIVER1:";
			text4 = "DRIVER2:";;
			text5 = "PREDRIV:";
			text6 = "TOT-6:";
			text7 = "POT-7:";
			text8 = "POT-8";

			registerValue1 = new RegisterValue(1, 0x0, null);
			registerValue2 = new RegisterValue(1, 0x8, null);
			registerValue3 = new RegisterValue(1, 0x30, null);
			registerValue4 = new RegisterValue(1, 0x38,null);
			registerValue5 = new RegisterValue(2, 0x0, null);
			registerValue6 = new RegisterValue(2, 0x8, null);
			registerValue7 = new RegisterValue(2, 0x30, null);
			registerValue8 = new RegisterValue(2, 0x38,null);

		}else {

			maxValue = 896;

			text1 = "OUTPUT1:";
			text2 = "OUTPUT2:";
			text3 = "DRIVER1:";
			text4 = "DRIVER2:";;
			text5 = "PREDRIV:";
			text6 = "PREDRIV:";
			text7 = "8 WATTS:";
			text8 = "MMIC:";

			registerValue1 = new RegisterValue(1, 8, null);
			registerValue2 = new RegisterValue(1, 0, null);
			registerValue3 = new RegisterValue(7, 8, null);
			registerValue4 = new RegisterValue(7, 0, null);
			registerValue5 = new RegisterValue(2, 0, null);
			registerValue6 = new RegisterValue(2, 8, null);
			registerValue7 = null;
			registerValue8 = null;
		}

		// 1 -registerValue1
		Optional.ofNullable(registerValue1).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1, P1, text1, maxValue, allTextFields));

		// 2 -registerValue2
		Optional.ofNullable(registerValue2).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2, P2, text2, maxValue, allTextFields));

		// 3 -registerValue3
		Optional.ofNullable(registerValue3).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3, P3, text3, maxValue, allTextFields));

		// 4 -registerValue4
		Optional.ofNullable(registerValue4).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4, P4, text4, maxValue, allTextFields));

		// 5 - registerValue5
		Optional.ofNullable(registerValue5).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5, P5, text5, maxValue, allTextFields));

		// 6 - registerValue6
		Optional.ofNullable(registerValue6).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6, P6, text6, maxValue, allTextFields));

		// 7 - registerValue7
		Optional.ofNullable(registerValue7).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N7, P7, text7, maxValue, allTextFields));

		// 8 - registerValue8
		Optional.ofNullable(registerValue8).ifPresent(addFields(addr, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N8, P8, text8, maxValue, allTextFields));

//		FocusListener listener = new FocusListener() {
//			
//			@Override public void focusGained(FocusEvent e) {
//				Component c = (Component) e.getSource();
//				final String[] a = c.getName().split(":");
//
//				// Set measurement name
//				lblCurrent1_text.setText(a[0] + '1');
//				lblCurrent2_text.setText(a[0] + '2');
//				lblCurrent1.setText(":");
//				lblCurrent2.setText(":");
//				
//				synchronized(adcWorkers) {
//
//					final List<AdcWorker> toRemove = adcWorkers.parallelStream()
//
//							.filter(
//									w->{
//										final JLabel label = w.getLabel();
//										return label==lblCurrent1 || label==lblCurrent2;
//									})
//							.collect(Collectors.toList());
//
//					adcWorkers.removeAll(toRemove);
//					adcWorkers.add(new AdcWorker(lblCurrent1, addr, null, DeviceDebugPacketIds.valueOf(a[1 ]+ '1' + a[2]), 1, "#.### A"));
//					adcWorkers.add(new AdcWorker(lblCurrent2, addr, null, DeviceDebugPacketIds.valueOf(a[1 ]+ '2' + a[2]), 1, "#.### A"));
//				}
//			}
//			@Override public void focusLost(FocusEvent e) { }
//		};

//		allTextFields.forEach(tf->tf.addFocusListener(listener));

		lblOutput_1 = new JLabel("OUTPUT");
		lblOutput_1.setRequestFocusEnabled(false);
		lblOutput_1.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblOutput_1.setBounds(10, 7, 57, 17);
		add(lblOutput_1);

		lblLineUp = new JLabel("LINE UP");
		lblLineUp.setRequestFocusEnabled(false);
		lblLineUp.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblLineUp.setBounds(10, 34, 57, 17);
		add(lblLineUp);

		switchNGlobal = new Switch(new DeviceDebugPacket(addr, isMainBoard ? DeviceDebugPacketIds.NGLOBAL : DeviceDebugPacketIds.NGLOBAL_REMOTE_BIAS));
		switchNGlobal.setName("Global");
		switchNGlobal.setEnabled(false);
		switchNGlobal.setBounds(61, 58, 55, 25);
		add(switchNGlobal);

		JLabel lblNglobal = new JLabel("NGlobal");
		lblNglobal.setRequestFocusEnabled(false);
		lblNglobal.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblNglobal.setBounds(10, 63, 57, 17);
		add(lblNglobal);

		chckbxStep = new JCheckBox("");
		chckbxStep.setText("Enable step");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setSnapToTicks(chckbxStep.isSelected());
			}
		});

		final Font font2 = font.deriveFont(12f);
		chckbxStep.setFont(font2.deriveFont(11f));
		chckbxStep.setOpaque(false);
		chckbxStep.setBounds(184, 234, 22, 23);
		add(chckbxStep);

		// HP BAIAS Current Measurement
		if(hpBais) {

//			txtPotentiometer1.setName(isMainBoard ? "HSS 1.:HS1_:_CURRENT_HP_BIAS" : "HSS 3.:HS3_:_CURRENT_HP_BIAS");	// HSS1.1 or HSS3.1
//			txtPotentiometer2.setName(isMainBoard ? "HSS 1.:HS1_:_CURRENT_HP_BIAS" : "HSS 3.:HS3_:_CURRENT_HP_BIAS");	// HSS1.2 or HSS3.2
//			txtPotentiometer3.setName(isMainBoard ? "HSS 2.:HS2_:_CURRENT_HP_BIAS" : "HSS 4.:HS4_:_CURRENT_HP_BIAS");	// HSS2.1 or HSS4.1
//			txtPotentiometer4.setName(isMainBoard ? "HSS 2.:HS2_:_CURRENT_HP_BIAS" : "HSS 4.:HS4_:_CURRENT_HP_BIAS");	// HSS2.2 or HSS4.2
		}

		lblCurrent1_text = new JLabel("CURR:");
		lblCurrent1_text.setRequestFocusEnabled(false);
		lblCurrent1_text.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent1_text.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblCurrent1_text.setBounds(10, 161, 41, 13);
		add(lblCurrent1_text);

		lblCurrent1 = new JLabel(":");
		lblCurrent1.setRequestFocusEnabled(false);
		lblCurrent1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent1.setFont(font2);
		lblCurrent1.setBounds(54, 158, 68, 17);
		add(lblCurrent1);

		lblCurrent2_text = new JLabel("CURR:");
		lblCurrent2_text.setRequestFocusEnabled(false);
		lblCurrent2_text.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent2_text.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblCurrent2_text.setBounds(10, 189, 41, 13);
		add(lblCurrent2_text);
		
		lblCurrent2 = new JLabel(":");
		lblCurrent2.setRequestFocusEnabled(false);
		lblCurrent2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent2.setFont(font2);
		lblCurrent2.setBounds(54, 186, 68, 17);
		add(lblCurrent2);
		
		lblOutPower = new JLabel("POWER:");
		lblOutPower.setRequestFocusEnabled(false);
		lblOutPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutPower.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblOutPower.setBounds(10, 245, 41, 13);
		add(lblOutPower);
		
		lblOPower = new JLabel(":");
		lblOPower.setRequestFocusEnabled(false);
		lblOPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOPower.setFont(font2);
		lblOPower.setBounds(54, 242, 68, 17);
		add(lblOPower);
		
		lblTemp_1 = new JLabel("TEMP:");
		lblTemp_1.setRequestFocusEnabled(false);
		lblTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemp_1.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblTemp_1.setBounds(20, 221, 32, 13);
		add(lblTemp_1);

		lblTemp = new JLabel(":");
		lblTemp.setRequestFocusEnabled(false);
		lblTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemp.setFont(font2);
		lblTemp.setBounds(54, 214, 68, 17);
		add(lblTemp);

		boolean isNewBiasBoard = GuiController.getDeviceInfo(linkHeader).map(di->di.getDeviceType().filter(dt->dt.TYPE_ID<1000).map(dt->true).orElse(false) && di.getRevision()>=2).orElse(true);

		final Optional<DeviceType> hpBias = deviceInfo.getDeviceType().filter(dt->dt.TYPE_ID>=DeviceType.HPB_L_TO_KU.TYPE_ID && dt.TYPE_ID<=DeviceType.KA_SSPA.TYPE_ID);
//		int index = hpBias.map(dt->20).orElse(isMainBoard ? 1 :201);

		double multiplier;
		if(isNewBiasBoard){
//			lblPotentiometer1.setText("Output1:");
//			lblPotentiometer2.setText("Output2:");
//			lblPotentiometer3.setText("Driver1:");
//			lblPotentiometer4.setText("Driver2:");
//			lblPotentiometer5.setText("Driver:");
//			lblPotentiometer6.setText("Pred.Dr");

//			index = isMainBoard ? 7 : 207;
			multiplier = 10.8;
		}else 
			multiplier = 5.4;

//		index = hpBias.map(dt->20).orElse(isMainBoard ? 5 : 205);

		DeviceDebugPacketIds hs1 = hpBias.map(dt->isMainBoard ? DeviceDebugPacketIds.HS1_1_CURRENT_HP_BIAS : DeviceDebugPacketIds.HS3_1_CURRENT_HP_BIAS).orElse(isMainBoard ? DeviceDebugPacketIds.HS1_CURRENT : DeviceDebugPacketIds.HS1_CURRENT_REMOTE_BIAS);
		AdcWorker adcWorker = new AdcWorker(lblCurrent1, addr, null, hs1, multiplier, "#.### A");
		synchronized(adcWorkers) { adcWorkers.add(adcWorker); }

		DeviceDebugPacketIds hs2 = hpBias.map(dt->isMainBoard ? DeviceDebugPacketIds.HS2_1_CURRENT_HP_BIAS : DeviceDebugPacketIds.HS3_1_CURRENT_HP_BIAS).orElse(isMainBoard ? DeviceDebugPacketIds.HS2_CURRENT : DeviceDebugPacketIds.HS2_CURRENT_REMOTE_BIAS);
		adcWorker = new AdcWorker(lblCurrent2, 	addr, null, hs2, multiplier, "#.### A");
		synchronized(adcWorkers) { adcWorkers.add(adcWorker); }

		DeviceDebugPacketIds outputPower = hpBias.map(dt->DeviceDebugPacketIds.OUTPUT_POWER_HP_BIAS).orElse(isMainBoard ? DeviceDebugPacketIds.OUTPUT_POWER : DeviceDebugPacketIds.OUTPUT_POWER_REMOTE_BIAS);
		adcWorker = new AdcWorker(lblOPower, 	addr, null, outputPower, 0, "#.###");
		synchronized(adcWorkers) {	adcWorkers.add(adcWorker); }

		DeviceDebugPacketIds temperature = hpBias.map(dt->DeviceDebugPacketIds.TEMPERATURE_HP_BIAS).orElse(isMainBoard ? DeviceDebugPacketIds.TEMPERATURE : DeviceDebugPacketIds.TEMPERATURE_REMOTE_BIAS);
		adcWorker = new AdcWorker(lblTemp, 		addr, null, temperature, 0, "#.###");
		synchronized(adcWorkers) {	adcWorkers.add(adcWorker); }

		URL resource = IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png");
		ImageButton btnInItialise = new ImageButton(resource!=null ? new ImageIcon(resource).getImage() : null);
		btnInItialise.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnInItialise.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				ThreadWorker.runThread(
						()->{

							try {

								if(setCalibrationModeOn()) {

									final String errorMessage = initialize();

									// No error
									if(errorMessage.isEmpty()) {
										Platform.runLater(
												()->{

													Alert alert = new Alert(AlertType.CONFIRMATION);
													alert.setTitle("Initialize");
													alert.setHeaderText(null);
													alert.setContentText("Initialization completed.");
													alert.showAndWait();												
												});
										return;
									}

									Platform.runLater(
											()->{

												Alert alert = new Alert(AlertType.ERROR);
												alert.setTitle("Initialize Error");
												alert.setHeaderText(null);
												alert.setContentText(errorMessage);
												alert.showAndWait();												
											});

								}else {
									Platform.runLater(
											()->{

												Alert alert = new Alert(AlertType.ERROR);
												alert.setTitle("Calibration Mode Error");
												alert.setHeaderText(null);
												alert.setContentText("Cannot enable calibration mode.");
												alert.showAndWait();												
											});
								}

							} catch (InterruptedException | ExecutionException e) {
								logger.catching(e);
							}

						}, "Bias Board Initialisation");

//				SwingUtilities.invokeLater(()->{
//
//					if(isMainBoard) {
//
//						final Optional<Boolean> oCallMode = checkCallibrationMode(addr);
//						if(!oCallMode.isPresent()) {
//							showMessage("Packet does not have value.");
//							return;
//						}
//
//						final Boolean callMode = oCallMode.get();
//						if(!callMode) {
//							if(!setCallibrationModeOn(addr).filter(b->b).orElse(false)) {
//								showMessage("Unable to set calibration mode on.");
//								return;
//							}
//						}
//
//						final boolean initialize = initialize(addr).orElse(false);
//
//						if(initialize)
//							showMessage("Initialization complete.\nReboot the unit.");
//						else
//							showMessage("Unable to initialize.");
//
//					}else{
//						logger.trace("\n\t{}", controller);
//						if(controller==null || !controller.isRun()){
//							if(setCalibrationMode(CalibrationMode.ON)){
//
//								if(	initialisePotenciometr(1,0x10, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(1,0x11, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(1,0x12, 0xFFFF) &&
//									initialisePotenciometr(1,0x13, 0xFFFF)
//								);
//
//								if(	initialisePotenciometr(2,0x10, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(2,0x11, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(2,0x12, 0xFFFF) &&
//									initialisePotenciometr(2,0x13, 0xFFFF)
//								);
//
//								if(	initialisePotenciometr(7,0x10, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(7,0x11, MAX_POTENTIOMETER_VALUE) &&
//									initialisePotenciometr(7,0x12, 0xFFFF) &&
//									initialisePotenciometr(7,0x13, 0xFFFF)
//								);
//
//								setCalibrationMode(CalibrationMode.OFF);
//							}else
//								showMessage("Set Calibration Mode 'ON' error.");
//						}else
//							showMessage("Operation is not completed");
//					}
//				});
			}

			private Boolean setCalibrationModeOn() throws InterruptedException, ExecutionException {

				final AtomicBoolean value = new AtomicBoolean();
				final Callable<Boolean> callable = ()->value.get();
				final FutureTask<Boolean> ft = new FutureTask<>(callable);
				final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

				PacketListener packetListener = new PacketListener() {

					private boolean haveToSend = true;

					@Override
					public void onPacketReceived(Packet packet) {

						final Optional<Packet> myPacket = Optional.of(packet).filter(PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE::match);

						if(myPacket.isPresent())
							ThreadWorker.runThread(
									()->{

										if(
												myPacket.flatMap(PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE::valueOf)
												.filter(
														b->{
															final Boolean bool = (Boolean)b;
															value.set(bool);
															return !bool;
														})
												.isPresent()) {

											if(haveToSend) {
												haveToSend = false;
												comPortThreadQueue.add(new CallibrationModePacket(addr, true));
												return;
											}
										}

										comPortThreadQueue.removePacketListener(this);
										ThreadWorker.runThread(ft, "Get Result");

									}, "Calibration Mode Listener");
					}
				};

				comPortThreadQueue.addPacketListener(packetListener);
				comPortThreadQueue.add(new CallibrationModePacket(addr));

				return ft.get();
			}

			private String initialize() throws InterruptedException, ExecutionException {

				final StringBuilder value = new StringBuilder();
				final Callable<String> callable = ()->value.toString();
				final FutureTask<String> ft = new FutureTask<>(callable);
				final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

				PacketListener packetListener = new PacketListener() {

					@Override
					public void onPacketReceived(Packet packet) {

						final Optional<Packet> myPacket = Optional.of(packet).filter(PacketIDs.PRODUCTION_GENERIC_SET_1_INITIALIZE::match);

						if(myPacket.isPresent())
							ThreadWorker.runThread(
									()->{

										final PacketHeader header = packet.getHeader();

										if(header.getPacketType()==PacketImp.PACKET_TYPE_COMMAND)
											value.append("The Unit did not answer.");

										else if(header.getError()!=0)
											value.append("The Unit answer with an error: " + header.getErrorStr());

										comPortThreadQueue.removePacketListener(this);
										ThreadWorker.runThread(ft, "Get Result");

									}, "Initialize Listener");
					}
				};
				comPortThreadQueue.addPacketListener(packetListener);
				comPortThreadQueue.add(new InitializePacket(addr));

				return ft.get();
			}
		});

		btnInItialise.setToolTipText("Initialize");
		btnInItialise.setName("Initialize");
		btnInItialise.setBounds(68, 90, 33, 33);
		add(btnInItialise);
		
		JLabel lblInitialize = new JLabel("Initialize");
		lblInitialize.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblInitialize.setHorizontalAlignment(SwingConstants.CENTER);
		lblInitialize.setBounds(11, 100, 46, 14);
		add(lblInitialize);
		
		JLabel label = new JLabel(isMainBoard ? "1" : "2");
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Tahoma", Font.PLAIN, 99));
		label.setBounds(90, 69, 68, 94);
		add(label);
		
		label = new JLabel(isMainBoard ? "1" : "2");
		label.setForeground(Color.DARK_GRAY);
		label.setFont(new Font("Tahoma", Font.PLAIN, 99));
		label.setBounds(90, 70, 68, 94);
		add(label);
		
		JButton btnSave = new JButton("S");
		btnSave.setToolTipText("Save");
		btnSave.setMargin(new Insets(0,0,0,0));
		btnSave.addActionListener(e->allTextFields.forEach(RegisterTextField::saveRegister));
		btnSave.setBounds(140, 234, 34, 23);
		add(btnSave);
	}

	public Consumer<? super RegisterValue> addFields(final byte addr, PacketIDs packetID, int y, String lblText, final int maxValue, List<RegisterTextField> allTextFields) {

		return rv->{

			RegisterTextField tf = new RegisterTextField(addr, rv, packetID, 0, maxValue);
			tf.setHorizontalAlignment(SwingConstants.RIGHT);
			tf.setFont(new Font("Tahoma", Font.PLAIN, 14));
			tf.setColumns(10);
			tf.setBounds(184, y, 55, 20);
			tf.addFocusListener(potentiometerfocusListener);
			add(tf);
			allTextFields.add(tf);

			JLabel lbl = new JLabel(lblText);
			lbl.setRequestFocusEnabled(false);
			lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl.setFont(new Font("Tahoma", Font.PLAIN, 12));
			lbl.setBounds(126, y, 57, 17);
			add(lbl);
		};
	}

	private int getStep() {
		String s = txtStep.getText().replaceAll("\\D", "");
		int step = 1;

		if(s.length()>0){
			step = Integer.parseInt(s);
			if(step>100)
				step = 100;
			else if(step==0)
				step = 1;
		}
		txtStep.setText(""+step);

		return step;
	}

	@Override
	public void onPacketReceived(Packet packet) {

		final PacketHeader header = packet.getHeader();
		if(header==null)
			return;

		final byte groupId = header.getGroupId();
		if(!PacketGroupIDs.DEVICE_DEBUG.match(groupId))
			return;

		synchronized(adcWorkers) { adcWorkers.stream().forEach(adc->adc.update(packet)); }
	}

	private int delay;
	private JPopupMenu popupMenu;
	private boolean browseData;
	private boolean httpRequest;
	@Override
	public void run() {

		try {
			// HTTP request
			final boolean isPresent;
			synchronized(adcWorkers) {
				isPresent = adcWorkers.parallelStream().filter(w->w.getLabel().equals(lblOPower)).map(AdcWorker::getPacketToSend).filter(w->w!=null).findAny().isPresent();
			}

			if(!isPresent || browseData){

				if(httpRequest)
					return;

				ThreadWorker.runThread(
						()->{

							httpRequest = true;

							try {
								final List<SonValue> sonValues = deviceInfo.getSerialNumber()

										.map(
												sn->{
													try {
														return getHttpUpdate(sn);
													} catch (ScriptException e1) {
														logger.catching(e1);
														return null;
													}
												}).orElse(null);

//							logger.error("{}", sonValues);
								if(sonValues!=null) {

									if(sonValues.size()==0) {

										deviceInfo.getSerialNumber().ifPresent(this::loginToHttp);

										return;
									}

									sonValues.parallelStream().filter(sv->sv.getName().equals("bias")).map(SonValue::getValue).map(a->(List<?>)a)
										.flatMap(List::parallelStream).map(SonValue.class::cast)
										.forEach(
												sv->{

													final String name = sv.getName();
													final Object value = sv.getValue();

													switch(name) {

													case "det1":
													case "power":
														((List<?>)value).stream().map(SonValue.class::cast).filter(v->v.getName().equals("value")).findAny().ifPresent(v->lblOPower.setText(v.getValue().toString()));
														break;

													case "temperature":
														lblTemp.setText(sv.getValue().toString());
														break;

													case "det2":
													case "refl_power":
														lblCurrent2_text.setText("R.Pow.:");
														((List<?>)value).stream().map(SonValue.class::cast).filter(v->v.getName().equals("value")).findAny().ifPresent(v->lblCurrent2.setText(v.getValue().toString()));
													}
												});
								}
							} catch (Exception e) {
								logger.catching(e);
							}
							httpRequest = false;
						}, "HTTP request");

				return;
			}

			final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
			final int size = queue.size();

//		logger.error("delay: {}; size: {}", delay, size);

			if(delay<=0)
				try{
					new ThreadWorker(
							()->{
								synchronized(adcWorkers) { 
									adcWorkers
									.stream()
									.forEach(adc->queue.add(adc.getPacketToSend()));
								}
							}, "BIASsPanel.run()");
				}catch (Exception e) {
					logger.catching(e);
				}
			else
				delay--;

			if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<=0)
				delay = ComPortThreadQueue.DELAY_TIMES;
			else if(size<=ComPortThreadQueue.QUEUE_SIZE_TO_RESUME)
				delay = -1;

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private synchronized void start() {

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);


		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("BIASsPanel"));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(BIASsPanel.this);
		scheduleAtFixedRate = service.scheduleAtFixedRate(BIASsPanel.this, 1, 3, TimeUnit.SECONDS);
	}

	private void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
//
//		synchronized(adcWorkers) {
//			adcWorkers.clear();
//		}

		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	public static class FixedSizeBuffer<T> extends ArrayList<T> {
		private static final long serialVersionUID = 1814997559400788891L;

		private int capacity;

		public FixedSizeBuffer(int initialCapacity) {
			super(initialCapacity);
			capacity = initialCapacity;
		}

		@Override
		public boolean add(T value) {
			final int size = size();
			if(capacity<=size)
				removeRange(0, capacity-size+1);
			return super.add(value);
		}

		@Override
		public void add(int index, T element) {
			throw new UnsupportedOperationException("This function can not be used");
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			throw new UnsupportedOperationException("This function can not be used");
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			throw new UnsupportedOperationException("This function can not be used");
		}
	}

	private void loginToHttp(String ipAddress) {

		HttpURLConnection connection = null;

		try {

			final URL url = new URL("http", ipAddress, "/hidden.cgi");
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

	private List<SonValue> getHttpUpdate(String ipAddress) throws ScriptException {

		StringBuilder sb = new StringBuilder();
		String urlParams = "exec=calib_ro_info&_http_id=irt";

		HttpURLConnection connection = null;

		try {

			final URL url = new URL("http", ipAddress, "/update.cgi");
			connection = (HttpURLConnection) url.openConnection();	
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");

			try(	OutputStream outputStream = connection.getOutputStream();
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);){

				outputStreamWriter.write(urlParams);
				outputStreamWriter.flush();

				String line;
				try(	final InputStream inputStream = connection.getInputStream();
						final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
						final BufferedReader reader = new BufferedReader(inputStreamReader);){

					while ((line = reader.readLine()) != null)
						sb.append(line);
					
				}
			}

		} catch (ConnectException e) {
			logger.catching(Level.DEBUG, e);

		} catch (IOException e) {
			logger.catching(e);
		}

		Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
		JSonValueMapper mapper = new JSonValueMapper();
		final String sonString = sb.toString();
		setToolTipText(sonString);
		return mapper.toSonValue(sonString);
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
//
//	public class ParsePacket implements Callable<Optional<?>>{
//
//		private Function<Packet, Optional<Object>> function;
//		private Packet packet;
//
//		public ParsePacket(Function<Packet, Optional<Object>> function) {
//			this.function = function;
//		}
//		public Packet getPacket() {
//			return packet;
//		}
//		public void setPacket(Packet packet) {
//			this.packet = packet;
//		}
//
//		@Override
//		public Optional<?> call() throws Exception {
//			if(function==null || packet==null)
//				return Optional.empty();
//			return function.apply(packet);
//		}
//	}
}
