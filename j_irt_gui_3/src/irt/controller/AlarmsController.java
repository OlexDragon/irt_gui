package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlarmsController extends ControllerAbstract {

	private static final int PRIORITY = 20;

	public static final short  PLL_OUT_OF_LOCK 	= 1,
								OWER_CURRENT	= 4,
								UNDER_CURRENT	= 5,
								OWER_TEMPERATURE= 7,
								HW_FAULT		= 10,
								REDUNDANT_FAULT = 11;

	public static final byte 	ALARMS_NUMBER 			= 1,
								ALARMS_IDS				= 2,
								ALARMS_SUMMARY_STATUS	= 3,
								ALARMS_STATUS			= 5,
								ALARMS_DESCRIPTION		= 6,
								ALARM_NAME				= 7;

	public static final byte 	ALARMS_STATUS_NO_ALARM	= 0,
								ALARMS_STATUS_INFO		= 1,
								ALARMS_STATUS_WARNING	= 2,
								ALARMS_STATUS_MINOR		= 3,
								ALARMS_STATUS_ALARM		= 4,
								ALARMS_STATUS_FAULT		= 5;

	private JLabel lblPllOutOfLock;
	private JLabel lblOwerCurrent;
	private JLabel lblUnderCurrent;
	private JLabel lblOwerTemperature;
	private JLabel lblHardware;
	private JLabel lblRedundant;

	private LinkHeader linkHeader;

	private List<DefaultController> alarmControllers = new ArrayList<>();
//	private DefaultController alarmController1;
//	private DefaultController alarmController2;
//	private DefaultController alarmController3;
//	private DefaultController alarmController4;
//	private DefaultController alarmController5;
//	private DefaultController alarmController6;

	public AlarmsController(LinkHeader linkHeader, JPanel panel) {
		super("AlarmsController", new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_IDS, PacketWork.PACKET_ID_ALARMS), panel, Style.CHECK_ALWAYS);
		this.linkHeader = linkHeader;
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.debug("valueChanged(ValueChangeEvent {})", valueChangeEvent);
				new ValueChangeWorker(valueChangeEvent);
			}
		};
	}

	private void startController(DefaultController controller, JLabel label) {

		Thread t = new Thread(controller, controller.getName());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		label.setEnabled(true);
	}

	private void setAlarmController(String name, JLabel label, Getter getter) {
		logger.entry("setAlarmOwerTemperatureController(name={}, {}, {})", name, label, getter);
		label.setEnabled(true);
		DefaultController alarmController = new DefaultController(name, getter,
															Style.CHECK_ALWAYS){

															@Override
															protected ValueChangeListener addGetterValueChangeListener() {
																return AlarmsController.this.valueChangeListener;
															}};
		startController(alarmController, label);
		alarmControllers.add(alarmController);
		logger.exit();
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
						lblPllOutOfLock = (JLabel) c;
						break;
					case "Over-Current":
						lblOwerCurrent = (JLabel) c;
						break;
					case "Under-Current":
						lblUnderCurrent = (JLabel) c;
						break;
					case "Over-Temperature":
						lblOwerTemperature = (JLabel) c;
						break;
					case "Other":
						lblHardware = (JLabel) c;
						break;
					case "Redundant":
						lblRedundant = (JLabel) c;
						break;
					default:
						isSet = false;
						logger.warn("Net Used: {}", name);
					}
			}
		}

	return isSet;
	}

	@Override
	protected void clear() {
		for(DefaultController ac:alarmControllers)
			ac.setRun(false);

		lblPllOutOfLock = null;
		lblOwerCurrent = null;
		lblUnderCurrent = null;
		lblOwerTemperature = null;
		lblHardware = null;
		lblRedundant = null;
		super.clear();
	}

	public static String alarmStatusToString(byte status){
		String alarmStr = null;
		status = (byte) (status&7);
		switch(status){
		case ALARMS_STATUS_ALARM:
			alarmStr = "Alarm";
			break;
		case ALARMS_STATUS_FAULT:
			alarmStr = "Fault";
			break;
		case ALARMS_STATUS_INFO:
			alarmStr = "Info";
			break;
		case ALARMS_STATUS_MINOR:
			alarmStr = "Minor";
			break;
		case ALARMS_STATUS_NO_ALARM:
			alarmStr = "No Alarm";
			break;
		case ALARMS_STATUS_WARNING:
			alarmStr = "Warning";
			break;
		default:
			alarmStr = "Alarm status = "+status;
		}
		return alarmStr;
	}

	public static String getAlarmName(short alarm) {
		String name;
		switch(alarm){
		case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
		case PLL_OUT_OF_LOCK:
			name = "Pll Out of Lock";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
		case OWER_CURRENT:
			name = "Ower Current";
			break;
		case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
		case UNDER_CURRENT:
			name = "Under Current";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
		case OWER_TEMPERATURE:
			name = "Ower Temperature";
			break;
		case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
		case HW_FAULT:
			name = "HW Fault";
			break;
		case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
		case REDUNDANT_FAULT:
			name = "Redundant Fault";
			break;
		default:
			name = "Alarm id="+alarm;
		}
		return name;
	}

	//********************* class ControllerWorker *****************
	private class ValueChangeWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ValueChangeWorker(ValueChangeEvent valueChangeEvent){
			logger.entry(valueChangeEvent);
			setDaemon(true);
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			start();
			logger.exit();
		}

		@Override
		public void run() {
			Object source = valueChangeEvent.getSource();
			logger.entry(source);
			if(source !=null && source instanceof short[]){
				logger.debug("valueChanged(ValueChangeEvent {})", Arrays.toString((short[])source));
				if(isSend())
					setControllers((short[])source);
				else
					fillFields((short[])source);
			}
			logger.exit();
		}

		private void fillFields(short[] source) {
			logger.debug("fillFields(Object {})", source);

			byte status = (byte) (source[2]&7);

			short alarm = source[0];
			switch(alarm){
			case PLL_OUT_OF_LOCK:
				setAlarm(lblPllOutOfLock, status);
				break;
			case OWER_CURRENT:
				setAlarm(lblOwerCurrent, status);
				break;
			case UNDER_CURRENT:
				setAlarm(lblUnderCurrent, status);
				break;
			case OWER_TEMPERATURE:
				setAlarm(lblOwerTemperature, status);
				break;
			case HW_FAULT:
				setAlarm(lblHardware, status);
				break;
			case REDUNDANT_FAULT:
				setAlarm(lblRedundant, status);
			}
		}

		private void setControllers(short[] source) {
			logger.trace("setControllers(source={})", source);

			setAlarmController("Alarm Hardware Fault", lblHardware, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT, HW_FAULT)
			{ @Override public Integer getPriority() { return PRIORITY; }});

			for(short sh:source)
				switch(sh){
				case PLL_OUT_OF_LOCK:
					setAlarmController("Alarm PLL out of lock", lblPllOutOfLock, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK, PLL_OUT_OF_LOCK)
					{ @Override public Integer getPriority() { return PRIORITY; }});
					break;
				case OWER_CURRENT:
					setAlarmController("Alarm Ower Current", lblOwerCurrent, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_OWER_CURRENT, OWER_CURRENT)
					{ @Override public Integer getPriority() { return PRIORITY; }});
					break;
				case UNDER_CURRENT:
					setAlarmController("Alarm Under current", lblUnderCurrent, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT, UNDER_CURRENT)
					{ @Override public Integer getPriority() { return PRIORITY; }});
					break;
				case OWER_TEMPERATURE:
					setAlarmController("Alarm Ower Temperature", lblOwerTemperature, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE, OWER_TEMPERATURE)
					{ @Override public Integer getPriority() { return PRIORITY; }});
					break;
				case REDUNDANT_FAULT:
					lblRedundant.getParent().setVisible(true);
					setAlarmController("Alarm Redundant", lblRedundant, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT, REDUNDANT_FAULT)
					{ @Override public Integer getPriority() { return PRIORITY; }});
				}

			setSend(false);
		}

		private void setAlarm(JLabel label, byte status) {
			logger.trace("status={}", status);
			switch(status){
			case ALARMS_STATUS_INFO:
			case ALARMS_STATUS_NO_ALARM:
				label.setBackground(new Color(46, 139, 87));
				label.setForeground(Color.YELLOW);
				label.setText(Translation.getValue(String.class, "no_alarm", "No Alarm"));
				break;
			case ALARMS_STATUS_WARNING:
			case ALARMS_STATUS_MINOR:
				label.setBackground(new Color(255, 204, 102));
				label.setForeground(Color.BLACK);
				label.setText(Translation.getValue(String.class, "warning", "Warning"));
				break;
			case ALARMS_STATUS_ALARM:
			case ALARMS_STATUS_FAULT:
				label.setBackground(Color.RED);
				label.setForeground(Color.YELLOW);
				label.setText(Translation.getValue(String.class, "alarm", "Alarm"));
			}
		}
	}
}
