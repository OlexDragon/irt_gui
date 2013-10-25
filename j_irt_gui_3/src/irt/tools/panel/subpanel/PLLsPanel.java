package irt.tools.panel.subpanel;

import irt.controller.DeviceDebagController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.seter.DeviceDebagSetter;
import irt.data.IdValueForComboBox;
import irt.data.Listeners;
import irt.data.PacketWork;
import irt.data.PllRegisterTextFieldSlider;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

@SuppressWarnings("serial")
public class PLLsPanel extends JPanel {

	private static final int DN = 4194304;
	private static final int UP = 2097152;
	private static final int HIK = 8388608;
	protected static final int ID_DOWN_GAIN = 0,
								ID_UP_GAIN = 1,
								ID_OFFSET = 2;
	private JTextField txtPllReg;
	private JTextField txtCpDnGain;
	private JTextField txtCpUpGain;
	private JTextField txtOffsetCurr;

	private int pllIndex = 5;
	private Value value = new Value(0, 0, Long.MAX_VALUE, 0);

	private DeviceDebagController registerController;
	private JCheckBox chckbxHik;
	private JTextField selectedTextField;
	private JSlider slider;
	private PllRegisterTextFieldSlider pllReg;
	private JCheckBox chckbxUp;
	private JCheckBox chckbxDn;
	private DocumentListener documentListener;

