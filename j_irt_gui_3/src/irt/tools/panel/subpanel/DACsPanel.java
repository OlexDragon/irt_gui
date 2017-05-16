package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import irt.controller.GuiControllerAbstract;
import irt.controller.SwitchController;
import irt.controller.TextSliderController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.setter.ConfigurationSetter;
import irt.controller.serial_port.value.setter.Setter;
import irt.data.MyThreadFactory;
import irt.data.RegisterValue;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.RegisterTextField;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.panel.subpanel.BIASsPanel.AdcWorker;

@SuppressWarnings("serial")
public class DACsPanel extends JPanel implements PacketListener, Runnable {

	private RegisterTextField txtDAC1;
	private RegisterTextField txtDAC2;
	private RegisterTextField txtDAC3;
	private RegisterTextField txtDAC4;
	private JSlider slider;
	private JLabel lblDAC1;
	private JLabel lblDAC2;
	private JLabel lblDAC3;
	private JLabel lblDAC4;

	private RegisterTextField activeTextField;
	private SwitchBox switchBoxCalibrationModeswitchBox;
	private JTextField txtStep;
	private JCheckBox chckbxStep;

	private static ComponentAdapter componentAdapter;
	private JLabel label;
	private JLabel lblInputPower;
	private JLabel lblOutputPower;
	private JLabel label_3;
	private JLabel lblTemperature;
	private JLabel lblTm;
	private JLabel lblCurrent;
	private JLabel lblCu;
	private JLabel lbl5V5;
	private JLabel label_9;
	private JLabel lbl13V2;
	private JLabel label_11;
	private JLabel lbl13V2_neg;
	private JLabel label_13;
	private JTextField txtGainOffset;
	private JSlider sliderGainOffset;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private final List<AdcWorker> adcWorkers = new ArrayList<>();

