package irt.tools.panel.subpanel.monitor;

import irt.controller.control.ControllerAbstract;
import irt.controller.monitor.MonitorControllerConverter;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MonitorPanelConverter extends MonitorPanelAbstract {

	public MonitorPanelConverter() {
		super(null, "Monitor", 214, 210);
		
		LED ledMute = new LED(Color.YELLOW, "MUTE");
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		ledMute.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledMute.setBounds(116, 175, 84, 28);
		add(ledMute);
		
		LED ledLock = new LED(Color.GREEN, "LOCK");
		ledLock.setName("Lock");
		ledLock.setForeground(Color.GREEN);
		ledLock.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledLock.setBounds(18, 175, 81, 28);
		add(ledLock);
		
		JLabel lblCpuTemp_1 = new JLabel("CPU Temp:");
		lblCpuTemp_1.setName("");
		lblCpuTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpuTemp_1.setForeground(new Color(153, 255, 255));
		lblCpuTemp_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCpuTemp_1.setBounds(15, 86, 93, 17);
		add(lblCpuTemp_1);
		
		JLabel lblCpuTemp = new JLabel(":");
		lblCpuTemp.setName("CPU Temp");
		lblCpuTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCpuTemp.setForeground(Color.WHITE);
		lblCpuTemp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCpuTemp.setBounds(121, 86, 82, 17);
		add(lblCpuTemp);
		
		JLabel lblUnitTemp_1 = new JLabel("Unit Temp:");
		lblUnitTemp_1.setName("");
		lblUnitTemp_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitTemp_1.setForeground(new Color(153, 255, 255));
		lblUnitTemp_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUnitTemp_1.setBounds(15, 62, 93, 17);
		add(lblUnitTemp_1);
		
		JLabel label_3 = new JLabel("Input Power:");
		label_3.setName("");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		label_3.setForeground(new Color(153, 255, 255));
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 14));
		label_3.setBounds(15, 14, 93, 17);
		add(label_3);
		
		JLabel lblInputPower = new JLabel(":");
		lblInputPower.setName("Input Power");
		lblInputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblInputPower.setForeground(Color.WHITE);
		lblInputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblInputPower.setBounds(121, 14, 82, 17);
		add(lblInputPower);
		
		JLabel lblUnitTemp = new JLabel(":");
		lblUnitTemp.setName("Unit Temp");
		lblUnitTemp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitTemp.setForeground(Color.WHITE);
		lblUnitTemp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUnitTemp.setBounds(121, 62, 82, 17);
		add(lblUnitTemp);
		
		JLabel lblV_2 = new JLabel("-13.2 V:");
		lblV_2.setName("");
		lblV_2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV_2.setForeground(new Color(153, 255, 255));
		lblV_2.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblV_2.setBounds(15, 158, 93, 17);
		add(lblV_2);
		
		JLabel lblV_1 = new JLabel("13.2 V:");
		lblV_1.setName("");
		lblV_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV_1.setForeground(new Color(153, 255, 255));
		lblV_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblV_1.setBounds(15, 134, 93, 17);
		add(lblV_1);
		
		JLabel lblV = new JLabel("5.5 V:");
		lblV.setName("");
		lblV.setHorizontalAlignment(SwingConstants.RIGHT);
		lblV.setForeground(new Color(153, 255, 255));
		lblV.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblV.setBounds(15, 110, 93, 17);
		add(lblV);
		
		JLabel lbl5V5Output = new JLabel(":");
		lbl5V5Output.setName("5.5V");
		lbl5V5Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl5V5Output.setForeground(Color.WHITE);
		lbl5V5Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl5V5Output.setBounds(121, 110, 82, 17);
		add(lbl5V5Output);
		
		JLabel lbl13V2Output = new JLabel(":");
		lbl13V2Output.setName("13.2V");
		lbl13V2Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl13V2Output.setForeground(Color.WHITE);
		lbl13V2Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl13V2Output.setBounds(121, 134, 82, 17);
		add(lbl13V2Output);
		
		JLabel lbl_13V2Output = new JLabel(":");
		lbl_13V2Output.setName("-13.2V");
		lbl_13V2Output.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl_13V2Output.setForeground(Color.WHITE);
		lbl_13V2Output.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lbl_13V2Output.setBounds(121, 158, 82, 17);
		add(lbl_13V2Output);
		
		JLabel lblOutputPower_1 = new JLabel("Output Power:");
		lblOutputPower_1.setName("");
		lblOutputPower_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower_1.setForeground(new Color(153, 255, 255));
		lblOutputPower_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblOutputPower_1.setBounds(15, 38, 93, 17);
		add(lblOutputPower_1);
		
		JLabel lblOutputPower = new JLabel(":");
		lblOutputPower.setName("Output Power");
		lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOutputPower.setForeground(Color.WHITE);
		lblOutputPower.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblOutputPower.setBounds(121, 38, 82, 17);
		add(lblOutputPower);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new MonitorControllerConverter(this);
	}
}
