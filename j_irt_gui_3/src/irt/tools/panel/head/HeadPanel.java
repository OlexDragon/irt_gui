package irt.tools.panel.head;

import irt.controller.monitor.MonitorController;
import irt.controller.translation.Translation;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	public static final Color BACKGROUND_COLOR = new Color(0x3B, 0x4A, 0x8B);
	public static LED ledRx = StaticComponents.getLedRx();
	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;

	public static final Properties properties = getProperties();

	public HeadPanel(JFrame target) {
		super(target, Integer.parseInt(properties.get("max_width").toString()));
		setSize(Integer.parseInt(properties.get("width").toString()), Integer.parseInt(properties.getProperty("height")));
		setBackground(BACKGROUND_COLOR);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		String selectedLanguage = Translation.getSelectedLanguage();
		
		ledPowerOn = new LED(Color.GREEN, Translation.getValue(String.class, "power_on", "POWER ON"));
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		Font font = Translation.getFont();
		ledPowerOn.setFont(font);
		String[] bounds = properties.get("led_powerOn_bounds_"+selectedLanguage).toString().split(",");
		ledPowerOn.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		ledAlarm.setFont(font);
		bounds = properties.get("led_alarm_bounds_"+selectedLanguage).toString().split(",");
		ledAlarm.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		ledMute.setFont(font);
		bounds = properties.get("led_mute_bounds_"+selectedLanguage).toString().split(",");
		ledMute.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));
		add(ledMute);
		
		ledRx.setBounds(10, 29, 17, 17);
		add(ledRx);
	}

	private static Properties getProperties() {
		Properties properties = new Properties();
		try {
			properties.load(HeadPanel.class.getResourceAsStream("HeadPanel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
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

		String selectedLanguage = Translation.getSelectedLanguage();

		ledPowerOn.setText(Translation.getValue(String.class, "power_on", "POWER ON"));
		Font font = Translation.getFont();
		ledPowerOn.setFont(font );
		String[] bounds = properties.get("led_powerOn_bounds_"+selectedLanguage).toString().split(",");
		ledPowerOn.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));

		ledAlarm.setText(Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setFont(font);
		bounds = properties.get("led_alarm_bounds_"+selectedLanguage).toString().split(",");
		ledAlarm.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));

		ledMute.setText(Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setFont(font);
		bounds = properties.get("led_mute_bounds_"+selectedLanguage).toString().split(",");
		ledMute.setBounds(Integer.parseInt(bounds[0]),
									Integer.parseInt(bounds[1]),
									Integer.parseInt(bounds[2]),
									Integer.parseInt(bounds[3]));
	}
}