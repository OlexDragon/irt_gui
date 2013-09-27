package irt.tools.panel;

import irt.data.Listeners;
import irt.data.packet.LinkHeader;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;
import irt.tools.label.ImageLabel;
import irt.tools.label.LED;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.InfoPanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import resources.Translation;

@SuppressWarnings("serial")
public class DemoPanel extends Panel {
	private static final float _14 = 14;
	private static final int ledLockWidth = 81;
	private static final float _18 = 18;
	private static final float MUTE_LABEL_FONT_SIZE = 16;
	private static final Font FONT = new Font("Tahoma", Font.BOLD, (int)MUTE_LABEL_FONT_SIZE);
	private JTextField textField;
	private JTextField textField_1;
	private JSlider slider;
	private JLabel lblMute;
	private ImageButton btnMute;
	private LED ledMute;
	private LED ledLock;
	private JLabel lblInputPower;
	private JLabel lblOutputPower;
	private JLabel lblTemperature;
	private TitledBorder controlTitledBorder;
	private TitledBorder monitorTitledBorder;
	private JComboBox<String> comboBox;
	private JCheckBox checkBoxStep;
	private ImageButton btnStorConfig;
	private InfoPanel infoPanel;
	private ImageLabel imageLabel;
	private String tabTitle;

	public DemoPanel(String imagePath, String tabTitle) {
		super("THE UNIT IS NOT CONNECTED", 0, 0, 0, 0, 0);
		this.tabTitle = tabTitle;
		
		Font font = Translation.replaceFont("resource.font", "mute_label_font_size", FONT, _18);
		JPanel panel = new JPanel();
		monitorTitledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), Translation.getValue(String.class, "monitor", "Monitor"), TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		panel.setBorder(monitorTitledBorder);
		panel.setOpaque(false);
		panel.setBounds(10, 12, 214, 210);
		panel.setLayout(null);
		userPanel.add(panel);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");
		
		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);
		ledLock.setBounds(19, 152, Translation.getValue(Integer.class, "monitor.led.lock.size", ledLockWidth), 28);
		panel.add(ledLock);

		ledMute = new LED(Color.YELLOW, muteText);
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);
		ledMute.setBounds(117, 152, 84, 28);
		panel.add(ledMute);

		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 233, 214, 150);
		controlTitledBorder = new TitledBorder(UIManager.getBorder("TitledBorder.border"), Translation.getValue(String.class, "control", "Control"), TitledBorder.LEADING, TitledBorder.TOP, font, Color.WHITE);
		panel_1.setBorder(controlTitledBorder);
		panel_1.setOpaque(false);
		panel_1.setBounds(10, 225, 214, 180);
		panel_1.setLayout(null);
		userPanel.add(panel_1);
		
		btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setToolTipText(muteText);
		btnMute.setShadowShiftY(4);
		btnMute.setShadowShiftX(4);
		btnMute.setShadowPressedShiftY(1);
		btnMute.setShadowPressedShiftX(1);
		btnMute.setName("Button Mute");
		btnMute.setBounds(13, 104, 33, 33);
		panel_1.add(btnMute);
		
		btnStorConfig = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		btnStorConfig.setToolTipText(Translation.getValue(String.class, "stor_config", "Store Config"));
		btnStorConfig.setShadowShiftY(4);
		btnStorConfig.setShadowShiftX(4);
		btnStorConfig.setShadowPressedShiftY(1);
		btnStorConfig.setShadowPressedShiftX(1);
		btnStorConfig.setName("Store");
		btnStorConfig.setBounds(162, 104, 33, 33);
		panel_1.add(btnStorConfig);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", MUTE_LABEL_FONT_SIZE));
		lblMute = new JLabel(muteText);
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(font);
		lblMute.setBounds(46, 110, 84, 20);
		panel_1.add(lblMute);
		
		textField = new JTextField();
		textField.setEnabled(false);
		textField.setName("Text Gain");
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setForeground(Color.YELLOW);
		textField.setFont(FONT);
		textField.setColumns(10);
		textField.setCaretColor(Color.WHITE);
		textField.setBackground(new Color(11, 23, 59));
		textField.setBounds(13, 45, 188, 20);
		panel_1.add(textField);
		
		comboBox = new JComboBox<String>();
		comboBox.addPopupMenuListener(Listeners.popupMenuListener);
		comboBox.setForeground(Color.YELLOW);
		comboBox.setBackground(new Color(11, 23, 59));
		comboBox.setBounds(13, 22, 85, 20);
		comboBox.addItem(Translation.getValue(String.class, "attenuation", "ATTENUATION"));
		((JLabel)comboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		comboBox.setUI(new BasicComboBoxUI(){

			@Override
			protected JButton createArrowButton() {
				return new JButton(){

					@Override
					public int getWidth() {
						return 0;
					}};
			}
		});
		panel_1.add(comboBox);
		
		textField_1 = new JTextField();
		textField_1.setEnabled(false);
		textField_1.setText("1");
		textField_1.setHorizontalAlignment(SwingConstants.CENTER);
		textField_1.setForeground(Color.WHITE);
		textField_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		textField_1.setColumns(10);
		textField_1.setCaretColor(Color.YELLOW);
		textField_1.setBackground(new Color(11, 23, 59));
		textField_1.setBounds(74, 68, 127, 20);
		panel_1.add(textField_1);
		
		JComboBox<String> cbLoSelect = new JComboBox<String>();
		cbLoSelect.setName("LO Select");
		cbLoSelect.setUI(new BasicComboBoxUI(){

			@Override
			protected JButton createArrowButton() {
				return new JButton(){

					@Override
					public int getWidth() {
						return 0;
					}};
			}
		});
		cbLoSelect.setForeground(Color.YELLOW);
		cbLoSelect.addPopupMenuListener(Listeners.popupMenuListener);
		cbLoSelect.setBackground(new Color(0x0B,0x17,0x3B));
		cbLoSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
		cbLoSelect.setFont(FONT);
		cbLoSelect.setBounds(10, 141, 194, 26);
		cbLoSelect.addItem("LO:1 12800 MHz");
		cbLoSelect.addItem("LO:2 13050 MHz");
		panel_1.add(cbLoSelect);
		
		slider = new JSlider();
		slider.setEnabled(false);
		slider.setMinimum(0);
		slider.setMaximum(20);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Value v = new ValueDouble(0, 0, 20, 1);
				v.setPrefix(" dB");
				if (slider.isFocusOwner())
					v.setValue(slider.getValue());
					textField.setText(v.toString());
			}
		});
		slider.setOpaque(false);
		slider.setBounds(234, 12, 28, 397);
		userPanel.add(slider);
		slider.setOrientation(SwingConstants.VERTICAL);
		
		infoPanel = new InfoPanel((LinkHeader) null);
		infoPanel.setBounds(10, 11, 286, 104);
		extraPanel.add(infoPanel);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setOpaque(false);
		tabbedPane.setBounds(10, 123, 286, 296);
		extraPanel.add(tabbedPane);
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		imageLabel = new ImageLabel(new ImageIcon(UserPicobucPanel.class.getResource(imagePath)),"");
		tabbedPane.addTab(tabTitle, null, imageLabel, null);
		
		font = font.deriveFont(Translation.getValue(Float.class, "monitor.led.ip.fomt.size", _14)).deriveFont(Font.PLAIN);

		lblInputPower = new JLabel(Translation.getValue(String.class, "input_power", "Input Power")+":");
		lblInputPower.setName("");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(new Color(153, 255, 255));
		lblInputPower.setFont(font);
		lblInputPower.setBounds(5, 36, 100, 17);
		panel.add(lblInputPower);
		
		JLabel lblDbm = new JLabel("-25 dBm");
		lblDbm.setName("Input Power");
		lblDbm.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm.setForeground(Color.WHITE);
		lblDbm.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDbm.setBounds(118, 36, 82, 17);
		panel.add(lblDbm);
		
		lblOutputPower = new JLabel(Translation.getValue(String.class, "output_power", "Output Power")+":");
		lblOutputPower.setName("");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(new Color(153, 255, 255));
		lblOutputPower.setFont(font);
		lblOutputPower.setBounds(5, 64, 100, 17);
		panel.add(lblOutputPower);
		
		JLabel lblDbm_1 = new JLabel("40 dBm");
		lblDbm_1.setName("Output Power");
		lblDbm_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm_1.setForeground(Color.WHITE);
		lblDbm_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDbm_1.setBounds(107, 64, 93, 17);
		panel.add(lblDbm_1);
		
		lblTemperature = new JLabel(Translation.getValue(String.class, "temperature", "Temperature")+":");
		lblTemperature.setName("");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(new Color(153, 255, 255));
		lblTemperature.setFont(font);
		lblTemperature.setBounds(5, 92, 100, 17);
		panel.add(lblTemperature);
		
		JLabel lblC = new JLabel("46 C");
		lblC.setName("Temperature");
		lblC.setHorizontalAlignment(SwingConstants.RIGHT);
		lblC.setForeground(Color.WHITE);
		lblC.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblC.setBounds(116, 92, 84, 17);
		panel.add(lblC);

		font = font.deriveFont((float) 12.0);
		checkBoxStep = new JCheckBox(Translation.getValue(String.class, "step", "Step")+":");
		checkBoxStep.setEnabled(false);
		checkBoxStep.setOpaque(false);
		checkBoxStep.setForeground(Color.WHITE);
		checkBoxStep.setFont(font);
		checkBoxStep.setBounds(13, 68, 55, 23);
		panel_1.add(checkBoxStep);
	}

	@Override
	public void refresh() {
		super.refresh();
		infoPanel.refresh();

		Font font = Translation.replaceFont("resource.font", "mute_label_font_size", FONT, MUTE_LABEL_FONT_SIZE);
		lblMute.setFont(font);
		String text = Translation.getValue(String.class, "mute", "MUTE");
		lblMute.setText(text);
		btnMute.setToolTipText(text);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", _18));
		ledMute.setFont(font);
		ledMute.setText(text);
		ledLock.setFont(font);
		ledLock.setSize(Translation.getValue(Integer.class, "monitor.led.lock.size", ledLockWidth), ledLock.getHeight() );
		ledLock.setText(Translation.getValue(String.class, "lock", "LOCK"));

		monitorTitledBorder.setTitle(Translation.getValue(String.class, "monitor", "Monitor"));
		monitorTitledBorder.setTitleFont(font);
		controlTitledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));
		controlTitledBorder.setTitleFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.led.ip.fomt.size", _14)).deriveFont(Font.PLAIN);
		lblInputPower.setFont(font);
		lblInputPower.setText(Translation.getValue(String.class, "input_power", "Input Power")+":");

		lblOutputPower.setFont(font);
		lblOutputPower.setText(Translation.getValue(String.class, "output_power", "Output Power")+":");

		lblTemperature.setFont(font);
		lblTemperature.setText(Translation.getValue(String.class, "temperature", "Temperature")+":");

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{Translation.getValue(String.class, "attenuation", "Attenuation")});
		comboBox.setModel(model);

		font = font.deriveFont((float) 12.0);
		checkBoxStep.setFont(font);
		checkBoxStep.setText(Translation.getValue(String.class, "step", "Step")+":");

		btnStorConfig.setToolTipText(Translation.getValue(String.class, "stor_config", "Store Config"));
	}

	public ImageLabel getImageLabel() {
		return imageLabel;
	}

	public String getTabTitle() {
		return tabTitle;
	}
}
