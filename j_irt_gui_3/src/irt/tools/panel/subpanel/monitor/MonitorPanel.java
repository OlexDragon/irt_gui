package irt.tools.panel.subpanel.monitor;
import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorController;
import irt.controller.translation.Translation;
import irt.data.packet.LinkHeader;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelAbstract {

	private LED ledLock;
	private LED ledMute;
	private JLabel lblInputPowerTxt;
	private JLabel lblOutputPowerTxt;
	private JLabel lblTemperatureTxt;

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader, Translation.getValue(String.class, "monitor", "Monitor"), 214, 210);

		String selectedLanguage = Translation.getSelectedLanguage();
		Font font = Translation.getFont().deriveFont(new Float(properties.getProperty("monitor.leds.font.size_"+selectedLanguage)));
		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);
		ledLock.setBounds(17, 138, Integer.parseInt(properties.getProperty("monitor.led.lock.width_"+selectedLanguage)), 28);
		add(ledLock);
		
		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);
		ledMute.setBounds(115, 138, Integer.parseInt(properties.getProperty("monitor.led.mute.width_"+selectedLanguage)), 28);
		add(ledMute);

		font = font.deriveFont(new Float(properties.getProperty("monitor.labels.font.size_"+selectedLanguage)));
		
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
		Font font = Translation.getFont().deriveFont(Float.parseFloat(properties.getProperty("monitor.leds.font.size_"+selectedLanguage)));

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
