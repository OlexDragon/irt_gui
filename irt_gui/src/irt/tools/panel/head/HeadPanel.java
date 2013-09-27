package irt.tools.panel.head;

import irt.controller.GuiController;
import irt.controller.monitor.MonitorController;
import irt.data.Listeners;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import resources.Translation;
import resources.tools.ValueLabel;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	private static final String MUTE = "MUTE";
	private static final String ALARM = "ALARM";
	private static final String POWER_ON = "POWER ON";
	private static final Font LEDS_FONT = new Font("Tahoma", Font.BOLD, 18);
	private static final float FONT_SIZE = 18;
	private static final int MUTE_WIDTH = 88;
	private static final int ALARM_WIDTH = 100;
	private static final int POWER_ONWIDTH = 139;
	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;
	public static LED ledRx = StaticComponents.getLedRx();

	public HeadPanel(JFrame target) {
		super(target, 650);
		setSize(607, 74);
		Color color = new Color(0x3B, 0x4A, 0x8B);
		setBackground(color);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		Font font = null;
		try {

			String fontURL = Translation.getValue(String.class, "resource.font", null);
			font = fontURL==null ? LEDS_FONT : Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResource(fontURL).openStream());
			if(!font.equals(LEDS_FONT))
				font = font.deriveFont(Translation.getValue(Float.class, "font.size", FONT_SIZE));

		} catch (FontFormatException | IOException e) {
			font = LEDS_FONT;
		}
			

		int powerOnWidth = Translation.getValue(Integer.class, "power_on.width", POWER_ONWIDTH);
		int alarmWidth = Translation.getValue(Integer.class, "alarm.width", ALARM_WIDTH);
		int muteWidth = Translation.getValue(Integer.class, "mute.width", MUTE_WIDTH);


		ledPowerOn = new LED(Color.GREEN, Translation.getValue(String.class, "power_on", POWER_ON));
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		ledPowerOn.setFont(font);
		ledPowerOn.setBounds(48, 22, powerOnWidth, 30);
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, Translation.getValue(String.class, "alarm", ALARM));
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		ledAlarm.setFont(font);
		ledAlarm.setBounds(207, 22, alarmWidth, 30);
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", MUTE));
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		ledMute.setFont(font);
		ledMute.setBounds(362, 22, muteWidth, 30);
		add(ledMute);
		
		ledRx.setForeground(new Color(176, 224, 230));
		ledRx.setBounds(10, 29, 17, 17);
		add(ledRx);
		
		ClassPathXmlApplicationContext context =  new ClassPathXmlApplicationContext("resources/translation/translation.xml");
		@SuppressWarnings("unchecked")
		List<ValueLabel> languages = (ArrayList<ValueLabel>) context.getBean("languages");
		ValueLabel[] valueLabels = new ValueLabel[languages.size()];
		DefaultComboBoxModel<ValueLabel> defaultComboBoxModel = new DefaultComboBoxModel<ValueLabel>(languages.toArray(valueLabels));
		JComboBox<ValueLabel> comboBoxLanguage = new JComboBox<ValueLabel>();
		comboBoxLanguage.setName("Language");
		comboBoxLanguage.setFont(UIManager.getFont("CheckBoxMenuItem.acceleratorFont"));
		comboBoxLanguage.setModel(defaultComboBoxModel);
		comboBoxLanguage.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxLanguage.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0;}};}});
		comboBoxLanguage.setForeground(Color.WHITE);
		comboBoxLanguage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboBoxLanguage.setBackground(color.darker().darker());
		comboBoxLanguage.setBounds(530, 50, 91, 17);
		comboBoxLanguage.setMinimumSize(new Dimension(77, 17));
		ValueLabel valueLabel = new ValueLabel();
		valueLabel.setValue(GuiController.getPrefs().get("locate", "en,US"));
		comboBoxLanguage.setSelectedItem(valueLabel);
		add(comboBoxLanguage);
		context.close();
	}

	public void setPowerOn(boolean isOn) {
		ledPowerOn.setOn(isOn);
		if(!isOn){
			ledMute.setOn(isOn);
			ledAlarm.setOn(isOn);
		}
	}

	public ValueChangeListener getStatusChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {

				Object source = valueChangeEvent.getSource();
				int status;

				if(source instanceof Long)
					status=((Long)source).intValue();
				else
					status = (int) source;

				ledMute.setOn((status&MonitorController.MUTE)>0);
				ledAlarm.setOn((status&MonitorController.LOCK)==0);
			}
		};
	}

	public void refresh() {

		Font font = null;
		try {
			String fontURL = Translation.getValue(String.class, "resource.font", null);
			font = fontURL==null ? LEDS_FONT : Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResource(fontURL).openStream());
			if(!font.equals(LEDS_FONT))
				font = font.deriveFont(Translation.getValue(Float.class, "font.size", FONT_SIZE));
		} catch (FontFormatException | IOException e) {}

		if(font==null)
			font = LEDS_FONT;

		int powerOnWidth = Translation.getValue(Integer.class, "power_on.width", POWER_ONWIDTH);
		int alarmWidth = Translation.getValue(Integer.class, "alarm.width", ALARM_WIDTH);
		int muteWidth = Translation.getValue(Integer.class, "mute.width", MUTE_WIDTH);

		ledPowerOn.setSize(powerOnWidth, ledPowerOn.getHeight());
		ledPowerOn.setFont(font);
		ledPowerOn.setText(Translation.getValue(String.class, "power_on", POWER_ON));

		ledAlarm.setSize(alarmWidth, ledAlarm.getHeight());
		ledAlarm.setFont(font);
		ledAlarm.setText(Translation.getValue(String.class, "alarm", ALARM));

		ledMute.setSize(muteWidth, ledMute.getHeight());
		ledMute.setFont(font);
		ledMute.setText(Translation.getValue(String.class, "mute", MUTE));
	}
}