	private final FocusListener dacfocusListener = new FocusListener() {
		@Override public void focusGained(FocusEvent e) {
			final RegisterTextField source = (RegisterTextField) e.getSource();
			setColors(source);
			setSliderValue(source);
		}
		private void setSliderValue(RegisterTextField registerTextField) {
			final String text = registerTextField.getText();
			if(text.isEmpty())
				return;

			slider.setValue(Integer.parseInt(text.replaceAll("\\D", "")));
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

	public DACsPanel(final int deviceType, final LinkHeader linkHeader) {
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null)
					service.shutdownNow();
			}
		});

		setLayout(null);
		final byte unitAddr = linkHeader==null ? 0 :  linkHeader.getAddr();
		addAncestorListener(new AncestorListener() {
			private List<ControllerAbstract> threadList = new ArrayList<>();

			public void ancestorAdded(AncestorEvent arg0) {

				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(DACsPanel.this);

				if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
					scheduleAtFixedRate = service.scheduleAtFixedRate(DACsPanel.this, 0, 1, TimeUnit.SECONDS);

				txtDAC1.start();
				txtDAC2.start();
				txtDAC3.start();
				txtDAC4.start();

				//Calibration mode
				startController(new SwitchController(deviceType, "Calibration Mode Switch Controller", switchBoxCalibrationModeswitchBox, new Setter(linkHeader, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE, PacketWork.PACKET_ID_DEVICE_DEBUG_CALIBRATION_MODE)));

				if(unitAddr==0){
					adcWorkers.add(new BIASsPanel.AdcWorker(lblInputPower, 	MonitorPanelFx.CONVERTER, new RegisterValue(10, 0, null), PacketWork.PACKET_ID_FCM_ADC_INPUT_POWER, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lblOutputPower, MonitorPanelFx.CONVERTER, new RegisterValue(10, 1, null), PacketWork.PACKET_ID_FCM_ADC_OUTPUT_POWER, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lblTemperature, MonitorPanelFx.CONVERTER, new RegisterValue(10, 2, null), PacketWork.PACKET_ID_FCM_ADC_TEMPERATURE, PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lblCurrent, 	MonitorPanelFx.CONVERTER, new RegisterValue(10, 4, null), PacketWork.PACKET_ID_FCM_ADC_CURRENT, 	PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lbl5V5, 		MonitorPanelFx.CONVERTER, new RegisterValue(10, 6, null), PacketWork.PACKET_ID_FCM_ADC_5V5, 		PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lbl13V2, 		MonitorPanelFx.CONVERTER, new RegisterValue(10, 7, null), PacketWork.PACKET_ID_FCM_ADC_13v2, 		PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));
					adcWorkers.add(new BIASsPanel.AdcWorker(lbl13V2_neg, 	MonitorPanelFx.CONVERTER, new RegisterValue(10, 8, null), PacketWork.PACKET_ID_FCM_ADC_13V2_NEG, 	PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE, 1, "#.###"));

					Value value = new Value(0, -100, 100, 0);
					startController(new TextSliderController(deviceType, "Gain Offset Controller", new ConfigurationSetter(null, PacketImp.PARAMETER_CONFIG_FCM_GAIN_OFFSET, PacketWork.PACKET_ID_CONFIGURATION_GAIN_OFFSET), value, txtGainOffset, sliderGainOffset, Style.CHECK_ONCE));
				}

			}

			public void startController(ControllerAbstract abstractController) {
				threadList.add(abstractController);

				Thread t = new Thread(abstractController, "DACsPanel."+abstractController.getName()+"-"+new RundomNumber());
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
			public void ancestorMoved(AncestorEvent arg0) {
			}
			public void ancestorRemoved(AncestorEvent arg0) {

				txtDAC1.stop();
				txtDAC2.stop();
				txtDAC3.stop();
				txtDAC4.stop();

				for(ControllerAbstract t:threadList){
					t.stop();
				}
				threadList.clear();

				//---------------------------------------------------------------------------------
				adcWorkers.clear();

				GuiControllerAbstract.getComPortThreadQueue().removePacketListener(DACsPanel.this);

				if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
					scheduleAtFixedRate.cancel(true);
			}
		});
		componentAdapter = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				slider.setBounds(getWidth()-22, 3, 22, getHeight()-6);
				txtDAC1.setLocation(getWidth()-85, txtDAC1.getY());
				txtDAC2.setLocation(getWidth()-85, txtDAC2.getY());
				txtDAC3.setLocation(getWidth()-85, txtDAC3.getY());
				txtDAC4.setLocation(getWidth()-85, txtDAC4.getY());
				lblDAC1.setLocation(getWidth()-160, txtDAC1.getY());
				lblDAC2.setLocation(getWidth()-160, txtDAC2.getY());
				lblDAC3.setLocation(getWidth()-160, txtDAC3.getY());
				lblDAC4.setLocation(getWidth()-160, txtDAC4.getY());
			}
		};
		addComponentListener(componentAdapter);
		setLayout(null);

		Font font = new Font("Tahoma", Font.PLAIN, 14);

		slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(4095);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setOpaque(false);
		slider.setBounds(251, 8, 22, 260);
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

		int index;
		int rAddr;
		if(unitAddr==0){
			index = 1;
			rAddr = 0;
		}else{
			index = 100;
			rAddr = 1;
		}
		RegisterValue registerValue = new RegisterValue(index, rAddr, null);
		txtDAC1 = new RegisterTextField(unitAddr, registerValue, PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC1, 0, 4095);
		txtDAC1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC1.setColumns(10);
		txtDAC1.setBounds(186, 16, 55, 20);
		txtDAC1.addFocusListener(dacfocusListener);
		add(txtDAC1);
		txtDAC1.setFont(font);

		if(unitAddr==0)
			index++;
		else
			rAddr++;

		registerValue = new RegisterValue(index, rAddr, null);
		txtDAC2 = new RegisterTextField(unitAddr, registerValue, PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC2, 0, 4095);
		txtDAC2.setEnabled(false);
		txtDAC2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC2.setColumns(10);
		txtDAC2.setBounds(186, 44, 55, 20);
		txtDAC2.addFocusListener(dacfocusListener);
		add(txtDAC2);
		txtDAC2.setFont(font);

		if(unitAddr==0)
			index++;
		else
			rAddr++;

		registerValue = new RegisterValue(index, rAddr, null);
		txtDAC3 = new RegisterTextField(unitAddr, registerValue, PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC3, 0, 4095);
		txtDAC3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC3.setColumns(10);
		txtDAC3.setBounds(186, 72, 55, 20);
		txtDAC3.addFocusListener(dacfocusListener);
		add(txtDAC3);
		txtDAC3.setFont(font);

		if(unitAddr==0)
			index++;
		else
			rAddr++;

		registerValue = new RegisterValue(index, rAddr, null);

		txtDAC4 = new RegisterTextField(unitAddr, registerValue, PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC4, 0, 4095);
		txtDAC4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC4.setColumns(10);
		txtDAC4.setBounds(187, 100, 55, 20);
		txtDAC4.addFocusListener(dacfocusListener);
		add(txtDAC4);
		txtDAC4.setFont(font);

		font = font.deriveFont(12f);

		lblDAC4 = new JLabel("DAC 4:");
		lblDAC4.setRequestFocusEnabled(false);
		lblDAC4.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC4.setBounds(112, 102, 73, 17);
		add(lblDAC4);
		lblDAC4.setFont(font);

		lblDAC1 = new JLabel("Gain DAC:");
		lblDAC1.setRequestFocusEnabled(false);
		lblDAC1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC1.setBounds(112, 18, 73, 17);
		add(lblDAC1);
		lblDAC1.setFont(font);

		lblDAC2 = new JLabel("Comp DAC:");
		lblDAC2.setRequestFocusEnabled(false);
		lblDAC2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC2.setBounds(112, 46, 73, 17);
		add(lblDAC2);
		lblDAC2.setFont(font);

		lblDAC3 = new JLabel("DAC 3:");
		lblDAC3.setRequestFocusEnabled(false);
		lblDAC3.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC3.setBounds(112, 74, 73, 17);
		add(lblDAC3);
		lblDAC3.setFont(font);

		URL resource = IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png");
		Image offImage = resource!=null ? new ImageIcon(resource).getImage() : null;
		resource = IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png");
		Image onImage = resource!=null ? new ImageIcon(resource).getImage() : null;
		switchBoxCalibrationModeswitchBox = new SwitchBox(offImage, onImage);
		switchBoxCalibrationModeswitchBox.setEnabled(false);
		switchBoxCalibrationModeswitchBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				boolean selected = switchBoxCalibrationModeswitchBox.isSelected();
				if(txtDAC2.isFocusOwner())
					txtDAC1.requestFocusInWindow();
				txtDAC2.setEnabled(selected);
			}
		});
		switchBoxCalibrationModeswitchBox.setBounds(6, 26, 107, 44);
		add(switchBoxCalibrationModeswitchBox);

		JLabel lblCalibrationMode = new JLabel("Calibration Mode");
		lblCalibrationMode.setRequestFocusEnabled(false);
		lblCalibrationMode.setBounds(6, 8, 107, 17);
		add(lblCalibrationMode);
		lblCalibrationMode.setFont(font);
		
		chckbxStep = new JCheckBox("Step:");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setSnapToTicks(chckbxStep.isSelected());
				slider.requestFocusInWindow();
			}
		});
		chckbxStep.setOpaque(false);
		chckbxStep.setBounds(6, 80, 59, 23);
		add(chckbxStep);
		chckbxStep.setFont(font.deriveFont(12f));
		
		txtStep = new JTextField();
		txtStep.setText("1");
		txtStep.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStep.setColumns(10);
		txtStep.setBounds(59, 81, 34, 20);
		txtStep.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				slider.setMinorTickSpacing(getStep());
			}
		});
		txtStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				slider.setMinorTickSpacing(getStep());
				slider.requestFocusInWindow();
			}
		});
		add(txtStep);
		txtStep.setFont(font);

		if(true){//linkHeader==null){
			
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "ADCs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setBounds(13, 156, 227, 108);
			add(panel);
			panel.setLayout(null);
			
			label = new JLabel("IP:");
			label.setToolTipText("Input Power");
			label.setRequestFocusEnabled(false);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			label.setForeground(Color.BLACK);
			label.setBounds(9, 19, 23, 17);
			panel.add(label);
			label.setFont(font);
			
			lblInputPower = new JLabel(":");
			lblInputPower.setRequestFocusEnabled(false);
			lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
			lblInputPower.setForeground(Color.BLUE);
			lblInputPower.setBounds(31, 19, 60, 17);
			panel.add(lblInputPower);
			lblInputPower.setFont(font);
			
			lblOutputPower = new JLabel(":");
			lblOutputPower.setRequestFocusEnabled(false);
			lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOutputPower.setForeground(Color.BLUE);
			lblOutputPower.setBounds(31, 42, 60, 17);
			panel.add(lblOutputPower);
			lblOutputPower.setFont(font);
			
			label_3 = new JLabel("OP:");
			label_3.setToolTipText("Output Power");
			label_3.setRequestFocusEnabled(false);
			label_3.setHorizontalAlignment(SwingConstants.RIGHT);
			label_3.setForeground(Color.BLACK);
			label_3.setBounds(9, 42, 23, 17);
			panel.add(label_3);
			label_3.setFont(font);
			
			lblTemperature = new JLabel(":");
			lblTemperature.setRequestFocusEnabled(false);
			lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTemperature.setForeground(Color.BLUE);
			lblTemperature.setBounds(31, 65, 60, 17);
			panel.add(lblTemperature);
			lblTemperature.setFont(font);
			
			lblTm = new JLabel("T:");
			lblTm.setToolTipText("Temperature");
			lblTm.setRequestFocusEnabled(false);
			lblTm.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTm.setForeground(Color.BLACK);
			lblTm.setBounds(9, 65, 23, 17);
			panel.add(lblTm);
			lblTm.setFont(font);
			
			lblCurrent = new JLabel(":");
			lblCurrent.setRequestFocusEnabled(false);
			lblCurrent.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCurrent.setForeground(Color.BLUE);
			lblCurrent.setBounds(31, 88, 60, 17);
			panel.add(lblCurrent);
			lblCurrent.setFont(font);
			
			lblCu = new JLabel("Cu:");
			lblCu.setToolTipText("Current");
			lblCu.setRequestFocusEnabled(false);
			lblCu.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCu.setForeground(Color.BLACK);
			lblCu.setBounds(9, 88, 23, 17);
			panel.add(lblCu);
			lblCu.setFont(font);
			
			lbl5V5 = new JLabel(":");
			lbl5V5.setRequestFocusEnabled(false);
			lbl5V5.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl5V5.setForeground(Color.BLUE);
			lbl5V5.setBounds(156, 21, 60, 17);
			panel.add(lbl5V5);
			lbl5V5.setFont(font);
			
			label_9 = new JLabel("5.5 V:");
			label_9.setRequestFocusEnabled(false);
			label_9.setHorizontalAlignment(SwingConstants.RIGHT);
			label_9.setForeground(Color.BLACK);
			label_9.setBounds(106, 21, 50, 17);
			panel.add(label_9);
			label_9.setFont(font);
			
			lbl13V2 = new JLabel(":");
			lbl13V2.setRequestFocusEnabled(false);
			lbl13V2.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl13V2.setForeground(Color.BLUE);
			lbl13V2.setBounds(156, 48, 60, 17);
			panel.add(lbl13V2);
			lbl13V2.setFont(font);
			
			label_11 = new JLabel("13.2 V:");
			label_11.setRequestFocusEnabled(false);
			label_11.setHorizontalAlignment(SwingConstants.RIGHT);
			label_11.setForeground(Color.BLACK);
			label_11.setBounds(106, 48, 50, 17);
			panel.add(label_11);
			label_11.setFont(font);
			
			lbl13V2_neg = new JLabel(":");
			lbl13V2_neg.setRequestFocusEnabled(false);
			lbl13V2_neg.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl13V2_neg.setForeground(Color.BLUE);
			lbl13V2_neg.setBounds(155, 75, 60, 17);
			panel.add(lbl13V2_neg);
			lbl13V2_neg.setFont(font);
			
			label_13 = new JLabel("-13.2 V:");
			label_13.setRequestFocusEnabled(false);
			label_13.setHorizontalAlignment(SwingConstants.RIGHT);
			label_13.setForeground(Color.BLACK);
			label_13.setBounds(105, 75, 50, 17);
			panel.add(label_13);
			label_13.setFont(font);
			
			JSeparator separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
			separator.setForeground(Color.CYAN);
			separator.setBackground(Color.CYAN);
			separator.setBounds(100, 9, 1, 109);
			panel.add(separator);
			
			sliderGainOffset = new JSlider();
			sliderGainOffset.setBounds(6, 139, 235, 17);
			add(sliderGainOffset);
			
			JLabel lblGainOffset = new JLabel("Gain Offset:");
			lblGainOffset.setRequestFocusEnabled(false);
			lblGainOffset.setHorizontalAlignment(SwingConstants.RIGHT);
			lblGainOffset.setBounds(8, 119, 72, 15);
			add(lblGainOffset);
			lblGainOffset.setFont(font.deriveFont(12f));
			
			txtGainOffset = new JTextField();
			txtGainOffset.setText("0");
			txtGainOffset.setHorizontalAlignment(SwingConstants.RIGHT);
			txtGainOffset.setColumns(10);
			txtGainOffset.setBounds(79, 116, 42, 20);
			add(txtGainOffset);
			txtGainOffset.setFont(font);
		}
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
	public void onPacketRecived(Packet packet) {
		adcWorkers.parallelStream().forEach(adc->adc.update(packet));
	}

	@Override
	public void run() {
		adcWorkers.stream().forEach(adc->GuiControllerAbstract.getComPortThreadQueue().add(adc.getPacketToSend()));
	}
}
