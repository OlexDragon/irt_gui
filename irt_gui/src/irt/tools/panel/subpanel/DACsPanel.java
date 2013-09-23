package irt.tools.panel.subpanel;

import irt.controller.AdcController;
import irt.controller.DeviceDebagController;
import irt.controller.SwitchController;
import irt.controller.TextSliderController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.Getter.DeviceDebagGetter;
import irt.controller.serial_port.value.seter.ConfigurationSetter;
import irt.controller.serial_port.value.seter.DeviceDebagSetter;
import irt.controller.serial_port.value.seter.Setter;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ControllerFocusListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

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

@SuppressWarnings("serial")
public class DACsPanel extends JPanel {
	private JTextField txtDAC1;
	private JTextField txtDAC2;
	private JTextField txtDAC3;
	private JTextField txtDAC4;
	private JSlider slider;
	private JLabel lblDAC1;
	private JLabel lblDAC2;
	private JLabel lblDAC3;
	private JLabel lblDAC4;

	private JTextField activeTextField;
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

	public DACsPanel(final LinkHeader linkHeader) {
		setLayout(null);
		addAncestorListener(new AncestorListener() {
			private List<ControllerAbstract> threadList = new ArrayList<>();

			private ControllerFocusListener controllerFocusListener = new ControllerFocusListener() {

				@Override
				public void focusGained(ValueChangeEvent valueChangeEvent) {
					if(activeTextField!=null)
						activeTextField.setBackground(Color.WHITE);

					activeTextField = (JTextField) valueChangeEvent.getSource();
					activeTextField.setBackground(Color.YELLOW);
				}
			};

			public void ancestorAdded(AncestorEvent arg0) {

				ControllerAbstract abstractController = linkHeader==null ?
						new DeviceDebagController(txtDAC1, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(null, 1, 0, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC1, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS) :
							new DeviceDebagController(txtDAC1, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(linkHeader, 100, 1, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC1, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS);
				Thread t = new Thread(abstractController, "DAC 1");
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();
				threadList.add(abstractController);
				((DeviceDebagController)abstractController).addFocusListener(controllerFocusListener);

				abstractController = linkHeader==null ?
						new DeviceDebagController(txtDAC2, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(null, 2, 0, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC2, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS) :
							new DeviceDebagController(txtDAC2, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(linkHeader, 100, 2, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC2, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS);
				t = new Thread(abstractController, "DAC 2");
				priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();
				threadList.add(abstractController);
				((DeviceDebagController)abstractController).addFocusListener(controllerFocusListener);

				abstractController = linkHeader==null ?
						new DeviceDebagController(txtDAC3, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(null, 3, 0, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC3, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS) :
							new DeviceDebagController(txtDAC3, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(linkHeader, 100, 3, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC3, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS);
				t = new Thread(abstractController, "DAC 3");
				priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();
				threadList.add(abstractController);
				((DeviceDebagController)abstractController).addFocusListener(controllerFocusListener);

				abstractController = linkHeader==null ?
						new DeviceDebagController(txtDAC4, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(null, 4, 0, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC4, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS) :
							new DeviceDebagController(txtDAC4, slider, new Value(0, 0, 4095, 0), new DeviceDebagSetter(linkHeader, 100, 4, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_CONVERTER_DAC4, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), -1, Style.CHECK_ALWAYS);
				t = new Thread(abstractController, "DAC 4");
				priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();
				threadList.add(abstractController);
				((DeviceDebagController)abstractController).addFocusListener(controllerFocusListener);

				//Calibration mode
				abstractController =  new SwitchController(switchBoxCalibrationModeswitchBox, new Setter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_CALIBRATION_MODE, PacketWork.PACKET_BIAS_DEVICE_DEBAG_CALIBRATION_MODE));
				t = new Thread(abstractController, "Calibration Mode");
				priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();
				threadList.add(abstractController);

				if(linkHeader==null){
					Value value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lblInputPower, new DeviceDebagGetter(null,  10, 0, PacketWork.PACKET_ID_FCM_ADC_INPUT_POWER, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					t = new Thread(abstractController, "ADC Input Power");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lblOutputPower, new DeviceDebagGetter(null,  10, 1, PacketWork.PACKET_ID_FCM_ADC_OUTPUT_POWER, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					t = new Thread(abstractController, "ADC Output Power");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lblTemperature, new DeviceDebagGetter(null,  10, 2, PacketWork.PACKET_ID_FCM_ADC_TEMPERATURE, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					abstractController.setWaitTime(10000);
					t = new Thread(abstractController, "ADC Tempetature");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lblCurrent, new DeviceDebagGetter(null,  10, 4, PacketWork.PACKET_ID_FCM_ADC_CURRENT, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					t = new Thread(abstractController, "ADC Current");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lbl5V5, new DeviceDebagGetter(null,  10, 6, PacketWork.PACKET_ID_FCM_ADC_5V5, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					abstractController.setWaitTime(10000);
					t = new Thread(abstractController, "ADC 5.5 V");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lbl13V2, new DeviceDebagGetter(null,  10, 7, PacketWork.PACKET_ID_FCM_ADC_13v2, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					abstractController.setWaitTime(10000);
					t = new Thread(abstractController, "ADC 13.2 V");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, 0, 4095, 0);
					abstractController = new AdcController(lbl13V2_neg, new DeviceDebagGetter(null,  10, 8, PacketWork.PACKET_ID_FCM_ADC_13V2_NEG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value);
					abstractController.setWaitTime(10000);
					t = new Thread(abstractController, "ADC -13.2 V");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);

					value = new Value(0, -100, 100, 0);
					abstractController = new TextSliderController(new ConfigurationSetter(null, Packet.IRT_SLCP_PARAMETER_FCM_CONFIG_GAIN_OFFSET, PacketWork.PACKET_ID_CONFIGURATION__GAIN_OFFSET), value, txtGainOffset, sliderGainOffset, Style.CHECK_ONCE);
					abstractController.setWaitTime(10000);
					t = new Thread(abstractController, "Gain Offset");
					priority = t.getPriority();
					if(priority>Thread.MIN_PRIORITY)
						t.setPriority(priority-1);
					t.start();
					threadList.add(abstractController);
				}

			}
			public void ancestorMoved(AncestorEvent arg0) {
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				for(ControllerAbstract t:threadList){
					t.setRun(false);
					if(t instanceof DeviceDebagController)
						((DeviceDebagController)t).removeFocusListener(controllerFocusListener);
				}
				threadList.clear();
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

		slider = new JSlider();
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setOpaque(false);
		slider.setBounds(251, 8, 22, 260);
		add(slider);

		txtDAC1 = new JTextField();
		txtDAC1.setText("0");
		txtDAC1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtDAC1.setColumns(10);
		txtDAC1.setBounds(186, 16, 55, 20);
		add(txtDAC1);

		txtDAC2 = new JTextField();
		txtDAC2.setEnabled(false);
		txtDAC2.setText("0");
		txtDAC2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC2.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtDAC2.setColumns(10);
		txtDAC2.setBounds(186, 44, 55, 20);
		add(txtDAC2);

		lblDAC1 = new JLabel("Gain DAC:");
		lblDAC1.setRequestFocusEnabled(false);
		lblDAC1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDAC1.setBounds(112, 18, 73, 17);
		add(lblDAC1);

		lblDAC2 = new JLabel("Comp DAC:");
		lblDAC2.setRequestFocusEnabled(false);
		lblDAC2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC2.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDAC2.setBounds(112, 46, 73, 17);
		add(lblDAC2);

		lblDAC3 = new JLabel("DAC 3:");
		lblDAC3.setRequestFocusEnabled(false);
		lblDAC3.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC3.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDAC3.setBounds(112, 74, 73, 17);
		add(lblDAC3);

		txtDAC3 = new JTextField();
		txtDAC3.setText("0");
		txtDAC3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC3.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtDAC3.setColumns(10);
		txtDAC3.setBounds(186, 72, 55, 20);
		add(txtDAC3);

		lblDAC4 = new JLabel("DAC 4:");
		lblDAC4.setRequestFocusEnabled(false);
		lblDAC4.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDAC4.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDAC4.setBounds(112, 102, 73, 17);
		add(lblDAC4);

		txtDAC4 = new JTextField();
		txtDAC4.setText("0");
		txtDAC4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtDAC4.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtDAC4.setColumns(10);
		txtDAC4.setBounds(187, 100, 55, 20);
		add(txtDAC4);
		
		Image offImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png")).getImage();
		Image onImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png")).getImage();
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
		lblCalibrationMode.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCalibrationMode.setBounds(6, 8, 100, 17);
		add(lblCalibrationMode);
		
		chckbxStep = new JCheckBox("Step:");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setSnapToTicks(chckbxStep.isSelected());
				slider.requestFocusInWindow();
			}
		});
		chckbxStep.setOpaque(false);
		chckbxStep.setFont(new Font("Tahoma", Font.PLAIN, 12));
		chckbxStep.setBounds(6, 80, 55, 23);
		add(chckbxStep);
		
		txtStep = new JTextField();
		txtStep.setText("1");
		txtStep.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStep.setFont(new Font("Tahoma", Font.PLAIN, 16));
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

		if(linkHeader==null){
			
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
			label.setFont(new Font("Tahoma", Font.PLAIN, 14));
			label.setBounds(9, 19, 23, 17);
			panel.add(label);
			
			lblInputPower = new JLabel(":");
			lblInputPower.setRequestFocusEnabled(false);
			lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
			lblInputPower.setForeground(Color.BLUE);
			lblInputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblInputPower.setBounds(31, 19, 60, 17);
			panel.add(lblInputPower);
			
			lblOutputPower = new JLabel(":");
			lblOutputPower.setRequestFocusEnabled(false);
			lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOutputPower.setForeground(Color.BLUE);
			lblOutputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblOutputPower.setBounds(31, 42, 60, 17);
			panel.add(lblOutputPower);
			
			label_3 = new JLabel("OP:");
			label_3.setToolTipText("Output Power");
			label_3.setRequestFocusEnabled(false);
			label_3.setHorizontalAlignment(SwingConstants.RIGHT);
			label_3.setForeground(Color.BLACK);
			label_3.setFont(new Font("Tahoma", Font.PLAIN, 14));
			label_3.setBounds(9, 42, 23, 17);
			panel.add(label_3);
			
			lblTemperature = new JLabel(":");
			lblTemperature.setRequestFocusEnabled(false);
			lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTemperature.setForeground(Color.BLUE);
			lblTemperature.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblTemperature.setBounds(31, 65, 60, 17);
			panel.add(lblTemperature);
			
			lblTm = new JLabel("T:");
			lblTm.setToolTipText("Temperature");
			lblTm.setRequestFocusEnabled(false);
			lblTm.setHorizontalAlignment(SwingConstants.RIGHT);
			lblTm.setForeground(Color.BLACK);
			lblTm.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblTm.setBounds(9, 65, 23, 17);
			panel.add(lblTm);
			
			lblCurrent = new JLabel(":");
			lblCurrent.setRequestFocusEnabled(false);
			lblCurrent.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCurrent.setForeground(Color.BLUE);
			lblCurrent.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblCurrent.setBounds(31, 88, 60, 17);
			panel.add(lblCurrent);
			
			lblCu = new JLabel("Cu:");
			lblCu.setToolTipText("Current");
			lblCu.setRequestFocusEnabled(false);
			lblCu.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCu.setForeground(Color.BLACK);
			lblCu.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lblCu.setBounds(9, 88, 23, 17);
			panel.add(lblCu);
			
			lbl5V5 = new JLabel(":");
			lbl5V5.setRequestFocusEnabled(false);
			lbl5V5.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl5V5.setForeground(Color.BLUE);
			lbl5V5.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lbl5V5.setBounds(156, 21, 60, 17);
			panel.add(lbl5V5);
			
			label_9 = new JLabel("5.5 V:");
			label_9.setRequestFocusEnabled(false);
			label_9.setHorizontalAlignment(SwingConstants.RIGHT);
			label_9.setForeground(Color.BLACK);
			label_9.setFont(new Font("Tahoma", Font.PLAIN, 14));
			label_9.setBounds(106, 21, 50, 17);
			panel.add(label_9);
			
			lbl13V2 = new JLabel(":");
			lbl13V2.setRequestFocusEnabled(false);
			lbl13V2.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl13V2.setForeground(Color.BLUE);
			lbl13V2.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lbl13V2.setBounds(156, 48, 60, 17);
			panel.add(lbl13V2);
			
			label_11 = new JLabel("13.2 V:");
			label_11.setRequestFocusEnabled(false);
			label_11.setHorizontalAlignment(SwingConstants.RIGHT);
			label_11.setForeground(Color.BLACK);
			label_11.setFont(new Font("Tahoma", Font.PLAIN, 14));
			label_11.setBounds(106, 48, 50, 17);
			panel.add(label_11);
			
			lbl13V2_neg = new JLabel(":");
			lbl13V2_neg.setRequestFocusEnabled(false);
			lbl13V2_neg.setHorizontalAlignment(SwingConstants.RIGHT);
			lbl13V2_neg.setForeground(Color.BLUE);
			lbl13V2_neg.setFont(new Font("Tahoma", Font.PLAIN, 14));
			lbl13V2_neg.setBounds(155, 75, 60, 17);
			panel.add(lbl13V2_neg);
			
			label_13 = new JLabel("-13.2 V:");
			label_13.setRequestFocusEnabled(false);
			label_13.setHorizontalAlignment(SwingConstants.RIGHT);
			label_13.setForeground(Color.BLACK);
			label_13.setFont(new Font("Tahoma", Font.PLAIN, 14));
			label_13.setBounds(105, 75, 50, 17);
			panel.add(label_13);
			
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
			lblGainOffset.setFont(new Font("Tahoma", Font.PLAIN, 12));
			lblGainOffset.setBounds(11, 119, 65, 15);
			add(lblGainOffset);
			
			txtGainOffset = new JTextField();
			txtGainOffset.setText("0");
			txtGainOffset.setHorizontalAlignment(SwingConstants.RIGHT);
			txtGainOffset.setFont(new Font("Tahoma", Font.PLAIN, 16));
			txtGainOffset.setColumns(10);
			txtGainOffset.setBounds(77, 116, 42, 20);
			add(txtGainOffset);
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
}
