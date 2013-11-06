package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.Getter;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlarmsController extends ControllerAbstract {

	public static final byte 	ALARMS_NUMBER 			= 1,
								ALARMS_IDS				= 2,
								ALARMS_SUMMARY_STATUS	= 3,
								ALARMS_STATUS			= 5,
								ALARMS_DESCRIPTION		= 6,
								ALARM_NAME				= 7;

	private JLabel lblPllOutOffLock;
	private JLabel lblOwerCurrent;
	private JLabel lblUnderCurrent;
	private JLabel lblOwerTemperature;

	public AlarmsController(LinkHeader linkHeader, JPanel panel) {
		super(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_IDS, PacketWork.PACKET_ID_ALARMS), panel, Style.CHECK_ALWAYS);
		logger.trace("AlarmsController({}, JPanel panel)", linkHeader);
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.trace(valueChangeEvent);

				setNoAlarms();

				Object source = valueChangeEvent.getSource();
				if(source instanceof short[]){
					for(short sh:(short[])source)
						switch(sh){
						case 1:
							setAlarm(lblPllOutOffLock);
							break;
						case 4:
							setAlarm(lblOwerCurrent);
							break;
						case 5:
							setAlarm(lblUnderCurrent);
							break;
						case 7:
							setAlarm(lblOwerTemperature);
						}
				}
			}

			private void setNoAlarms() {
				lblPllOutOffLock.setBackground(new Color(255, 255, 0));
				lblPllOutOffLock.setText("No Alarm");

				lblOwerCurrent.setBackground(new Color(255, 255, 0));
				lblOwerCurrent.setText("No Alarm");

				lblUnderCurrent.setBackground(new Color(255, 255, 0));
				lblUnderCurrent.setText("No Alarm");

				lblOwerTemperature.setBackground(new Color(255, 255, 0));
				lblOwerTemperature.setText("No Alarm");
			}

			private void setAlarm(JLabel label) {
				label.setBackground(Color.RED);
				label.setText("Alarm");
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		logger.trace("setComponent({})", component);
		boolean isSet = true;

		if (component instanceof JPanel) {
			JPanel p = (JPanel)component;

			for (Component c : p.getComponents()) {
				String name = c.getName();
				if (name != null)
					switch (name) {
					case "PLL Out Off Lock":
						lblPllOutOffLock = (JLabel) c;
						break;
					case "Ower-Current":
						lblOwerCurrent = (JLabel) c;
						break;
					case "Under-Current":
						lblUnderCurrent = (JLabel) c;
						break;
					case "Ower-Temperature":
						lblOwerTemperature = (JLabel) c;
						break;
					default:
						isSet = false;
						logger.warn("Net Used: {}", name);
					}
			}
		}

	return isSet;
	}

}
