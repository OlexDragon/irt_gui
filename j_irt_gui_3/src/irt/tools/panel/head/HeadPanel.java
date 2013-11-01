package irt.tools.panel.head;

import irt.controller.monitor.MonitorController;
import irt.controller.translation.Translation;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final Color BACKGROUND_COLOR = new Color(0x3B, 0x4A, 0x8B);
	public static LED ledRx = StaticComponents.getLedRx();
	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;

	private static Properties properties;

	public HeadPanel(JFrame target) {
		super(target, Translation.getValue(Integer.class, "headPanel.max_width", 650));
		setSize(Translation.getValue(Integer.class, "headPanel.width", 650), Translation.getValue(Integer.class, "headPanel.height", 74));
		setBackground(BACKGROUND_COLOR);
		setCorner(35);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		String selectedLanguage = Translation.getSelectedLanguage();

		logger.trace("selectedLanguage ={}", selectedLanguage);
		ledPowerOn = new LED(Color.GREEN, Translation.getValue(String.class, "power_on", "POWER ON"));
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		Font font = Translation.getFont();
		ledPowerOn.setFont(font);
		Rectangle rectangle = Translation.getValue(Rectangle.class, "headPanel.led_powerOn_bounds", new Rectangle());
		ledPowerOn.setBounds(rectangle);
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		ledAlarm.setFont(font);
		rectangle = Translation.getValue(Rectangle.class, "headPanel.led_alarm_bounds", new Rectangle());
		ledAlarm.setBounds(rectangle);
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		ledMute.setFont(font);
		rectangle = Translation.getValue(Rectangle.class, "headPanel.led_mute_bounds", new Rectangle());
		ledMute.setBounds(rectangle);
		add(ledMute);
		
		ledRx.setBounds(10, 29, 17, 17);
		add(ledRx);
	}

	private static Properties getProperties() {
		if(properties==null){

			properties = new Properties();
			try {
				properties.load(HeadPanel.class.getResourceAsStream("HeadPanel.properties"));
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		return properties;
	}

	public static String getProperty(String key) {
		return getProperties().getProperty(key);
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

		ledPowerOn.setText(Translation.getValue(String.class, "power_on", "POWER ON"));
		Font font = Translation.getFont();
		ledPowerOn.setFont(font );
		Rectangle rectangle = Translation.getValue(Rectangle.class, "headPanel.led_powerOn_bounds", new Rectangle());
		ledPowerOn.setBounds(rectangle);

		ledAlarm.setText(Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setFont(font);
		rectangle = Translation.getValue(Rectangle.class, "headPanel.led_alarm_bounds", new Rectangle());
		ledAlarm.setBounds(rectangle);

		ledMute.setText(Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setFont(font);
		rectangle = Translation.getValue(Rectangle.class, "headPanel.led_mute_bounds", new Rectangle());
		ledMute.setBounds(rectangle);
	}
}