	public PLLsPanel() {
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				startController();
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				if(registerController!=null)
					registerController.setRun(false);
				if(selectedTextField!=null)
					selectedTextField.setBackground(Color.WHITE);
			}
		});
		setBackground(new Color(25, 25, 112));
		setLayout(null);

		JComboBox<IdValueForComboBox> comboBox = new JComboBox<>(new DefaultComboBoxModel<>(new IdValueForComboBox[]{new IdValueForComboBox((short) 5, "PLL 1"),new IdValueForComboBox((short) 6, "PLL 2")}));
		comboBox.addItemListener(new ItemListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {

				if(itemEvent.getStateChange()==ItemEvent.SELECTED){
					pllIndex = ((IdValueForComboBox)((JComboBox<IdValueForComboBox>)itemEvent.getSource()).getSelectedItem()).getID();
					startController();
				}
			}
		});
		comboBox.setName("LO Select");
		comboBox.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0; }};}});
		comboBox.addPopupMenuListener(Listeners.popupMenuListener);
		comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		((JLabel)comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		comboBox.setForeground(Color.YELLOW);
		comboBox.setFont(new Font("Tahoma", Font.BOLD, 16));
		comboBox.setBackground((Color) null);
		comboBox.setBounds(72, 11, 122, 26);
		add(comboBox);
		
		slider = new JSlider();
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setOpaque(false);
		slider.setBounds(258, 5, 22, 260);
		add(slider);
		
		txtPllReg = new JTextField();
		txtPllReg.setHorizontalAlignment(SwingConstants.RIGHT);
		txtPllReg.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtPllReg.setColumns(10);
		txtPllReg.setBounds(68, 61, 97, 20);
		documentListener = new DocumentListener() {

			@Override public void removeUpdate(DocumentEvent e) { valueChanged(); }
			@Override public void insertUpdate(DocumentEvent e) { valueChanged(); }
			@Override public void changedUpdate(DocumentEvent e) { valueChanged(); }
			private void valueChanged() {
				new Parse(value, chckbxHik, chckbxDn, chckbxUp, txtCpDnGain, txtCpUpGain, txtOffsetCurr);
			}
		};
		txtPllReg.getDocument().addDocumentListener(documentListener);
		add(txtPllReg);
		
		JLabel lblPllReg = new JLabel("PLL reg:");
		lblPllReg.setForeground(Color.YELLOW);
		lblPllReg.setRequestFocusEnabled(false);
		lblPllReg.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPllReg.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblPllReg.setBounds(10, 62, 57, 17);
		add(lblPllReg);

		chckbxHik = new JCheckBox("HiK");
		chckbxHik.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setFlag((JCheckBox)ae.getSource(), HIK);
			}
		});
		chckbxHik.setFont(new Font("Tahoma", Font.BOLD, 14));
		chckbxHik.setForeground(Color.WHITE);
		chckbxHik.setOpaque(false);
		chckbxHik.setBounds(35, 223, 49, 25);
		add(chckbxHik);

		chckbxUp = new JCheckBox("UP");
		chckbxUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setFlag((JCheckBox)ae.getSource(), UP);
			}
		});
		chckbxUp.setFont(new Font("Tahoma", Font.BOLD, 14));
		chckbxUp.setForeground(Color.WHITE);
		chckbxUp.setOpaque(false);
		chckbxUp.setBounds(119, 224, 46, 23);
		add(chckbxUp);
		
		ImageButton imageButton = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		imageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DeviceDebagSetter packetWork = (DeviceDebagSetter)registerController.getPacketWork();
				packetWork.preparePacketToSend(packetWork.getPacketThread().getValue());
				registerController.setSend(true);
			}
		});
		imageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageButton.setToolTipText("Store Config");
		imageButton.setName("Store");
		imageButton.setBounds(174, 54, 33, 33);
		add(imageButton);

		chckbxDn = new JCheckBox("DN");
		chckbxDn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setFlag((JCheckBox)ae.getSource(), DN);
			}
		});
		chckbxDn.setOpaque(false);
		chckbxDn.setForeground(Color.WHITE);
		chckbxDn.setFont(new Font("Tahoma", Font.BOLD, 14));
		chckbxDn.setBounds(200, 224, 46, 23);
		add(chckbxDn);

		JLabel lblSet = new JLabel("SET");
		lblSet.setRequestFocusEnabled(false);
		lblSet.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSet.setForeground(Color.YELLOW);
		lblSet.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblSet.setBounds(207, 61, 27, 17);
		add(lblSet);

		txtCpDnGain = new JTextField();
		txtCpDnGain.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent fe) {
				setTextFieldBackground(ID_DOWN_GAIN, (JTextField) fe.getSource());
			}
		});
		txtCpDnGain.setHorizontalAlignment(SwingConstants.RIGHT);
		txtCpDnGain.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtCpDnGain.setColumns(10);
		txtCpDnGain.setBounds(173, 110, 75, 20);
		add(txtCpDnGain);

		JLabel lblCpDnGain = new JLabel("CP DN Gain:");
		lblCpDnGain.setRequestFocusEnabled(false);
		lblCpDnGain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpDnGain.setForeground(Color.YELLOW);
		lblCpDnGain.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblCpDnGain.setBounds(87, 112, 83, 17);
		add(lblCpDnGain);

		txtCpUpGain = new JTextField();
		txtCpUpGain.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent fe) {
				setTextFieldBackground(ID_UP_GAIN, (JTextField) fe.getSource());
			}
		});
		txtCpUpGain.setHorizontalAlignment(SwingConstants.RIGHT);
		txtCpUpGain.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtCpUpGain.setColumns(10);
		txtCpUpGain.setBounds(173, 147, 75, 20);
		add(txtCpUpGain);
		
		JLabel lblCpUpGain = new JLabel("CP UP Gain:");
		lblCpUpGain.setRequestFocusEnabled(false);
		lblCpUpGain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpUpGain.setForeground(Color.YELLOW);
		lblCpUpGain.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblCpUpGain.setBounds(89, 148, 81, 17);
		add(lblCpUpGain);
		
		txtOffsetCurr = new JTextField();
		txtOffsetCurr.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent fe) {
				setTextFieldBackground(ID_OFFSET, (JTextField) fe.getSource());
			}
		});
		txtOffsetCurr.setHorizontalAlignment(SwingConstants.RIGHT);
		txtOffsetCurr.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtOffsetCurr.setColumns(10);
		txtOffsetCurr.setBounds(173, 184, 75, 20);
		add(txtOffsetCurr);
		
		JLabel lblOffsetCurr = new JLabel("Offset Curr.:");
		lblOffsetCurr.setRequestFocusEnabled(false);
		lblOffsetCurr.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOffsetCurr.setForeground(Color.YELLOW);
		lblOffsetCurr.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblOffsetCurr.setBounds(84, 184, 86, 17);
		add(lblOffsetCurr);
	}

	private void setFlag(JCheckBox cb, int value) {
		if(cb.isSelected()){
			this.value.setValue(this.value.getValue() | value);
		}
		else{
			this.value.setValue(this.value.getValue() & (value^Integer.MAX_VALUE));
		}
		setPllRegText();
	}

	private void setPllRegText() {
		txtPllReg.getDocument().removeDocumentListener(documentListener);
		txtPllReg.setText(value.toString());
		txtPllReg.setToolTipText(Long.toHexString(value.getValue()).toUpperCase());
		txtPllReg.getDocument().addDocumentListener(documentListener);
	}

	private void setTextFieldBackground(int id, JTextField textField) {
		if(selectedTextField!=null)
			selectedTextField.setBackground(Color.WHITE);
		if(pllReg!=null)
			pllReg.clear();

		selectedTextField = textField;
		selectedTextField.setBackground(Color.YELLOW);
		pllReg = new PllRegisterTextFieldSlider(id, textField, slider, selectedTextField==txtOffsetCurr ? 5 : 20);
		pllReg.addVlueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {

				long v = value.getValue();
				long sv = ((Value) valueChangeEvent.getSource()).getValue();
				int id = valueChangeEvent.getID();
				long filter = 127;
				if(id>0){
					int shift = 7*id;
					filter = filter<<shift;
					if(sv>0)
						sv = sv<<shift;
				}

				filter^=Long.MAX_VALUE;
				v &= filter;
				v |= sv;
				value.setValue(v);
				setPllRegText();
			}
		});
	}

	private void startController() {
		if(registerController!=null)
			registerController.setRun(false);

		registerController = new DeviceDebagController(txtPllReg,
									null,
									value,
									new DeviceDebagSetter(null,
											pllIndex,
											9,
											PacketWork.PACKET_ID_FCM_DEVICE_DEBAG_PLL_REG,
											Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_READ_WRITE),
							0,
							Style.CHECK_ONCE);

		Thread t = new Thread(registerController, "PLL reg.N9");
		int priority = t.getPriority();
		t.setPriority(priority<=Thread.MIN_PRIORITY ? Thread.MIN_PRIORITY : priority-1);
		t.start();
	}

	private class Parse extends Thread{
		
		private long MAXIMUM = 127;

		private volatile Value value;
		private volatile JCheckBox chckbxHik;
		private volatile JCheckBox chckbxDn;
		private volatile JCheckBox chckbxUp;
		private volatile JTextField txtCpDnGain;
		private volatile JTextField txtCpUpGain;
		private volatile JTextField txtOffsetCurr;

		public Parse(Value value, JCheckBox chckbxHik, JCheckBox chckbxDn, JCheckBox chckbxUp, JTextField txtCpDnGain, JTextField txtCpUpGain, JTextField txtOffsetCurr) {
			this.value = value;
			this.chckbxHik = chckbxHik;
			this.chckbxDn = chckbxDn;
			this.chckbxUp = chckbxUp;
			this.txtCpDnGain = txtCpDnGain;
			this.txtCpUpGain = txtCpUpGain;
			this.txtOffsetCurr = txtOffsetCurr;
			
			int priority = getPriority();
			setPriority(priority<=Thread.MIN_PRIORITY ? Thread.MIN_PRIORITY : priority-1);
			start();
		}

		@Override
		public void run() {
			long v = value.getValue();
			if(v!=value.getOldValue()){
				txtPllReg.setToolTipText(Long.toHexString(v).toUpperCase());
				value.setValue(v);//to set oldValue == value
				chckbxHik.setSelected((v&HIK)>0);
				chckbxDn.setSelected((v&DN)>0);
				chckbxUp.setSelected((v&UP)>0);

				Value tmpValue = new Value(v&MAXIMUM, 0, MAXIMUM, 0);
				txtCpDnGain.setToolTipText(tmpValue.toString());
				txtCpDnGain.setText(""+tmpValue.getValue()*20);

				tmpValue.setValue((v>>7)&MAXIMUM);
				txtCpUpGain.setToolTipText(tmpValue.toString());
				txtCpUpGain.setText(""+tmpValue.getValue()*20);

				tmpValue.setValue((v>>14)&MAXIMUM);
				txtOffsetCurr.setToolTipText(tmpValue.toString());
				txtOffsetCurr.setText(""+tmpValue.getValue()*5);
			}
			this.value = null;
			this.chckbxHik = null;
			this.chckbxDn = null;
			this.chckbxUp = null;
			this.txtCpDnGain = null;
			this.txtCpUpGain = null;
			this.txtOffsetCurr = null;
		}
		
	}
}
