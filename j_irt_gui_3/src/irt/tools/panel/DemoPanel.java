package irt.tools.panel;

import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.packet.LinkHeader;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.irt_gui.IrtGui;
import irt.tools.button.ImageButton;
import irt.tools.label.ImageLabel;
import irt.tools.label.LED;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.Panel;
import irt.tools.panel.subpanel.InfoPanel;
import irt.tools.panel.subpanel.NetworkPanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.io.IOException;
import java.util.Properties;

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

@SuppressWarnings("serial")
public class DemoPanel extends Panel {
	private JTextField textField;
	private JTextField textField_1;
	private JSlider slider;
	private JLabel lblMute;
	private ImageButton btnMute;
	private LED ledMute;
	private LED ledLock;
	private JLabel lblInputPowerText;
	private JLabel lblOutputPowerText;
	private JLabel lblTemperatureText;

	private Properties properties = getProperties();
	private TitledBorder monitorTitledBorder;
	private TitledBorder controlTitledBorder;
	private ImageButton btnStoreConfig;
	private JCheckBox checkBoxStep;
	private JComboBox<String> cbLoSelect;
	private JComboBox<String> comboBoxAttenuation;
	private InfoPanel infoPanel;
	private JLabel lblDbm;

	public DemoPanel() {
		super(Translation.getValue(String.class, "vertical_label_text", "THE UNIT IS NOT CONNECTED"), 0, 0, 0, 0, 0);
		
		String selectedLanguage = Translation.getSelectedLanguage();

		JPanel panel = new JPanel();
		Font font = Translation.getFont();
		monitorTitledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "monitor", "Monitor"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font ,
				Color.WHITE);
		panel.setBorder(monitorTitledBorder);
		panel.setOpaque(false);
		panel.setBounds(10, 12, 214, 210);
		panel.setLayout(null);
		userPanel.add(panel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 233, 214, 150);
		controlTitledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "control", "Control"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font,
				Color.WHITE);

		panel_1.setBorder(controlTitledBorder);
		panel_1.setOpaque(false);
		panel_1.setBounds(10, 225, 214, 180);
		panel_1.setLayout(null);
		userPanel.add(panel_1);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		font = font.deriveFont(
				Float.parseFloat(
						properties.getProperty(
								"verticalLabel.font.size_"+selectedLanguage)
				)
			).deriveFont(Font.PLAIN);

		verticalLabel.setFont(font);

		font = font.deriveFont(Float.parseFloat(properties.getProperty("monitor.leds.font.size_"+selectedLanguage)))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.leds.font.style_" + selectedLanguage)));

		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);
		ledLock.setBounds(
						19,
						152,
						Integer.parseInt(properties.getProperty("monitor.led.lock.width_"+selectedLanguage)),
						28);
		panel.add(ledLock);

		ledMute = new LED(Color.YELLOW, muteText);
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);
		ledMute.setBounds(
						117,
						152,
						Integer.parseInt(properties.getProperty("monitor.led.mute.width_"+selectedLanguage)),
						28);
		panel.add(ledMute);

		lblMute = new JLabel(muteText);
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(font);
		lblMute.setBounds(46, 110, 84, 20);
		panel_1.add(lblMute);

		btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setName("Button Mute");
		btnMute.setToolTipText(muteText);
		btnMute.setShadowShiftY(4);
		btnMute.setShadowShiftX(4);
		btnMute.setShadowPressedShiftY(1);
		btnMute.setShadowPressedShiftX(1);
		btnMute.setBounds(13, 104, 33, 33);
		panel_1.add(btnMute);
		
		btnStoreConfig = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		btnStoreConfig.setName("Store");
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));
		btnStoreConfig.setShadowShiftY(4);
		btnStoreConfig.setShadowShiftX(4);
		btnStoreConfig.setShadowPressedShiftY(1);
		btnStoreConfig.setShadowPressedShiftX(1);
		btnStoreConfig.setBounds(124, 104, 33, 33);
		panel_1.add(btnStoreConfig);
		
		textField = new JTextField();
		textField.setEnabled(false);
		textField.setName("Text Gain");
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setForeground(Color.YELLOW);
		textField.setFont(font);
		textField.setColumns(10);
		textField.setCaretColor(Color.WHITE);
		textField.setBackground(new Color(11, 23, 59));
		textField.setBounds(13, 45, 188, 20);
		panel_1.add(textField);

		comboBoxAttenuation = new JComboBox<>();
		comboBoxAttenuation.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxAttenuation.setForeground(Color.YELLOW);
		comboBoxAttenuation.setBackground(new Color(11, 23, 59));
		comboBoxAttenuation.setBounds(13, 22, 85, 20);
		comboBoxAttenuation.addItem(Translation.getValue(String.class, "attenuation", "ATTENUATION"));
		((JLabel)comboBoxAttenuation.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		comboBoxAttenuation.setUI(new BasicComboBoxUI(){

			@Override
			protected JButton createArrowButton() {
				return new JButton(){

					@Override
					public int getWidth() {
						return 0;
					}};
			}
		});
		comboBoxAttenuation.setFont(comboBoxAttenuation.getFont().deriveFont(Float.parseFloat(properties.getProperty("control.comboBox.font.size_"+selectedLanguage))));
		panel_1.add(comboBoxAttenuation);

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
		
		slider = new JSlider();
		slider.setEnabled(false);
		slider.setMinimum(0);
		slider.setMaximum(20);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Value v = new ValueDouble(0, 0, 20, 1);
				v.setPrefix(Translation.getValue(String.class, "dbm", " dBm"));
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

		JLabel lblLogo = new ImageLabel(
				new ImageIcon(IrtGui.class.getResource(
						IrtPanel.properties.getProperty("company_logo_"+IrtPanel.companyIndex))
				),"");
		tabbedPane.addTab(IrtPanel.properties.getProperty("company_name_"+IrtPanel.companyIndex), null, lblLogo, null);

		font = font.deriveFont(new Float(properties.getProperty("monitor.labels.font.size_" + selectedLanguage)))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.labels.font.style_" + selectedLanguage)));

		lblInputPowerText = new JLabel(Translation.getValue(String.class, "input_power", "Input Power")+":");
		lblInputPowerText.setName("");
		lblInputPowerText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPowerText.setForeground(new Color(153, 255, 255));
		lblInputPowerText.setFont(font);
		lblInputPowerText.setBounds(5, 36, 112, 17);
		panel.add(lblInputPowerText);
		
		lblOutputPowerText = new JLabel(Translation.getValue(String.class, "output_power", "Output Power")+":");
		lblOutputPowerText.setName("");
		lblOutputPowerText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPowerText.setForeground(new Color(153, 255, 255));
		lblOutputPowerText.setFont(font);
		lblOutputPowerText.setBounds(5, 64, 112, 17);
		panel.add(lblOutputPowerText);
		
		lblTemperatureText = new JLabel(Translation.getValue(String.class, "temperature", "Temperature")+":");
		lblTemperatureText.setName("");
		lblTemperatureText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperatureText.setForeground(new Color(153, 255, 255));
		lblTemperatureText.setFont(font);
		lblTemperatureText.setBounds(5, 92, 112, 17);
		panel.add(lblTemperatureText);

//		font = new Font("Tahoma", Font.PLAIN, 14);

		lblDbm = new JLabel("-25 "+Translation.getValue(String.class, "dbm", " dBm"));
		lblDbm.setName("Input Power");
		lblDbm.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm.setForeground(Color.WHITE);
		lblDbm.setFont(font);
		lblDbm.setBounds(118, 36, 82, 17);
		panel.add(lblDbm);

		font = font.deriveFont(Float.parseFloat(properties.getProperty("control.checkBox.font.size_"+selectedLanguage)));

		checkBoxStep = new JCheckBox(Translation.getValue(String.class, "step", "Step")+":");
		checkBoxStep.setEnabled(false);
		checkBoxStep.setOpaque(false);
		checkBoxStep.setForeground(Color.WHITE);
		checkBoxStep.setFont(font);
		checkBoxStep.setBounds(13, 68, 65, 23);
		panel_1.add(checkBoxStep);
		
		JLabel lblDbm_1 = new JLabel("40 "+Translation.getValue(String.class, "dbm", " dBm"));
		lblDbm_1.setName("Output Power");
		lblDbm_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm_1.setForeground(Color.WHITE);
		lblDbm_1.setFont(font);
		lblDbm_1.setBounds(107, 64, 93, 17);
		panel.add(lblDbm_1);
		
		JLabel lblC = new JLabel("46 C");
		lblC.setName("Temperature");
		lblC.setHorizontalAlignment(SwingConstants.RIGHT);
		lblC.setForeground(Color.WHITE);
		lblC.setFont(font);
		lblC.setBounds(116, 92, 84, 17);
		panel.add(lblC);

		font = font.deriveFont(18f);

		cbLoSelect = new JComboBox<String>();
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
		cbLoSelect.setFont(font);
		cbLoSelect.setBounds(10, 141, 194, 26);
		String loTxt = Translation.getValue(String.class, "lo", "LO");
		cbLoSelect.addItem(loTxt+":1 12800 MHz");
		cbLoSelect.addItem(loTxt+":2 13050 MHz");
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(cbLoSelect);

		NetworkPanel networkPanel = new NetworkPanel(null);
		tabbedPane.addTab("Network", null, networkPanel, null);
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getResourceAsStream("PicoBucPanel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	@Override
	public void refresh() {

		String selectedLanguage = Translation.getSelectedLanguage();

		monitorTitledBorder.setTitle(Translation.getValue(String.class, "monitor", "Monitor"));
		Font font =Translation.getFont();
		monitorTitledBorder.setTitleFont(font);
		controlTitledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));
		controlTitledBorder.setTitleFont(font);

		font = font.deriveFont(Float.parseFloat(properties.getProperty("monitor.leds.font.size_"+selectedLanguage)))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.leds.font.style_" + selectedLanguage)));

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		ledMute.setFont(font);
		ledMute.setText(muteText);
		ledMute.setSize(
				Integer.parseInt(properties.getProperty("monitor.led.mute.width_"+selectedLanguage)),
				ledMute.getHeight());

		ledLock.setFont(font);
		ledLock.setSize(
				Integer.parseInt(properties.getProperty("monitor.led.lock.width_"+selectedLanguage)),
				ledLock.getHeight());
		ledLock.setText(Translation.getValue(String.class, "lock", "LOCK"));

		lblMute.setText(muteText);
		lblMute.setFont(font);

		font = font.deriveFont(new Float(properties.getProperty("monitor.labels.font.size_" + selectedLanguage)))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.labels.font.style_" + selectedLanguage)));

		lblInputPowerText.setFont(font);
		lblInputPowerText.setText(Translation.getValue(String.class, "input_power", "Input Power")+":");

		lblOutputPowerText.setFont(font);
		lblOutputPowerText.setText(Translation.getValue(String.class, "output_power", "Output Power")+":");

		lblTemperatureText.setFont(font);
		lblTemperatureText.setText(Translation.getValue(String.class, "temperature", "Temperature")+":");

		btnMute.setToolTipText(muteText);
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));

		font = font.deriveFont(Float.parseFloat(properties.getProperty("verticalLabel.font.size_"+selectedLanguage))).deriveFont(Font.PLAIN);

		verticalLabel.setFont(font);
		verticalLabel.setText(Translation.getValue(String.class, "vertical_label_text", "THE UNIT IS NOT CONNECTED"));

		font = font.deriveFont(Float.parseFloat(properties.getProperty("control.checkBox.font.size_"+selectedLanguage)));

		checkBoxStep.setFont(font);
		checkBoxStep.setText(Translation.getValue(String.class, "step", "Step")+":");

		DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>();
		defaultComboBoxModel.addElement(Translation.getValue(String.class, "attenuation", "ATTENUATION"));
		comboBoxAttenuation.setModel(defaultComboBoxModel);
		comboBoxAttenuation.setFont(comboBoxAttenuation.getFont().deriveFont(Float.parseFloat(properties.getProperty("control.comboBox.font.size_"+selectedLanguage))));

		infoPanel.refresh();
	}
}
