package irt.tools.panel.subpanel;

import irt.controller.AdcController;
import irt.controller.DeviceDebagController;
import irt.controller.NGlobalController;
import irt.controller.SetterController;
import irt.controller.SwitchControllerRegister;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.DeviceDebagGetter;
import irt.controller.serial_port.value.seter.DeviceDebagSetter;
import irt.controller.serial_port.value.seter.Setter;
import irt.controller.to_do.InitializePicoBuc;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ControllerFocusListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.irt_gui.IrtGui;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.button.ImageButton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class BIASsPanel extends JPanel {

	private JSlider slider;
	private JTextField txtStep;
	private SwitchBox switch_1;
	private SwitchBox switch_2;
	private JTextField txtPotentiometer1;
	private JTextField txtPotentiometer2;
	private JTextField txtPotentiometer3;
	private JTextField txtPotentiometer4;
	private JLabel lblOutput;
	private JLabel lblMmic;
	private JLabel lblDriver;
	private JLabel lblPred;
	private JLabel lblOutput_1;
	private JLabel lblLineUp;
	private SwitchBox switchNGlobal;
	private JCheckBox chckbxStep;
	private JTextField activeTextField;
	private JLabel lblCurrent_11;
	private JLabel lblCurrent1;
	private JLabel lblCurr;
	private JLabel lblCurrent2;
	private JLabel lblOutPower;
	private JLabel lblOPower;
	private JLabel lblTemp_1;
	private JLabel lblTemp;

	public BIASsPanel(final LinkHeader linkHeader) {
		setLayout(null);
		addAncestorListener(new AncestorListener() {
			private ControllerFocusListener focusListener = new ControllerFocusListener() {

				@Override
				public void focusGained(ValueChangeEvent valueChangeEvent) {

					if(activeTextField!=null)
						activeTextField.setBackground(Color.WHITE);

					activeTextField = (JTextField) valueChangeEvent.getSource();

					activeTextField.setBackground(Color.YELLOW);
				}
			};

			private List<ControllerAbstract> threadList = new ArrayList<>();

			public void ancestorAdded(AncestorEvent arg0) {

				addController("NGlobal",
						new NGlobalController(switchNGlobal,
								new DeviceDebagGetter(linkHeader,
										6,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_NGLOBAL,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));

				((DeviceDebagController)addController("Potentiometer 1",
						new DeviceDebagController(txtPotentiometer1,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										1,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTRNTIOMETER_N1,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						3,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController("Potentiometer 2",
						new DeviceDebagController(txtPotentiometer2,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										1,
										8,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTRNTIOMETER_N2,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						11,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController("Potentiometer 3",
						new DeviceDebagController(txtPotentiometer3,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										2,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTRNTIOMETER_N3,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						3,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController("Potentiometer 4",
						new DeviceDebagController(txtPotentiometer4,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										2,
										8,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTRNTIOMETER_N4,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						11,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				addController("Switch 1", new SwitchControllerRegister(switch_1, new DeviceDebagSetter(linkHeader, 3, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_SWITCH_N1, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));
				addController("Switch 2", new SwitchControllerRegister(switch_2, new DeviceDebagSetter(linkHeader, 4, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_SWITCH_N2, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));

				ValueDouble value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController("HS1_CURRENT", new AdcController(lblCurrent1, new DeviceDebagGetter(linkHeader,  5, 1, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_HS1_CURRENT, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController("HS2_CURRENT", new AdcController(lblCurrent2, new DeviceDebagGetter(linkHeader,  5, 2, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_HS2_CURRENT, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController("Output Power", new AdcController(lblOPower, new DeviceDebagGetter(linkHeader,  5, 3, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_OUTPUT_POWER, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController("DeviceDebag Temperature", new AdcController(lblTemp, new DeviceDebagGetter(linkHeader,  5, 4, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_TEMPERATURE, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));
			}
			public void ancestorMoved(AncestorEvent arg0) {}

			public void ancestorRemoved(AncestorEvent arg0) {
				for(ControllerAbstract t:threadList){
					t.setRun(false);
					if(t instanceof DeviceDebagController)
						((DeviceDebagController)t).removeFocusListener(focusListener);
				}
				threadList.clear();
			}

			private ControllerAbstract addController(String threadName, ControllerAbstract abstractController){

				Thread t = new Thread(abstractController, threadName);

				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.start();

				threadList.add(abstractController);

				return abstractController;
			}
		});

		Image offImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png")).getImage();
		Image onImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png")).getImage();

		switch_1 = new SwitchBox(offImage, onImage);
		switch_1.setBounds(22, 31, 72, 30);
		switch_1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(switch_1);

		switch_2= new SwitchBox(offImage, onImage);
		switch_2.setCursor(new Cursor(Cursor.HAND_CURSOR));
		switch_2.setBounds(22, 82, 72, 30);
		add(switch_2);

		slider = new JSlider();
		slider.setOpaque(false);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setBounds(249, 0, 22, 260);
		add(slider);

		Font font = new Font("Tahoma", Font.PLAIN, 14);

		txtPotentiometer1 = new JTextField();
		txtPotentiometer1.setText("0");
		txtPotentiometer1.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer1.setFont(font);
		txtPotentiometer1.setColumns(10);
		txtPotentiometer1.setBounds(184, 13, 55, 20);
		add(txtPotentiometer1);

		txtStep = new JTextField();
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
		txtStep.setText("1");
		txtStep.setHorizontalAlignment(SwingConstants.RIGHT);
		txtStep.setFont(font);
		txtStep.setColumns(10);
		txtStep.setBounds(205, 134, 34, 20);
		add(txtStep);

		txtPotentiometer2 = new JTextField();
		txtPotentiometer2.setText("0");
		txtPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer2.setFont(font);
		txtPotentiometer2.setColumns(10);
		txtPotentiometer2.setBounds(184, 44, 55, 20);
		add(txtPotentiometer2);

		txtPotentiometer3 = new JTextField();
		txtPotentiometer3.setText("0");
		txtPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer3.setFont(font);
		txtPotentiometer3.setColumns(10);
		txtPotentiometer3.setBounds(184, 75, 55, 20);
		add(txtPotentiometer3);

		txtPotentiometer4 = new JTextField();
		txtPotentiometer4.setText("0");
		txtPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer4.setFont(font);
		txtPotentiometer4.setColumns(10);
		txtPotentiometer4.setBounds(184, 106, 55, 20);
		add(txtPotentiometer4);

		font = font.deriveFont(12f);

		lblOutput = new JLabel("OUTPUT:");
		lblOutput.setRequestFocusEnabled(false);
		lblOutput.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutput.setFont(font);
		lblOutput.setBounds(126, 13, 57, 17);
		add(lblOutput);

		lblMmic = new JLabel("MMIC:");
		lblMmic.setRequestFocusEnabled(false);
		lblMmic.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMmic.setFont(font);
		lblMmic.setBounds(126, 106, 57, 17);
		add(lblMmic);

		lblDriver = new JLabel("DRIVER:");
		lblDriver.setRequestFocusEnabled(false);
		lblDriver.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDriver.setFont(font);
		lblDriver.setBounds(126, 47, 57, 17);
		add(lblDriver);

		lblPred = new JLabel("PRED:");
		lblPred.setRequestFocusEnabled(false);
		lblPred.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPred.setFont(font);
		lblPred.setBounds(126, 78, 57, 17);
		add(lblPred);

		lblOutput_1 = new JLabel("OUTPUT");
		lblOutput_1.setRequestFocusEnabled(false);
		lblOutput_1.setFont(font);
		lblOutput_1.setBounds(30, 13, 57, 17);
		add(lblOutput_1);

		lblLineUp = new JLabel("LINE UP");
		lblLineUp.setRequestFocusEnabled(false);
		lblLineUp.setFont(font);
		lblLineUp.setBounds(31, 64, 57, 17);
		add(lblLineUp);

		switchNGlobal = new SwitchBox(offImage, onImage);
		switchNGlobal.setEnabled(false);
		switchNGlobal.setBounds(13, 131, 107, 44);
		add(switchNGlobal);

		JLabel lblNglobal = new JLabel("NGlobal");
		lblNglobal.setRequestFocusEnabled(false);
		lblNglobal.setFont(font);
		lblNglobal.setBounds(22, 113, 57, 17);
		add(lblNglobal);

		chckbxStep = new JCheckBox("Step:");
		chckbxStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				slider.setSnapToTicks(chckbxStep.isSelected());
			}
		});
		chckbxStep.setFont(font.deriveFont(11f));
		chckbxStep.setOpaque(false);
		chckbxStep.setBounds(152, 133, 55, 23);
		add(chckbxStep);

		lblCurrent_11 = new JLabel("CURR:");
		lblCurrent_11.setRequestFocusEnabled(false);
		lblCurrent_11.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent_11.setFont(font);
		lblCurrent_11.setBounds(124, 158, 41, 17);
		add(lblCurrent_11);

		lblCurrent1 = new JLabel(":");
		lblCurrent1.setRequestFocusEnabled(false);
		lblCurrent1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent1.setFont(font);
		lblCurrent1.setBounds(171, 158, 68, 17);
		add(lblCurrent1);

		lblCurr = new JLabel("CURR:");
		lblCurr.setRequestFocusEnabled(false);
		lblCurr.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurr.setFont(font);
		lblCurr.setBounds(124, 186, 41, 17);
		add(lblCurr);
		
		lblCurrent2 = new JLabel(":");
		lblCurrent2.setRequestFocusEnabled(false);
		lblCurrent2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCurrent2.setFont(font);
		lblCurrent2.setBounds(171, 186, 68, 17);
		add(lblCurrent2);
		
		lblOutPower = new JLabel("OUTPUT POWER:");
		lblOutPower.setRequestFocusEnabled(false);
		lblOutPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutPower.setFont(font);
		lblOutPower.setBounds(49, 242, 116, 17);
		add(lblOutPower);
		
		lblOPower = new JLabel(":");
		lblOPower.setRequestFocusEnabled(false);
		lblOPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOPower.setFont(font);
		lblOPower.setBounds(171, 242, 68, 17);
		add(lblOPower);
		
		lblTemp_1 = new JLabel("TEMP:");
		lblTemp_1.setRequestFocusEnabled(false);
		lblTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemp_1.setFont(font);
		lblTemp_1.setBounds(124, 214, 39, 17);
		add(lblTemp_1);

		lblTemp = new JLabel(":");
		lblTemp.setRequestFocusEnabled(false);
		lblTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemp.setFont(font);
		lblTemp.setBounds(171, 214, 68, 17);
		add(lblTemp);

		
		ImageButton imageButton = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				new SetterController(
						new Setter(linkHeader,
								Packet.IRT_SLCP_PACKET_TYPE_COMMAND,
								Packet.IRT_SLCP_PACKET_ID_PRODUCTION_GENERIC_SET_1,
								Packet.IRTSCP_PACKET_ID_PRODUCTION_GENERIC_SET_1_DP_INIT,
								PacketWork.PACKET_ID_PRODUCTION_GENERIC_SET_1_INITIALIZE),
						new InitializePicoBuc(BIASsPanel.this), Style.CHECK_ONCE
				);
			}
		});
		imageButton.setToolTipText("Initialize");
		imageButton.setName("Initialize");
		imageButton.setBounds(22, 182, 33, 33);
		add(imageButton);
		
		JLabel lblInitialize = new JLabel("Initialize");
		lblInitialize.setHorizontalAlignment(SwingConstants.CENTER);
		lblInitialize.setBounds(14, 216, 46, 14);
		add(lblInitialize);
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
