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

	private TitledBorder monitorTitledBorder;
	private TitledBorder controlTitledBorder;
	private ImageButton btnStoreConfig;
	private JCheckBox checkBoxStep;
	private JComboBox<String> cbLoSelect;
	private JComboBox<String> comboBoxAttenuation;
	private InfoPanel infoPanel;
	private JLabel lblDbm;
	private JTabbedPane tabbedPane;
	private JLabel lblSave;

	public DemoPanel() {
		super(Translation.getValue(String.class, "vertical_label_text", "THE UNIT IS NOT CONNECTED"), 0, 0, 0, 0, 0);

		JPanel monitorPanel = new JPanel();
		Font font = Translation.getFont();
		monitorTitledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "monitor", "Monitor"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font ,
				Color.WHITE);
		monitorPanel.setBorder(monitorTitledBorder);
		monitorPanel.setOpaque(false);
		monitorPanel.setBounds(10, 12, 214, 210);
		monitorPanel.setLayout(null);
		userPanel.add(monitorPanel);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBounds(10, 233, 214, 150);
		controlTitledBorder = new TitledBorder(
				UIManager.getBorder("TitledBorder.border"),
				Translation.getValue(String.class, "control", "Control"),
				TitledBorder.LEADING,
				TitledBorder.TOP,
				font,
				Color.WHITE);

		controlPanel.setBorder(controlTitledBorder);
		controlPanel.setOpaque(false);
		controlPanel.setBounds(10, 225, 214, 180);
		controlPanel.setLayout(null);
		userPanel.add(controlPanel);

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		font = font.deriveFont(Translation.getValue(Float.class, "verticalLabel.font.size", 18f)).deriveFont(Font.PLAIN);

		verticalLabel.setFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", 14f))
				.deriveFont(Translation.getValue(Integer.class, "monitor.leds.font.style", Font.BOLD));

		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);

		int width = Translation.getValue(Integer.class, "monitor.led.lock.width", 100);
		int x = Translation.getValue(Integer.class, "monitor.led.lock.x", 17);
		int y = Translation.getValue(Integer.class, "monitor.led.lock.y", 138);
		ledLock.setBounds(x, y, width, 28);
		monitorPanel.add(ledLock);

		ledMute = new LED(Color.YELLOW, muteText);
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);
		width = Translation.getValue(Integer.class, "monitor.led.mute.width", 100);
		x = Translation.getValue(Integer.class, "monitor.led.mute.x", 115);
		y = Translation.getValue(Integer.class, "monitor.led.mute.y", 138);
		ledMute.setBounds(x, y, width, 28);
		monitorPanel.add(ledMute);

		font = font.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 14f))
				.deriveFont(Font.BOLD);

		lblMute = new JLabel(muteText);
		lblMute.setName("Label Mute");
		lblMute.setHorizontalAlignment(SwingConstants.LEFT);
		lblMute.setForeground(Color.YELLOW);
		lblMute.setFont(font);
		x = Translation.getValue(Integer.class, "control.label.mute.x", 48);
		y = Translation.getValue(Integer.class, "control.label.mute.y", 107);
		width = Translation.getValue(Integer.class, "control.label.mute.width", 93);
		lblMute.setBounds(x, y, width, 20);
		controlPanel.add(lblMute);

		btnMute = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/power-red.png")).getImage());
		btnMute.setName("Button Mute");
		btnMute.setToolTipText(muteText);
		x = Translation.getValue(Integer.class, "control.button.mute.x", 14);
		y = Translation.getValue(Integer.class, "control.button.mute.y", 101);
		int size = Translation.getValue(Integer.class, "control.buttons.size", 33);
		btnMute.setBounds(x, y, size, size);
		controlPanel.add(btnMute);

		btnStoreConfig = new ImageButton(new ImageIcon(IrtGui.class.getResource("/irt/irt_gui/images/whitehouse_button.png")).getImage());
		btnStoreConfig.setName("Store");
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));
		x = Translation.getValue(Integer.class, "control.button.save.x", 118);
		y = Translation.getValue(Integer.class, "control.button.save.y", 101);
		btnStoreConfig.setBounds(x, y, size, size);
		controlPanel.add(btnStoreConfig);

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
		controlPanel.add(textField);

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
		comboBoxAttenuation.setFont(comboBoxAttenuation.getFont().deriveFont(Translation.getValue(Float.class, "control.comboBox.font.size", 12f)));
		controlPanel.add(comboBoxAttenuation);

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
		controlPanel.add(textField_1);
		
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
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setOpaque(false);
		tabbedPane.setBounds(10, 123, 286, 296);
		extraPanel.add(tabbedPane);

		JLabel lblLogo = new ImageLabel(
				new ImageIcon(IrtGui.class.getResource(
						IrtPanel.properties.getProperty("company_logo_"+IrtPanel.companyIndex))
				),"");
		tabbedPane.addTab(IrtPanel.properties.getProperty("company_name_"+IrtPanel.companyIndex), null, lblLogo, null);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.labels.font.size", 12f))
				.deriveFont(Translation.getValue(Integer.class, "monitor.labels.font.style", Font.PLAIN));

		lblInputPowerText = new JLabel(Translation.getValue(String.class, "input_power", "Input Power")+":");
		lblInputPowerText.setName("");
		lblInputPowerText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPowerText.setForeground(new Color(153, 255, 255));
		lblInputPowerText.setFont(font);
		lblInputPowerText.setBounds(5, 36, 112, 17);
		monitorPanel.add(lblInputPowerText);
		
		lblOutputPowerText = new JLabel(Translation.getValue(String.class, "output_power", "Output Power")+":");
		lblOutputPowerText.setName("");
		lblOutputPowerText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPowerText.setForeground(new Color(153, 255, 255));
		lblOutputPowerText.setFont(font);
		lblOutputPowerText.setBounds(5, 64, 112, 17);
		monitorPanel.add(lblOutputPowerText);
		
		lblTemperatureText = new JLabel(Translation.getValue(String.class, "temperature", "Temperature")+":");
		lblTemperatureText.setName("");
		lblTemperatureText.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperatureText.setForeground(new Color(153, 255, 255));
		lblTemperatureText.setFont(font);
		lblTemperatureText.setBounds(5, 92, 112, 17);
		monitorPanel.add(lblTemperatureText);

