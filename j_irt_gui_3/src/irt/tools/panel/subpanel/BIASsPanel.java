package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.SetterController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.setter.DeviceDebagSetter;
import irt.controller.serial_port.value.setter.Setter;
import irt.controller.to_do.InitializePicoBuc;
import irt.data.AdcWorker;
import irt.data.DeviceInfo.DeviceType;
import irt.data.MyThreadFactory;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.DeviceDebugPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.button.ImageButton;
import irt.tools.button.Switch;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.textField.RegisterTextField;

@SuppressWarnings("serial")
public class BIASsPanel extends JPanel implements PacketListener, Runnable {

	protected final Logger logger = LogManager.getLogger();

	private static final int MAX_POTENTIOMETER_VALUE = 896;
	private static final int P1 = 13;
	private static final int P2 = 44;
	private static final int P3 = 75;
	private static final int P4 = 106;
	private static final int P5 = 137;
	private static final int P6 = 170;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private final List<AdcWorker> adcWorkers = new ArrayList<>();

	public enum CalibrationMode{
		OFF,
		ON
	}

	private JSlider slider;
	private JTextField txtStep;
	private SwitchBox switch_1;
	private SwitchBox switch_2;
	private RegisterTextField txtPotentiometer1;
	private RegisterTextField txtPotentiometer2;
	private RegisterTextField txtPotentiometer3;
	private RegisterTextField txtPotentiometer4;
	private RegisterTextField txtPotentiometer5;
	private RegisterTextField txtPotentiometer6;
	private JLabel lblPotentiometer1;
	private JLabel lblPotentiometer4;
	private JLabel lblPotentiometer2;
	private JLabel lblPotentiometer3;
	private JLabel lblPotentiometer5;
	private JLabel lblPotentiometer6;
	private JLabel lblOutput_1;
	private JLabel lblLineUp;
	private SwitchBox switchNGlobal;
	private JCheckBox chckbxStep;
	private RegisterTextField activeTextField;
	private JLabel lblCurrent_11;
	private JLabel lblCurrent1;
	private JLabel lblCurr;
	private JLabel lblCurrent2;
	private JLabel lblOutPower;
	private JLabel lblOPower;
	private JLabel lblTemp_1;
	private JLabel lblTemp;

	private final FocusListener potentiometerfocusListener = new FocusListener() {
		@Override public void focusGained(FocusEvent e) {
			final RegisterTextField source = (RegisterTextField) e.getSource();
			setColors(source);
			setSliderValue(source);
		}
		private void setSliderValue(RegisterTextField registerTextField) {
			final String text = registerTextField.getText();
			if(text.isEmpty())
				return;

			slider.setValue(Integer.parseInt(text));
		}
		private void setColors(RegisterTextField registerTextField) {
			if(activeTextField!=null){
				activeTextField.setBackground(Color.WHITE);
				activeTextField.start();
			}

			activeTextField = registerTextField;
			activeTextField.stop();

			activeTextField.setBackground(Color.YELLOW);
		}
		@Override public void focusLost(FocusEvent e) {}
	};

