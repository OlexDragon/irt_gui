package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.setter.DeviceDebagSetter;
import irt.controller.serial_port.value.setter.Setter;
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
import irt.data.packet.PacketWork;
import irt.data.packet.PacketWork.DeviceDebugPacketIds;
import irt.data.packet.denice_debag.CallibrationModePacket;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
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
	private JLabel lblCurrent1_text;
	private JLabel lblCurrent1;
	private JLabel lblCurrent2_text;
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
			final String text = registerTextField.getText().replace(",", "");
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

	public BIASsPanel(final DeviceInfo deviceInfo, final LinkHeader linkHeader, final boolean isMainBoard) {

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
		addAncestorListener(new AncestorListener() {

			public void ancestorAdded(AncestorEvent arg0)  { start(); }
			public void ancestorRemoved(AncestorEvent arg0){ stop();  }
			public void ancestorMoved(AncestorEvent arg0) {}
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

		final RegisterValue registerValue1;
		final RegisterValue registerValue2;
		final RegisterValue registerValue3;
		final RegisterValue registerValue4;
		final RegisterValue registerValue5;
		final RegisterValue registerValue6;
		final RegisterValue registerValue7;
		final RegisterValue registerValue8;

		final boolean hpBais = deviceInfo.getDeviceType().map(dt->dt.HARDWARE_TYPE).filter(ht->ht==HardwareType.HP_BAIS).isPresent();
		if(hpBais) {

			if(isMainBoard) {

				registerValue1 = new RegisterValue(1, 0, null);
				registerValue2 = new RegisterValue(1, 8, null);
				registerValue3 = new RegisterValue(2, 0, null);
				registerValue4 = new RegisterValue(2, 8, null);
				registerValue5 = new RegisterValue(3, 8, null);
				registerValue6 = new RegisterValue(3, 0, null);
				registerValue7 = new RegisterValue(7, 8, null);

			}else{

				registerValue1 = new RegisterValue(4, 0, null);
				registerValue2 = new RegisterValue(4, 8, null);
				registerValue3 = new RegisterValue(5, 0, null);
				registerValue4 = new RegisterValue(5, 8, null);
				registerValue5 = new RegisterValue(6, 8, null);
				registerValue6 = new RegisterValue(6, 0, null);
				registerValue7 = new RegisterValue(7, 0, null);

			}

			// Text Field
			RegisterTextField txtPotentiometer7 = new RegisterTextField(addr, registerValue7, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N7, 0, 896);
			txtPotentiometer7.setHorizontalAlignment(SwingConstants.RIGHT);
			txtPotentiometer7.setFont(new Font("Tahoma", Font.PLAIN, 14));
			txtPotentiometer7.setColumns(10);
			txtPotentiometer7.setBounds(184, P7, 55, 20);
			add(txtPotentiometer7);

			// Label
			JLabel lblPotentiometer7 = new JLabel("MMIC:");
			lblPotentiometer7.setRequestFocusEnabled(false);
			lblPotentiometer7.setHorizontalAlignment(SwingConstants.RIGHT);
			lblPotentiometer7.setFont(font);
			lblPotentiometer7.setBounds(126, P7, 57, 17);
			add(lblPotentiometer7);

			//New Bias board
		}else if(deviceInfo.getRevision()>10) {	

			registerValue1 = new RegisterValue(1, 0, null);
			registerValue2 = new RegisterValue(1, 4, null);
			registerValue3 = new RegisterValue(1, 8, null);
			registerValue4 = new RegisterValue(1, 12,null);
			registerValue5 = new RegisterValue(2, 0, null);
			registerValue6 = new RegisterValue(2, 4, null);
			registerValue7 = new RegisterValue(2, 8, null);
			registerValue8 = new RegisterValue(2, 12,null);

			// Text Fields
			RegisterTextField txtPotentiometer7 = new RegisterTextField(addr, registerValue7, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N7, 0, 896);
			txtPotentiometer7.setHorizontalAlignment(SwingConstants.RIGHT);
			txtPotentiometer7.setFont(new Font("Tahoma", Font.PLAIN, 14));
			txtPotentiometer7.setColumns(10);
			txtPotentiometer7.setBounds(184, P7, 55, 20);
			add(txtPotentiometer7);

			RegisterTextField txtPotentiometer8 = new RegisterTextField(addr, registerValue8, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N8, 0, 896);
			txtPotentiometer8.setHorizontalAlignment(SwingConstants.RIGHT);
			txtPotentiometer8.setFont(new Font("Tahoma", Font.PLAIN, 14));
			txtPotentiometer8.setColumns(10);
			txtPotentiometer8.setBounds(184, P8, 55, 20);
			add(txtPotentiometer8);

			//labels
			JLabel lblPotentiometer7 = new JLabel("POT7:");
			lblPotentiometer7.setRequestFocusEnabled(false);
			lblPotentiometer7.setHorizontalAlignment(SwingConstants.RIGHT);
			lblPotentiometer7.setFont(font);
			lblPotentiometer7.setBounds(126, P7, 57, 17);
			add(lblPotentiometer7);

			JLabel lblPotentiometer8 = new JLabel("POT8:");
			lblPotentiometer8.setRequestFocusEnabled(false);
			lblPotentiometer8.setHorizontalAlignment(SwingConstants.RIGHT);
			lblPotentiometer8.setFont(font);
			lblPotentiometer8.setBounds(126, P8, 57, 17);
			add(lblPotentiometer8);

		}else {

			registerValue1 = new RegisterValue(1, 8, null);
			registerValue2 = new RegisterValue(1, 0, null);
			registerValue3 = new RegisterValue(7, 8, null);
			registerValue4 = new RegisterValue(7, 0, null);
			registerValue5 = new RegisterValue(2, 0, null);
			registerValue6 = new RegisterValue(2, 8, null);
		}

		txtPotentiometer1 = new RegisterTextField(addr, registerValue1, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N1, 0, 896);
		txtPotentiometer1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer1.setBounds(184, P1, 55, 20);
		txtPotentiometer1.setFont(font);
		txtPotentiometer1.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer1);

		txtPotentiometer2 = new RegisterTextField(addr, registerValue2, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N2, 0, 896);
		txtPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer2.setFont(font);
		txtPotentiometer2.setBounds(184, P2, 55, 20);
		txtPotentiometer2.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer2);

		txtPotentiometer3 = new RegisterTextField(addr, registerValue3, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N3, 0, 896);
		txtPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer3.setFont(font);
		txtPotentiometer3.setBounds(184, P3, 55, 20);
		txtPotentiometer3.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer3);

		txtPotentiometer4 = new RegisterTextField(addr, registerValue4, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N4, 0, 896);
		txtPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer4.setFont(font);
		txtPotentiometer4.setBounds(184, P4, 55, 20);
		txtPotentiometer4.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer4);

		txtPotentiometer5 = new RegisterTextField(addr, registerValue5, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N5, 0, 896);
		txtPotentiometer5.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer5.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer5.setColumns(10);
		txtPotentiometer5.setBounds(184, P5, 55, 20);
		txtPotentiometer5.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer5);

		txtPotentiometer6 = new RegisterTextField(addr, registerValue6, PacketIDs.DEVICE_DEBUG_POTENTIOMETER_N6, 0, 896);
		txtPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer6.setColumns(10);
		txtPotentiometer6.setBounds(184, P6, 55, 20);
		txtPotentiometer6.addFocusListener(potentiometerfocusListener);
		add(txtPotentiometer6);

		font = font.deriveFont(12f);

		lblPotentiometer1 = new JLabel("OUTPUT1:");
		lblPotentiometer1.setRequestFocusEnabled(false);
		lblPotentiometer1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer1.setFont(font);
		lblPotentiometer1.setBounds(126, P1, 59, 17);
		add(lblPotentiometer1);

		lblPotentiometer2 = new JLabel("OUTPUT2:");
		lblPotentiometer2.setRequestFocusEnabled(false);
		lblPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer2.setFont(font);
		lblPotentiometer2.setBounds(126, P2, 59, 17);
		add(lblPotentiometer2);

		lblPotentiometer3 = new JLabel("DRIVER:");
		lblPotentiometer3.setRequestFocusEnabled(false);
		lblPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer3.setFont(font);
		lblPotentiometer3.setBounds(126, P3, 57, 17);
		add(lblPotentiometer3);

		lblPotentiometer4 = new JLabel("PRED:");
		lblPotentiometer4.setRequestFocusEnabled(false);
		lblPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer4.setFont(font);
		lblPotentiometer4.setBounds(126, P4, 57, 17);
		add(lblPotentiometer4);

		lblPotentiometer5 = new JLabel("Pot. #5:");
		lblPotentiometer5.setRequestFocusEnabled(false);
		lblPotentiometer5.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer5.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPotentiometer5.setBounds(126, P5, 57, 17);
		add(lblPotentiometer5);

		lblPotentiometer6 = new JLabel("Pot. #6:");
		lblPotentiometer6.setRequestFocusEnabled(false);
		lblPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPotentiometer6.setBounds(126, P6, 57, 17);
		add(lblPotentiometer6);

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
		chckbxStep.setFont(font.deriveFont(11f));
		chckbxStep.setOpaque(false);
		chckbxStep.setBounds(184, 234, 22, 23);
		add(chckbxStep);

		// HP BAIAS Current Measurement
		if(hpBais) {

			txtPotentiometer1.setName(isMainBoard ? "HSS 1.:HS1_:_CURRENT_HP_BIAS" : "HSS 3.:HS3_:_CURRENT_HP_BIAS");	// HSS1.1 or HSS3.1
			txtPotentiometer2.setName(isMainBoard ? "HSS 1.:HS1_:_CURRENT_HP_BIAS" : "HSS 3.:HS3_:_CURRENT_HP_BIAS");	// HSS1.2 or HSS3.2
			txtPotentiometer3.setName(isMainBoard ? "HSS 2.:HS2_:_CURRENT_HP_BIAS" : "HSS 4.:HS4_:_CURRENT_HP_BIAS");	// HSS2.1 or HSS4.1
			txtPotentiometer4.setName(isMainBoard ? "HSS 2.:HS2_:_CURRENT_HP_BIAS" : "HSS 4.:HS4_:_CURRENT_HP_BIAS");	// HSS2.2 or HSS4.2

			FocusListener listener = new FocusListener() {
				
				@Override public void focusGained(FocusEvent e) {
					Component c = (Component) e.getSource();
					final String[] a = c.getName().split(":");

					// Set measurement name
					lblCurrent1_text.setText(a[0] + '1');
					lblCurrent2_text.setText(a[0] + '2');
					lblCurrent1.setText(":");
					lblCurrent2.setText(":");
					
					synchronized(adcWorkers) {

						final List<AdcWorker> toRemove = adcWorkers.parallelStream()

								.filter(
										w->{
											final JLabel label = w.getLabel();
											return label==lblCurrent1 || label==lblCurrent2;
										})
								.collect(Collectors.toList());

						adcWorkers.removeAll(toRemove);
						adcWorkers.add(new AdcWorker(lblCurrent1, addr, null, DeviceDebugPacketIds.valueOf(a[1 ]+ '1' + a[2]), 1, "#.### A"));
						adcWorkers.add(new AdcWorker(lblCurrent2, addr, null, DeviceDebugPacketIds.valueOf(a[1 ]+ '2' + a[2]), 1, "#.### A"));
					}
				}
				@Override public void focusLost(FocusEvent e) { }
			};
			txtPotentiometer1.addFocusListener(listener);
			txtPotentiometer2.addFocusListener(listener);
			txtPotentiometer3.addFocusListener(listener);
			txtPotentiometer4.addFocusListener(listener);
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
		lblCurrent1.setFont(font);
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

		boolean isNewBiasBoard = GuiController.getDeviceInfo(linkHeader).map(di->di.getDeviceType().filter(dt->dt.TYPE_ID<1000).map(dt->true).orElse(false) && di.getRevision()>=2).orElse(true);

		final Optional<DeviceType> hpBias = deviceInfo.getDeviceType().filter(dt->dt.TYPE_ID>=DeviceType.HPB_L_TO_KU.TYPE_ID && dt.TYPE_ID<=DeviceType.KA_SSPA.TYPE_ID);
//		int index = hpBias.map(dt->20).orElse(isMainBoard ? 1 :201);

		double multiplier;
		if(isNewBiasBoard){
			lblPotentiometer1.setText("Output1:");
			lblPotentiometer2.setText("Output2:");
			lblPotentiometer3.setText("Driver1:");
			lblPotentiometer4.setText("Driver2:");
			lblPotentiometer5.setText("Driver:");
			lblPotentiometer6.setText("Pred.Dr");

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
		ImageButton imageButton = new ImageButton(resource!=null ? new ImageIcon(resource).getImage() : null);
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.addMouseListener(new MouseAdapter() {
			DefaultController controller;

			@Override
			public void mouseClicked(MouseEvent arg0) {
				SwingUtilities.invokeLater(()->{

					if(isMainBoard) {

						final Optional<Boolean> oCallMode = checkCallibrationMode(addr);
						if(!oCallMode.isPresent()) {
							showMessage("Packet does not have value.");
							return;
						}

						final Boolean callMode = oCallMode.get();
						if(!callMode) {
							if(!setCallibrationModeOn(addr).filter(b->b).orElse(false)) {
								showMessage("Unable to set calibration mode on.");
								return;
							}
						}

						final boolean initialize = initialize(addr).orElse(false);

						if(initialize)
							showMessage("Initialization complete.\nReboot the unit.");
						else
							showMessage("Unable to initialize.");

					}else{
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
				});
			}

			private Optional<Boolean> initialize(byte addr) {
				final PacketWork initializePacket = new InitializePacket(addr);
				return sendPacket(initializePacket, InitializePacket.parseValueFunction).filter(Boolean.class::isInstance).map(Boolean.class::cast);
			}

			private Optional<Boolean> checkCallibrationMode(byte addr) {
				final PacketWork callibrationModePacket = new CallibrationModePacket(addr);
				return sendPacket(callibrationModePacket, CallibrationModePacket.parseValueFunction).filter(Boolean.class::isInstance).map(Boolean.class::cast);
			}

			private Optional<Boolean> setCallibrationModeOn(byte addr) {
				final PacketWork callibrationModePacket = new CallibrationModePacket(addr, true);
				return sendPacket(callibrationModePacket, CallibrationModePacket.parseValueFunction).filter(Boolean.class::isInstance).map(Boolean.class::cast);
			}

			private Optional<?> sendPacket(PacketWork packetToSend, Function<Packet, Optional<Object>> function) {

				final ParsePacket parsePacket = new ParsePacket(function);
				FutureTask<Optional<?>> ft = new FutureTask<>(parsePacket);

				final PacketListener packetListener = new PacketListener() {
					
					@Override
					public void onPacketReceived(Packet packet) {

						if(packet==null)
							return;

						final Optional<PacketIDs> oPacketId = PacketIDs.valueOf(packet.getHeader().getPacketId());
						if(!oPacketId.isPresent())
							return;

						final PacketIDs packetID = oPacketId.get();
						if(!packetID.match(packet))
							return;

						parsePacket.setPacket(packet);
						new ThreadWorker(ft, "Get Callibration Mode");
					}
				};

				final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

				comPortThreadQueue.addPacketListener(packetListener);

				comPortThreadQueue.add(packetToSend);
				Optional<?> result;
				try {

					result = ft.get(3, TimeUnit.SECONDS);

				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					logger.catching(e);
					result = Optional.empty();
				}
				comPortThreadQueue.removePacketListener(packetListener);

				return result;
			}

			private boolean initialisePotenciometr(int index, int addr, int potentiometerValue) {
				Value value = new Value( potentiometerValue, 0, MAX_POTENTIOMETER_VALUE, 0);
				RegisterValue registerValue = new RegisterValue(index, addr, value);
				DeviceDebagSetter setter = new DeviceDebagSetter(linkHeader,
						index,
						addr,
						PacketIDs.DEVICE_POTENTIOMETERS_INIT,
						PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE);
				PacketThreadWorker packetThread = setter.getPacketThread();
				packetThread.start();
				try { packetThread.join(); } catch (InterruptedException e) { logger.catching(e); }
				setter.preparePacketToSend(registerValue);
				return runController(createController(setter, index, addr));
			}

			private DefaultController createController(DeviceDebagSetter setter, int index, int address) {
				return new DefaultController(deviceInfo.getDeviceType(), "Potenciometer index="+index+", address="+address, setter, Style.CHECK_ONCE){

					@Override
					public void onPacketReceived(Packet packet) {

						new ThreadWorker(()->{

							if(		getPacketWork().isAddressEquals(packet) &&
									packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE &&
									PacketIDs.DEVICE_POTENTIOMETERS_INIT.match(packet.getHeader().getPacketId())){

								BIASsPanel.this.logger.trace("\n\t{}", packet);
								PacketHeader header = packet.getHeader();
								if(header!=null && header.getOption()==0){
									BIASsPanel.this.logger.info("\n\tPacket recived");
									stop();
								} else
									showMessage("Some Problem("+header.getOptionStr()+")");
							}
						}, "BIASsPanel.DefaultController.onPacketReceived()");
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

				Setter setter = new Setter(
						linkHeader,
						PacketImp.PACKET_TYPE_COMMAND,
						PacketGroupIDs.DEVICE_DEBUG.getId(),
						PacketImp.PARAMETER_DEVICE_DEBUG_CALIBRATION_MODE,
						PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE,
						(Integer)calibrationMode.ordinal()
				);
				controller = new DefaultController(deviceInfo.getDeviceType(), "CalibrationMode", setter, Style.CHECK_ONCE){

					@Override
					public void onPacketReceived(Packet packet) {

						new ThreadWorker(()->{

							if(		getPacketWork().isAddressEquals(packet) &&
									packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE &&
									PacketIDs.DEVICE_DEBUG_CALIBRATION_MODE.match(packet.getHeader().getPacketId())){

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
						}, "BIASsPanel.DefaultController-2.onPacketReceived()");
					}
				};
				return runController(controller);
			};

			private static final long TIMEOUT = 1000;
			private boolean runController(DefaultController controller) {

				boolean don;
				if(controller!=null){
					ExecutorService executor = Executors.newFixedThreadPool(1, new ThreadWorker("BIASsPanel.runController(DefaultController)"));
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
		btnSave.setBounds(140, 234, 34, 23);
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
	@Override
	public void run() {

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
			delay = 0;
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

	public class ParsePacket implements Callable<Optional<?>>{

		private Function<Packet, Optional<Object>> function;
		private Packet packet;

		public ParsePacket(Function<Packet, Optional<Object>> function) {
			this.function = function;
		}
		public Packet getPacket() {
			return packet;
		}
		public void setPacket(Packet packet) {
			this.packet = packet;
		}

		@Override
		public Optional<?> call() throws Exception {
			if(function==null || packet==null)
				return Optional.empty();
			return function.apply(packet);
		}
	}
}
