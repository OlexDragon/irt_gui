package irt.tools.panel.subpanel;

import irt.controller.AdcController;
import irt.controller.AdcCurrentController;
import irt.controller.DeviceDebagController;
import irt.controller.GuiController;
import irt.controller.NGlobalController;
import irt.controller.SetterController;
import irt.controller.SwitchControllerRegister;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.DeviceDebagGetter;
import irt.controller.serial_port.value.seter.DeviceDebagSetter;
import irt.controller.serial_port.value.seter.Setter;
import irt.controller.to_do.InitializePicoBuc;
import irt.data.DeviceInfo;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class BIASsPanel extends JPanel {

	private static final int P6 = 170;

	private static final int P5 = 137;

	private static final int P4 = 106;

	private static final int P3 = 75;

	private static final int P2 = 44;

	protected final Logger logger = (Logger) LogManager.getLogger();

	private final int P1 = 13;

	private JSlider slider;
	private JTextField txtStep;
	private SwitchBox switch_1;
	private SwitchBox switch_2;
	private JTextField txtPotentiometer1;
	private JTextField txtPotentiometer2;
	private JTextField txtPotentiometer3;
	private JTextField txtPotentiometer4;
	private JLabel lblPotentiometer1;
	private JLabel lblPotentiometer4;
	private JLabel lblPotentiometer2;
	private JLabel lblPotentiometer3;
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
	private JTextField txtPotentiometer5;
	private JTextField txtPotentiometer6;
	private JLabel lblPotentiometer5;
	private JLabel lblPotentiometer6;

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

				DeviceInfo deviceInfo = GuiController.getDeviceInfo();
				logger.trace(deviceInfo);
				boolean isNewBiasBoard = deviceInfo!=null ? deviceInfo.getType()<1000 && deviceInfo.getRevision()==2 : false;

				addController(new NGlobalController(switchNGlobal,
								new DeviceDebagGetter(linkHeader,
										6,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_NGLOBAL,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));

				((DeviceDebagController)addController(
						new DeviceDebagController(isNewBiasBoard ? "Potentiometer 2" : "Potentiometer 1",isNewBiasBoard ? txtPotentiometer2 : txtPotentiometer1,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										1,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N1,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						3,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController(
						new DeviceDebagController(isNewBiasBoard ? "Potentiometer 1" : "Potentiometer 2", isNewBiasBoard ? txtPotentiometer1 : txtPotentiometer2,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										1,
										8,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N2,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						11,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController(
						new DeviceDebagController(isNewBiasBoard ? "Potentiometer 5" : "Potentiometer 3", isNewBiasBoard ? txtPotentiometer5 : txtPotentiometer3,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										2,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N3,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						3,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				((DeviceDebagController)addController(
						new DeviceDebagController(isNewBiasBoard ? "Potentiometer 6" : "Potentiometer 4", isNewBiasBoard ? txtPotentiometer6 : txtPotentiometer4,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										2,
										8,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N4,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						11,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

				lblPotentiometer5.setVisible(isNewBiasBoard);
				lblPotentiometer6.setVisible(isNewBiasBoard);
				txtPotentiometer5.setVisible(isNewBiasBoard);
				txtPotentiometer6.setVisible(isNewBiasBoard);
				if(isNewBiasBoard){
					lblPotentiometer1.setText("Output1:");
					lblPotentiometer2.setText("Output2:");
					lblPotentiometer3.setText("Driver1:");
					lblPotentiometer4.setText("Driver2:");
					lblPotentiometer5.setText("Driver:");
					lblPotentiometer6.setText("Pred.Dr");

					((DeviceDebagController)addController(
							new DeviceDebagController(isNewBiasBoard ? "Potentiometer 4" : "Potentiometer 5", isNewBiasBoard ? txtPotentiometer4 : txtPotentiometer5,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										7,
										0,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N5,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						3,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);

					((DeviceDebagController)addController(
							new DeviceDebagController(isNewBiasBoard ? "Potentiometer 3" : "Potentiometer 6", isNewBiasBoard ? txtPotentiometer3 : txtPotentiometer6,
								slider,
								new Value(0, 0, 896, 0),
								new DeviceDebagSetter(linkHeader,
										7,
										8,
										PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_POTENTIOMETER_N6,
										Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
						11,
						Style.CHECK_ALWAYS))).addFocusListener(focusListener);
				}

				addController(new SwitchControllerRegister("Switch 1", switch_1, new DeviceDebagSetter(linkHeader, 3, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_SWITCH_N1, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));
				addController(new SwitchControllerRegister("Switch 2", switch_2, new DeviceDebagSetter(linkHeader, 4, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_SWITCH_N2, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE)));

				ValueDouble value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController(new AdcCurrentController("HS1_CURRENT", lblCurrent1, new DeviceDebagGetter(linkHeader,  5, 1, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_HS1_CURRENT, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController(new AdcCurrentController("HS2_CURRENT", lblCurrent2, new DeviceDebagGetter(linkHeader,  5, 2, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_HS2_CURRENT, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController(new AdcController("Output Power", lblOPower, new DeviceDebagGetter(linkHeader,  5, 3, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_OUTPUT_POWER, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));

				value = new ValueDouble(0, 0, 4095, 0);
				value.setPrefix(" mV");
				addController(new AdcController("DeviceDebag Temperature", lblTemp, new DeviceDebagGetter(linkHeader,  5, 4, PacketWork.PACKET_BIAS_25W_DEVICE_DEBAG_TEMPERATURE, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE), value));
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

			private ControllerAbstract addController(ControllerAbstract abstractController){

				Thread t = new Thread(abstractController);

				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();

				threadList.add(abstractController);

				return abstractController;
			}
		});

		Image offImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_off.png")).getImage();
		Image onImage = new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/switch_on.png")).getImage();

		switch_1 = new SwitchBox(offImage, onImage);
		switch_1.setBounds(61, 2, 55, 25);
		switch_1.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(switch_1);

		switch_2= new SwitchBox(offImage, onImage);
		switch_2.setCursor(new Cursor(Cursor.HAND_CURSOR));
		switch_2.setBounds(61, 30, 55, 25);
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
		txtPotentiometer1.setBounds(184, P1, 55, 20);
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
		txtStep.setBounds(205, 203, 34, 20);
		add(txtStep);

		txtPotentiometer2 = new JTextField();
		txtPotentiometer2.setText("0");
		txtPotentiometer2.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer2.setFont(font);
		txtPotentiometer2.setColumns(10);
		txtPotentiometer2.setBounds(184, P2, 55, 20);
		add(txtPotentiometer2);

		txtPotentiometer3 = new JTextField();
		txtPotentiometer3.setText("0");
		txtPotentiometer3.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer3.setFont(font);
		txtPotentiometer3.setColumns(10);
		txtPotentiometer3.setBounds(184, P3, 55, 20);
		add(txtPotentiometer3);

		txtPotentiometer4 = new JTextField();
		txtPotentiometer4.setText("0");
		txtPotentiometer4.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer4.setFont(font);
		txtPotentiometer4.setColumns(10);
		txtPotentiometer4.setBounds(184, P4, 55, 20);
		add(txtPotentiometer4);

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

		switchNGlobal = new SwitchBox(offImage, onImage);
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

		
		ImageButton imageButton = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				new SetterController("Initialize Controller",
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
		
		txtPotentiometer5 = new JTextField();
		txtPotentiometer5.setText("0");
		txtPotentiometer5.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer5.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer5.setColumns(10);
		txtPotentiometer5.setBounds(184, P5, 55, 20);
		add(txtPotentiometer5);
		
		lblPotentiometer6 = new JLabel("MMIC:");
		lblPotentiometer6.setRequestFocusEnabled(false);
		lblPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPotentiometer6.setBounds(126, 170, 57, 17);
		add(lblPotentiometer6);
		
		txtPotentiometer6 = new JTextField();
		txtPotentiometer6.setText("0");
		txtPotentiometer6.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPotentiometer6.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPotentiometer6.setColumns(10);
		txtPotentiometer6.setBounds(184, P6, 55, 20);
		add(txtPotentiometer6);
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
