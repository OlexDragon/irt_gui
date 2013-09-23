package irt.tools.panel.head;

import irt.controller.monitor.MonitorController;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.value.StaticComponents;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;
	public static LED ledRx = StaticComponents.getLedRx();

	public HeadPanel(JFrame target) {
		super(target, 650);
		setSize(607, 74);
		setBackground(new Color(0x3B, 0x4A, 0x8B));
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		ledPowerOn = new LED(Color.GREEN, "POWER ON");
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		ledPowerOn.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledPowerOn.setBounds(48, 22, 137, 30);
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, "ALARM");
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		ledAlarm.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledAlarm.setBounds(207, 22, 100, 30);
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, "MUTE");
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		ledMute.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledMute.setBounds(362, 22, 88, 30);
		add(ledMute);
		
		ledRx.setForeground(new Color(176, 224, 230));
		ledRx.setFont(new Font("Tahoma", Font.BOLD, 18));
		ledRx.setBounds(10, 29, 17, 17);
		add(ledRx);
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
}