	public BIASsPanel(final Optional<DeviceType> deviceType, final LinkHeader linkHeader, final boolean isMainBoard) {

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
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(BIASsPanel.this);
							service.shutdownNow();
						}));

		setLayout(null);

		final byte addr = linkHeader.getAddr();
		addAncestorListener(new AncestorListener() {

			private List<ControllerAbstract> threadList = new ArrayList<>();

			public void ancestorAdded(AncestorEvent arg0) {

				start();

				boolean isNewBiasBoard = GuiController.getDeviceInfo(linkHeader).map(di->di.getDeviceType().filter(dt->dt.TYPE_ID<1000).map(dt->true).orElse(false) && di.getRevision()>=2).orElse(true);


				final Optional<DeviceType> hpBias = deviceType.filter(dt->dt.TYPE_ID>=DeviceType.HPB_L_TO_KU.TYPE_ID && dt.TYPE_ID<=DeviceType.KA_SSPA.TYPE_ID);
				int index = hpBias.map(dt->20).orElse(isMainBoard ? 1 :201);

				double multiplier;
				if(isNewBiasBoard){
					lblPotentiometer1.setText("Output1:");
					lblPotentiometer2.setText("Output2:");
					lblPotentiometer3.setText("Driver1:");
					lblPotentiometer4.setText("Driver2:");
					lblPotentiometer5.setText("Driver:");
					lblPotentiometer6.setText("Pred.Dr");

//					index = isMainBoard ? 7 : 207;
					multiplier = 10.8;
				}else 
					multiplier = 5.4;

				index = hpBias.map(dt->20).orElse(isMainBoard ? 5 : 205);
				
				adcWorkers.add(new AdcWorker(lblCurrent1, 	addr, new RegisterValue(index, 1, null), PacketWork.PACKET_ID_DEVICE_DEBUG_HS1_CURRENT, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, multiplier, "#.### A"));
				adcWorkers.add(new AdcWorker(lblCurrent2, 	addr, new RegisterValue(index, 2, null), PacketWork.PACKET_ID_DEVICE_DEBUG_HS2_CURRENT, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, multiplier, "#.### A"));
				adcWorkers.add(new AdcWorker(lblOPower, 	addr, new RegisterValue(index, hpBias.map(dt->0).orElse(3), null), PacketWork.PACKET_ID_DEVICE_DEBUG_OUTPUT_POWER, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 0, "#.###"));
				adcWorkers.add(new AdcWorker(lblTemp, 		addr, new RegisterValue(index, 4, null), PacketWork.PACKET_ID_DEVICE_DEBUG_TEMPERATURE, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 0, "#.###"));
			}
			public void ancestorMoved(AncestorEvent arg0) {}

			public void ancestorRemoved(AncestorEvent arg0) {

				stop();

				for(ControllerAbstract t:threadList)
					t.stop();
				
				threadList.clear();
			}
		});

		switch_1 = new Switch(new DeviceDebugPacket(addr, new RegisterValue(isMainBoard ? 3 : 203, 1, null), PacketWork.PACKET_ID_DEVICE_DEBUG_SWITCH_N1, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE));
		switch_1.setBounds(61, 2, 55, 25);
		add(switch_1);

		switch_2= new Switch(new DeviceDebugPacket(addr, new RegisterValue(isMainBoard ? 4 : 204, 1, null), PacketWork.PACKET_ID_DEVICE_DEBUG_SWITCH_N2, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE));
		switch_2.setBounds(61, 30, 55, 25);
		add(switch_2);

		slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(896);
		slider.setOpaque(false);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setBounds(249, 0, 22, 260);
		add(slider);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(activeTextField!=null)
					activeTextField.setText(Integer.toString(slider.getValue()));
			}
		});
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

		Font font = new Font("Tahoma", Font.PLAIN, 14);


		txtStep = new JTextField("1", 10);
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
		txtStep.setBounds(205, 203, 34, 20);
		add(txtStep);
		txtStep.setFont(font);

		txtPotentiometer1 = new RegisterTextField(addr, new RegisterValue(1, 8, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N1, 0, 896);
		txtPotentiometer1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer1.setBounds(184, P1, 55, 20);
		txtPotentiometer1.setFont(font);
		txtPotentiometer1.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer1);

		txtPotentiometer2 = new RegisterTextField(addr, new RegisterValue(1, 0, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N2, 0, 896);
		txtPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer2.setFont(font);
		txtPotentiometer2.setBounds(184, P2, 55, 20);
		txtPotentiometer2.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer2);

		txtPotentiometer3 = new RegisterTextField(addr, new RegisterValue(7, 8, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N3, 0, 896);
		txtPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer3.setFont(font);
		txtPotentiometer3.setBounds(184, P3, 55, 20);
		txtPotentiometer3.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer3);

		txtPotentiometer4 = new RegisterTextField(addr, new RegisterValue(7, 0, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N4, 0, 896);
		txtPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer4.setFont(font);
		txtPotentiometer4.setBounds(184, P4, 55, 20);
		txtPotentiometer4.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer4);

		txtPotentiometer5 = new RegisterTextField(addr, new RegisterValue(2, 0, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N5, 0, 896);
		txtPotentiometer5.setText("0");
		txtPotentiometer5.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer5.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer5.setColumns(10);
		txtPotentiometer5.setBounds(184, P5, 55, 20);
		txtPotentiometer5.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer5);
		
		txtPotentiometer6 = new RegisterTextField(addr, new RegisterValue(2, 8, null), PacketWork.PACKET_ID_DEVICE_DEBUG_POTENTIOMETER_N6, 0, 896);
		txtPotentiometer6.setText("0");
		txtPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer6.setColumns(10);
		txtPotentiometer6.setBounds(184, P6, 55, 20);
		txtPotentiometer6.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer6);

		font = font.deriveFont(12f);

		lblPotentiometer1 = new JLabel("OUTPUT:");
		lblPotentiometer1.setRequestFocusEnabled(false);
		lblPotentiometer1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer1.setFont(font);
		lblPotentiometer1.setBounds(126, 13, 57, 17);
		add(lblPotentiometer1);

		lblPotentiometer4 = new JLabel("MMIC:");
		lblPotentiometer4.setRequestFocusEnabled(false);
		lblPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer4.setFont(font);
		lblPotentiometer4.setBounds(126, 106, 57, 17);
		add(lblPotentiometer4);

		lblPotentiometer2 = new JLabel("DRIVER:");
		lblPotentiometer2.setRequestFocusEnabled(false);
		lblPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer2.setFont(font);
		lblPotentiometer2.setBounds(126, 47, 57, 17);
		add(lblPotentiometer2);

		lblPotentiometer3 = new JLabel("PRED:");
		lblPotentiometer3.setRequestFocusEnabled(false);
		lblPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer3.setFont(font);
		lblPotentiometer3.setBounds(126, 78, 57, 17);
		add(lblPotentiometer3);

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

		switchNGlobal = new Switch(new DeviceDebugPacket(addr, new RegisterValue(isMainBoard ? 6 : 206, 0, null), PacketWork.PACKET_ID_DEVICE_DEBUG_NGLOBAL, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE));
		switchNGlobal.setEnabled(false);
		switchNGlobal.setBounds(61, 58, 55, 25);
		add(switchNGlobal);

		JLabel lblNglobal = new JLabel("NGlobal");
		lblNglobal.setRequestFocusEnabled(false);
		lblNglobal.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblNglobal.setBounds(10, 63, 57, 17);
		add(lblNglobal);

		chckbxStep = new JCheckBox("Step:");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setSnapToTicks(chckbxStep.isSelected());
			}
		});
		chckbxStep.setFont(font.deriveFont(11f));
		chckbxStep.setOpaque(false);
		chckbxStep.setBounds(152, 202, 55, 23);
		add(chckbxStep);

		lblCurrent_11 = new JLabel("CURR:");
		lblCurrent_11.setRequestFocusEnabled(false);
		lblCurrent_11.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent_11.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblCurrent_11.setBounds(20, 161, 31, 13);
		add(lblCurrent_11);

		lblCurrent1 = new JLabel(":");
		lblCurrent1.setRequestFocusEnabled(false);
		lblCurrent1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent1.setFont(font);
		lblCurrent1.setBounds(54, 158, 68, 17);
		add(lblCurrent1);

		lblCurr = new JLabel("CURR:");
		lblCurr.setRequestFocusEnabled(false);
		lblCurr.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurr.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblCurr.setBounds(20, 189, 31, 13);
		add(lblCurr);
		
		lblCurrent2 = new JLabel(":");
		lblCurrent2.setRequestFocusEnabled(false);
		lblCurrent2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent2.setFont(font);
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
		lblOPower.setFont(font);
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
		lblTemp.setFont(font);
		lblTemp.setBounds(54, 214, 68, 17);
		add(lblTemp);

		
		URL resource = IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png");
		ImageButton imageButton = new ImageButton(resource!=null ? new ImageIcon(resource).getImage() : null);
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.addMouseListener(new MouseAdapter() {
			DefaultController controller;

			@Override
			public void mouseClicked(MouseEvent arg0) {
				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {

						if(isMainBoard)
							new SetterController(deviceType, "Initialize UnitController",
									new Setter(linkHeader,
										PacketImp.PACKET_TYPE_COMMAND,
										PacketImp.GROUP_ID_PRODUCTION_GENERIC_SET_1,
										PacketImp.PARAMETER_ID_PRODUCTION_GENERIC_SET_1_DP_INIT,
										PacketWork.PACKET_ID_PRODUCTION_GENERIC_SET_1_INITIALIZE
									),
									new InitializePicoBuc(BIASsPanel.this), Style.CHECK_ONCE
							);
						else{
							logger.trace("\n\t{}", controller);
							if(controller==null || !controller.isRun()){
								if(setCalibrationMode(CalibrationMode.ON)){

									if(	initialisePotenciometr(1,0x10, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(1,0x11, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(1,0x12, 0xFFFF) &&
										initialisePotenciometr(1,0x13, 0xFFFF)
									);

									if(	initialisePotenciometr(2,0x10, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(2,0x11, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(2,0x12, 0xFFFF) &&
										initialisePotenciometr(2,0x13, 0xFFFF)
									);

									if(	initialisePotenciometr(7,0x10, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(7,0x11, MAX_POTENTIOMETER_VALUE) &&
										initialisePotenciometr(7,0x12, 0xFFFF) &&
										initialisePotenciometr(7,0x13, 0xFFFF)
									);

									setCalibrationMode(CalibrationMode.OFF);
								}else
									showMessage("Set Calibration Mode 'ON' error.");
							}else
								showMessage("Operation is not completed");
						}
						return null;
					}
				}.execute();
			}

			private boolean initialisePotenciometr(int index, int addr, int potentiometerValue) {
				logger.entry( index, addr, potentiometerValue);
				Value value = new Value( potentiometerValue, 0, MAX_POTENTIOMETER_VALUE, 0);
				RegisterValue registerValue = new RegisterValue(index, addr, value);
				DeviceDebagSetter setter = new DeviceDebagSetter(linkHeader,
						index,
						addr,
						PacketWork.PACKET_ID_DEVICE_POTENTIOMETERS_INIT,
						PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);
				PacketThreadWorker packetThread = setter.getPacketThread();
				packetThread.start();
				try { packetThread.join(); } catch (InterruptedException e) { logger.catching(e); }
				setter.preparePacketToSend(registerValue);
				return runController(createController(setter, index, addr));
			}

			private DefaultController createController(DeviceDebagSetter setter, int index, int address) {
				logger.entry(setter, index, address);
				return new DefaultController(deviceType, "Potenciometer index="+index+", address="+address, setter, Style.CHECK_ONCE){

					@Override
					public void onPacketRecived(Packet packet) {

						if(		getPacketWork().isAddressEquals(packet) &&
								packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE &&
								packet.getHeader().getPacketId()==PacketWork.PACKET_ID_DEVICE_POTENTIOMETERS_INIT){

							BIASsPanel.this.logger.trace("\n\t{}", packet);
							PacketHeader header = packet.getHeader();
							if(header!=null && header.getOption()==0){
								BIASsPanel.this.logger.info("\n\tPacket recived");
								stop();
							} else
								showMessage("Some Problem("+header.getOptionStr()+")");
						}
					}
				};
			}

			private void showMessage(final String message) {
				EventQueue.invokeLater(new Runnable() {
				    @Override
				    public void run() {
				    	try{
				    		JOptionPane.showMessageDialog(null, message);
				    	}catch (Exception e) {
							logger.catching(e);
						}
				    }
				});
			}

			private synchronized boolean setCalibrationMode(final CalibrationMode calibrationMode) {
				BIASsPanel.this.logger.entry(calibrationMode);

				Setter setter = new Setter(
						linkHeader,
						PacketImp.PACKET_TYPE_COMMAND,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE,
						PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE,
						(Integer)calibrationMode.ordinal()
				);
				controller = new DefaultController(deviceType, "CalibrationMode", setter, Style.CHECK_ONCE){

					@Override
					public void onPacketRecived(Packet packet) {

						if(		getPacketWork().isAddressEquals(packet) &&
								packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE &&
								packet.getHeader().getPacketId()==PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE){

							BIASsPanel.this.logger.trace("\n\t{}", packet);
									
							PacketHeader header = packet.getHeader();
							if(header!=null && header.getOption()==0){
								BIASsPanel.this.logger.info("\n\tPacket recived");
								stop();
							}else{
								String optionStr = header.getOptionStr();
								BIASsPanel.this.logger.warn("\n\theader.getOptionStr() = {}", optionStr);
								showMessage("Some Problem("+optionStr+")");
							}
						}
					}
				};
				return runController(controller);
			};

			private static final long TIMEOUT = 1000;
			private boolean runController(DefaultController controller) {
				BIASsPanel.this.logger.entry(controller);

				boolean don;
				if(controller!=null){
					ExecutorService executor = Executors.newFixedThreadPool(1, new MyThreadFactory());
					executor.execute(controller);
					executor.shutdown();

					don = true;
					Long start = System.currentTimeMillis();

					while(!executor.isShutdown() || controller.isRun()){
						try { Thread.sleep(100); } catch (InterruptedException e) { logger.catching(e); }
						if(System.currentTimeMillis()-start>TIMEOUT){
							controller.stop();
							logger.warn("Timeout");
							don = false;
						}
					}
				}else
					don = false;

				return don;
			}
		});

		imageButton.setToolTipText("Initialize");
		imageButton.setName("Initialize");
		imageButton.setBounds(68, 90, 33, 33);
		add(imageButton);
		
		JLabel lblInitialize = new JLabel("Initialize");
		lblInitialize.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblInitialize.setHorizontalAlignment(SwingConstants.CENTER);
		lblInitialize.setBounds(11, 100, 46, 14);
		add(lblInitialize);
		
		lblPotentiometer5 = new JLabel("MMIC:");
		lblPotentiometer5.setRequestFocusEnabled(false);
		lblPotentiometer5.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer5.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPotentiometer5.setBounds(126, 137, 57, 17);
		add(lblPotentiometer5);
			
		lblPotentiometer6 = new JLabel("MMIC:");
		lblPotentiometer6.setRequestFocusEnabled(false);
		lblPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPotentiometer6.setBounds(126, 170, 57, 17);
		add(lblPotentiometer6);
		
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
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtPotentiometer1.saveRegister();
				txtPotentiometer2.saveRegister();
				txtPotentiometer3.saveRegister();
				txtPotentiometer4.saveRegister();
				txtPotentiometer5.saveRegister();
				txtPotentiometer6.saveRegister();
			}
		});
		btnSave.setBounds(140, 234, 97, 23);
		add(btnSave);
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
	public synchronized void onPacketRecived(Packet packet) {
		adcWorkers.parallelStream().forEach(adc->adc.update(packet));
	}

	private int delay;
	@Override
	public synchronized void run() {

		final ComPortThreadQueue queue = GuiControllerAbstract.getComPortThreadQueue();
		final int size = queue.size();

		if(delay<=0)
			try{
				adcWorkers.stream().forEach(adc->queue.add(adc.getPacketToSend()));
			}catch (Exception e) {
				logger.catching(e);
			}
		else
			delay--;

		if(size>ComPortThreadQueue.QUEUE_SIZE_TO_DELAY && delay<=0)
			delay = ComPortThreadQueue.DELAY_TIMES;
		else if(size==0)
			delay = 0;
	}

	private synchronized void start() {

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		if(!service.isShutdown() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
	}

	private void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);

		adcWorkers.parallelStream().forEach(AdcWorker::clear);
		adcWorkers.clear();

		if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate.cancel(true);
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
}
