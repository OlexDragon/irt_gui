package irt.tools.panel.subpanel.monitor;
import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorController;
import irt.data.packet.LinkHeader;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class MonitorPanel extends MonitorPanelAbstract {

	public MonitorPanel(LinkHeader linkHeader) {
		super(linkHeader, "Monitor", 214, 210);

		LED ledLock = new LED(Color.GREEN, "LOCK");
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledLock.setBounds(17, 138, 81, 28);
		add(ledLock);
		
		LED led_1 = new LED(Color.YELLOW, "MUTE");
		led_1.setName("Mute");
		led_1.setForeground(Color.GREEN);
		led_1.setFont(new Font("Tahoma", Font.BOLD, 18));
		led_1.setBounds(115, 138, 84, 28);
		add(led_1);
		
		JLabel lblInputPower_1 = new JLabel("Input Power:");
		lblInputPower_1.setName("");
		lblInputPower_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower_1.setForeground(new Color(153, 255, 255));
		lblInputPower_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblInputPower_1.setBounds(10, 22, 93, 17);
		add(lblInputPower_1);
		
		JLabel lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblInputPower.setBounds(105, 22, 93, 17);
		add(lblInputPower);

		JLabel lblOutputPower_1 = new JLabel("Output Power:");
		lblOutputPower_1.setName("");
		lblOutputPower_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower_1.setForeground(new Color(153, 255, 255));
		lblOutputPower_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblOutputPower_1.setBounds(8, 50, 93, 17);
		add(lblOutputPower_1);

		JLabel lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblOutputPower.setBounds(105, 50, 93, 17);
		add(lblOutputPower);

		JLabel lblTemperature_1 = new JLabel("Temperature:");
		lblTemperature_1.setName("");
		lblTemperature_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature_1.setForeground(new Color(153, 255, 255));
		lblTemperature_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTemperature_1.setBounds(8, 78, 93, 17);
		add(lblTemperature_1);

		JLabel lblTemperature = new JLabel(":");
		lblTemperature.setName("Temperature");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.WHITE);
		lblTemperature.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTemperature.setBounds(114, 78, 84, 17);
		add(lblTemperature);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new MonitorController(getLinkHeader(), this) ;
	}
}