//		font = new Font("Tahoma", Font.PLAIN, 14);

		lblDbm = new JLabel("-25 "+Translation.getValue(String.class, "dbm", " dBm"));
		lblDbm.setName("Input Power");
		lblDbm.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm.setForeground(Color.WHITE);
		lblDbm.setFont(font);
		lblDbm.setBounds(118, 36, 82, 17);
		monitorPanel.add(lblDbm);

		font = font.deriveFont(Translation.getValue(Float.class, "control.checkBox.font.size", 12f));

		checkBoxStep = new JCheckBox(Translation.getValue(String.class, "step", "Step")+":");
		checkBoxStep.setEnabled(false);
		checkBoxStep.setOpaque(false);
		checkBoxStep.setForeground(Color.WHITE);
		checkBoxStep.setFont(font);
		checkBoxStep.setBounds(13, 68, 65, 23);
		controlPanel.add(checkBoxStep);
		
		JLabel lblDbm_1 = new JLabel("40 "+Translation.getValue(String.class, "dbm", " dBm"));
		lblDbm_1.setName("Output Power");
		lblDbm_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDbm_1.setForeground(Color.WHITE);
		lblDbm_1.setFont(font);
		lblDbm_1.setBounds(107, 64, 93, 17);
		monitorPanel.add(lblDbm_1);
		
		JLabel lblC = new JLabel("46 C");
		lblC.setName("Temperature");
		lblC.setHorizontalAlignment(SwingConstants.RIGHT);
		lblC.setForeground(Color.WHITE);
		lblC.setFont(font);
		lblC.setBounds(116, 92, 84, 17);
		monitorPanel.add(lblC);

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
		controlPanel.add(cbLoSelect);

		NetworkPanel networkPanel = new NetworkPanel(null);
		tabbedPane.addTab("network", null, networkPanel, null);

		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			String title = tabbedPane.getTitleAt(i);
			String value = Translation.getValue(String.class, title, null);
			if(value!=null){
				JLabel label = new JLabel(value);
				label.setName(title);
				label.setFont(Translation.getFont().deriveFont(12f));
				tabbedPane.setTabComponentAt(i, label);
			}
		}
		
		font = font.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);

		lblSave = new JLabel(Translation.getValue(String.class, "save", "SAVE"));
		lblSave.setHorizontalAlignment(SwingConstants.LEFT);
		lblSave.setForeground(Color.YELLOW);
		lblSave.setFont(font);
		x = Translation.getValue(Integer.class, "control.label.save.x", 153);
		y = Translation.getValue(Integer.class, "control.label.save.y", 107);
		width = Translation.getValue(Integer.class, "control.label.save.width", 61);
		lblSave.setBounds(x, y, width, 20);
		controlPanel.add(lblSave);
		((JLabel)cbLoSelect.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public void refresh() {
		super.refresh();

		monitorTitledBorder.setTitle(Translation.getValue(String.class, "monitor", "Monitor"));
		Font font =Translation.getFont();
		monitorTitledBorder.setTitleFont(font);
		controlTitledBorder.setTitle(Translation.getValue(String.class, "control", "Control"));
		controlTitledBorder.setTitleFont(font);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", 12f))
				.deriveFont(Translation.getValue(Integer.class, "monitor.leds.font.style", Font.PLAIN));

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		ledLock.setFont(font);
		int width = Translation.getValue(Integer.class, "monitor.led.lock.width", 100);
		int x = Translation.getValue(Integer.class, "monitor.led.lock.x", 17);
		int y = Translation.getValue(Integer.class, "monitor.led.lock.y", 138);
		ledLock.setBounds(x, y, width, 28);
		ledLock.setText(Translation.getValue(String.class, "lock", "LOCK"));

		ledMute.setFont(font);
		ledMute.setText(muteText);
		width = Translation.getValue(Integer.class, "monitor.led.mute.width", 100);
		x = Translation.getValue(Integer.class, "monitor.led.mute.x", 115);
		y = Translation.getValue(Integer.class, "monitor.led.mute.y", 138);
		ledMute.setBounds(x, y, width, 28);

		font = font.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);

		lblMute.setText(muteText);
		lblMute.setFont(font);
		x = Translation.getValue(Integer.class, "control.label.mute.x", 48);
		y = Translation.getValue(Integer.class, "control.label.mute.y", 107);
		width = Translation.getValue(Integer.class, "control.label.mute.width", 93);
		lblMute.setBounds(x, y, width, 20);

		lblSave.setText(Translation.getValue(String.class, "save", "SAVE"));
		lblSave.setFont(font);
		x = Translation.getValue(Integer.class, "control.label.save.x", 153);
		y = Translation.getValue(Integer.class, "control.label.save.y", 107);
		width = Translation.getValue(Integer.class, "control.label.save.width", 61);
		lblSave.setBounds(x, y, width, 20);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.labels.font.size", 12f))
				.deriveFont(Translation.getValue(Integer.class, "monitor.labels.font.style", Font.PLAIN));

		lblInputPowerText.setFont(font);
		lblInputPowerText.setText(Translation.getValue(String.class, "input_power", "Input Power")+":");

		lblOutputPowerText.setFont(font);
		lblOutputPowerText.setText(Translation.getValue(String.class, "output_power", "Output Power")+":");

		lblTemperatureText.setFont(font);
		lblTemperatureText.setText(Translation.getValue(String.class, "temperature", "Temperature")+":");

		x = Translation.getValue(Integer.class, "control.button.mute.x", 14);
		y = Translation.getValue(Integer.class, "control.button.mute.y", 101);
		int size = Translation.getValue(Integer.class, "control.buttons.size", 33);
		btnMute.setBounds(x, y, size, size);
		btnMute.setToolTipText(muteText);

		x = Translation.getValue(Integer.class, "control.button.save.x", 118);
		y = Translation.getValue(Integer.class, "control.button.save.y", 101);
		btnStoreConfig.setBounds(x, y, size, size);
		btnStoreConfig.setToolTipText(Translation.getValue(String.class, "store_config", "Store Config"));

		font = font.deriveFont(Translation.getValue(Float.class, "verticalLabel.font.size", 12f)).deriveFont(Font.PLAIN);

		verticalLabel.setFont(font);
		verticalLabel.setText(Translation.getValue(String.class, "vertical_label_text", "THE UNIT IS NOT CONNECTED"));

		font = font.deriveFont(Translation.getValue(Float.class, "control.checkBox.font.size", 12f));

		checkBoxStep.setFont(font);
		checkBoxStep.setText(Translation.getValue(String.class, "step", "Step")+":");

		DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<String>();
		defaultComboBoxModel.addElement(Translation.getValue(String.class, "attenuation", "ATTENUATION"));
		comboBoxAttenuation.setModel(defaultComboBoxModel);
		comboBoxAttenuation.setFont(comboBoxAttenuation.getFont().deriveFont(Translation.getValue(Float.class, "control.comboBox.font.size", 12f)));

		infoPanel.refresh();

		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			String title = tabbedPane.getTitleAt(i);
			String value = Translation.getValue(String.class, title, null);
			if(value!=null){
				JLabel label = new JLabel(value);
				label.setName(title);
				label.setFont(Translation.getFont().deriveFont(12f));
				tabbedPane.setTabComponentAt(i, label);
			}
		}
	}
}
