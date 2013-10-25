package irt.tools.panel.subpanel.monitor;
import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorController;
import irt.controller.translation.Translation;
import irt.data.packet.LinkHeader;
import irt.tools.label.LED;
import irt.tools.panel.head.IrtPanel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelAbstract {

	private final Logger logger = (Logger) LogManager.getLogger();

	private LED ledLock;
	private LED ledMute;
	private JLabel lblInputPowerTxt;
	private JLabel lblOutputPowerTxt;
	private JLabel lblTemperatureTxt;

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader, Translation.getValue(String.class, "monitor", "Monitor"), 214, 210);

		String selectedLanguage = Translation.getSelectedLanguage();
		String propertyToGet = "monitor.leds.font.size_"+selectedLanguage;
		String property = properties.getProperty(propertyToGet);

		if(property==null){
			logger.error("Impossible to to get properties for {}", propertyToGet);
			selectedLanguage = "en_US";
			logger.error("English will be used ({})", selectedLanguage);
			Translation.setLocale(selectedLanguage);
			propertyToGet = "monitor.leds.font.size_"+selectedLanguage;
			property = properties.getProperty(propertyToGet);
		}

		Font font = Translation.getFont().deriveFont(new Float(property))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.leds.font.style_" + selectedLanguage)));

		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);

		int width = (property = properties.getProperty("monitor.led.lock.width_"+selectedLanguage))!=null ? Integer.parseInt(property) : 100;
		int x = (property = properties.getProperty("monitor.led.lock.x_"+selectedLanguage))!=null ? Integer.parseInt(property) : 17;
		int y = (property = properties.getProperty("monitor.led.lock.y_"+selectedLanguage))!=null ? Integer.parseInt(property) : 138;
		logger.debug("ledLock: x={}, y={}, width={}", x, y, width);
		ledLock.setBounds(x, y, width, 28);
		add(ledLock);
		
		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);

		width = (property = properties.getProperty("monitor.led.mute.width_"+selectedLanguage))!=null ? Integer.parseInt(property) : 100;
		x = (property = properties.getProperty("monitor.led.mute.x_"+selectedLanguage))!=null ? Integer.parseInt(property) : 115;
		y = (property = properties.getProperty("monitor.led.mute.y_"+selectedLanguage))!=null ? Integer.parseInt(property) : 138;
		logger.debug("ledMute: x={}, y={}, width={}", x, y, width);
		ledMute.setBounds(x, y, width, 28);
		add(ledMute);

		font = font.deriveFont(new Float(properties.getProperty("monitor.labels.font.size_" + selectedLanguage)))
				.deriveFont(IrtPanel.fontStyle.get(properties.getProperty("monitor.labels.font.style_" + selectedLanguage)));

		JLabel lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setFont(font);
		lblInputPower.setBounds(99, 22, 100, 17);
		add(lblInputPower);

		lblInputPowerTxt = new JLabel(Translation.getValue(String.class, "input_power", "Input Power")+":");
		lblInputPowerTxt.setName("");
		lblInputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPowerTxt.setForeground(new Color(153, 255, 255));
		lblInputPowerTxt.setFont(font);
		lblInputPowerTxt.setBounds(2, 22, 104, 17);
		add(lblInputPowerTxt);

		JLabel lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setFont(font);
		lblOutputPower.setBounds(99, 50, 100, 17);
		add(lblOutputPower);

		lblOutputPowerTxt = new JLabel(Translation.getValue(String.class, "output_power", "Output Power")+":");
		lblOutputPowerTxt.setName("");
		lblOutputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPowerTxt.setForeground(new Color(153, 255, 255));
		lblOutputPowerTxt.setFont(font);
		lblOutputPowerTxt.setBounds(2, 50, 104, 17);
		add(lblOutputPowerTxt);

		JLabel lblTemperature = new JLabel(":");
		lblTemperature.setName("Temperature");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.WHITE);
		lblTemperature.setFont(font);
		lblTemperature.setBounds(99, 78, 100, 17);
		add(lblTemperature);

		lblTemperatureTxt = new JLabel(Translation.getValue(String.class, "temperature", "Temperature")+":");
		lblTemperatureTxt.setName("");
		lblTemperatureTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperatureTxt.setForeground(new Color(153, 255, 255));
		lblTemperatureTxt.setFont(font);
		lblTemperatureTxt.setBounds(2, 78, 104, 17);
		add(lblTemperatureTxt);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new MonitorController(getLinkHeader(), this) ;
	}

	@Override
	public void refresh() {
		super.refresh();

		titledBorder.setTitle(Translation.getValue(String.class, "monitor", "Monitor"));

		String selectedLanguage = Translation.getSelectedLanguage();
		String propertyToGet = "monitor.leds.font.size_"+selectedLanguage;
		String property = properties.getProperty(propertyToGet);

		if(property==null){
			logger.error("Impossible to to get properties for {}", propertyToGet);
			selectedLanguage = "en_US";
			Translation.setLocale(selectedLanguage);
			logger.error("English will be used ({})", selectedLanguage);
			propertyToGet = "monitor.leds.font.size_"+selectedLanguage;
			property = properties.getProperty(propertyToGet);
		}

		Font font = Translation.getFont().deriveFont(new Float(property));

		String muteText = Translation.getValue(String.class, "mute", "MUTE");

		ledMute.setFont(font);
		ledMute.setText(muteText);
		int width = (property = properties.getProperty("monitor.led.mute.width_"+selectedLanguage))!=null ? Integer.parseInt(property) : 100;
		int x = (property = properties.getProperty("monitor.led.mute.x_"+selectedLanguage))!=null ? Integer.parseInt(property) : 115;
		int y = (property = properties.getProperty("monitor.led.mute.y_"+selectedLanguage))!=null ? Integer.parseInt(property) : 138;
		logger.debug("ledMute: x={}, y={}, width={}", x, y, width);
		ledMute.setBounds(x, y, width, 28);

		ledLock.setFont(font);
		width = (property = properties.getProperty("monitor.led.lock.width_"+selectedLanguage))!=null ? Integer.parseInt(property) : 100;
		x = (property = properties.getProperty("monitor.led.lock.x_"+selectedLanguage))!=null ? Integer.parseInt(property) : 17;
		y = (property = properties.getProperty("monitor.led.lock.y_"+selectedLanguage))!=null ? Integer.parseInt(property) : 138;
		logger.debug("ledLock: x={}, y={}, width={}", x, y, width);
		ledLock.setBounds(x, y, width, 28);
		ledLock.setText(Translation.getValue(String.class, "lock".toLowerCase(), "LOCK"));

		font = font.deriveFont(new Float(properties.getProperty("monitor.labels.font.size_"+selectedLanguage)));
		lblInputPowerTxt.setFont(font);
		String string = Translation.getValue(String.class, "input_power", "Input Power")+":";
		lblInputPowerTxt.setText(string);
		lblOutputPowerTxt.setFont(font);
		string = Translation.getValue(String.class, "output_power", "Output Power")+":";
		lblOutputPowerTxt.setText(string);
		lblTemperatureTxt.setFont(font);
		string = Translation.getValue(String.class, "temperature", "Temperature")+":";
		lblTemperatureTxt.setText(string);
	}
}
