package irt.tools.panel.subpanel.monitor;
import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorController;
import irt.data.packet.LinkHeader;
import irt.tools.label.LED;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import resources.Translation;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelAbstract {

	private static final float _18 = 18;
	private JLabel labelInputPower;
	private JLabel labelOutputPower;
	private JLabel labelTempereture;
	private LED ledLock;
	private LED ledMute;

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader, "Monitor", 214, 210);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.led.ip.fomt.size", _14));
		labelInputPower = new JLabel(Translation.getValue(String.class, "input_power", "Input Power")+":");
		labelInputPower.setName("");
		labelInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		labelInputPower.setForeground(new Color(153, 255, 255));
		labelInputPower.setFont(font);
		labelInputPower.setBounds(10, 22, 93, 17);
		add(labelInputPower);

		JLabel lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setFont(FONT);
		lblInputPower.setBounds(105, 22, 93, 17);
		add(lblInputPower);

		labelOutputPower = new JLabel(Translation.getValue(String.class, "output_power", "Output Power")+":");
		labelOutputPower.setName("");
		labelOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		labelOutputPower.setForeground(new Color(153, 255, 255));
		labelOutputPower.setFont(font);
		labelOutputPower.setBounds(8, 50, 93, 17);
		add(labelOutputPower);

		JLabel lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setFont(FONT);
		lblOutputPower.setBounds(105, 50, 93, 17);
		add(lblOutputPower);

		labelTempereture = new JLabel(Translation.getValue(String.class, "temperature", "Temperature")+":");
		labelTempereture.setName("");
		labelTempereture.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTempereture.setForeground(new Color(153, 255, 255));
		labelTempereture.setFont(font);
		labelTempereture.setBounds(8, 78, 93, 17);
		add(labelTempereture);

		JLabel lblTemperature = new JLabel(":");
		lblTemperature.setName("Temperature");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.WHITE);
		lblTemperature.setFont(FONT);
		lblTemperature.setBounds(114, 78, 84, 17);
		add(lblTemperature);

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", _18));
		ledLock = new LED(Color.GREEN, Translation.getValue(String.class, "lock", "LOCK"));
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(font);
		ledLock.setBounds(17, 138, 81, 28);
		add(ledLock);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(font);
		ledMute.setBounds(115, 138, 84, 28);
		add(ledMute);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new MonitorController(getLinkHeader(), this) ;
	}

	@Override
	public void refresh() {
		super.refresh();
		font = font.deriveFont(Translation.getValue(Float.class, "monitor.led.ip.fomt.size", _14));
		labelInputPower.setFont(font);
		labelInputPower.setText(Translation.getValue(String.class, "input_power", "Input Power")+":");
		labelOutputPower.setFont(font);
		labelOutputPower.setText(Translation.getValue(String.class, "output_power", "Output Power")+":");
		labelTempereture.setFont(font);
		labelTempereture.setText(Translation.getValue(String.class, "temperature", "Temperature")+":");

		font = font.deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", _18));
		ledLock.setFont(font);
		ledLock.setText(Translation.getValue(String.class, "lock", "LOCK"));
		ledMute.setFont(font);
		ledMute.setText(Translation.getValue(String.class, "mute", "MUTE"));
	}
}
