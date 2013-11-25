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
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlarmsController extends ControllerAbstract {

	private static final int PRIORITY = 20;

	public static final short  PLL_OUT_OF_LOCK 	= 1,
								OWER_CURRENT	= 4,
								UNDER_CURRENT	= 5,
								OWER_TEMPERATURE= 7,
								HW_FAULT		= 10;

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

	private JLabel lblPllOutOffLock;
	private JLabel lblOwerCurrent;
	private JLabel lblUnderCurrent;
	private JLabel lblOwerTemperature;
	private JLabel lblHardware;

	private LinkHeader linkHeader;
	
	private DefaultController alarmController1;
	private DefaultController alarmController2;
	private DefaultController alarmController3;
	private DefaultController alarmController4;
	private DefaultController alarmController5;

	public AlarmsController(LinkHeader linkHeader, JPanel panel) {
		super(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_IDS, PacketWork.PACKET_ID_ALARMS), panel, Style.CHECK_ALWAYS);
		logger.trace("AlarmsController({}, JPanel panel)", linkHeader);

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

				Object source = valueChangeEvent.getSource();
				if(source !=null && source instanceof short[]){
					logger.debug("valueChanged(ValueChangeEvent {})", Arrays.toString((short[])source));
					if(isSend())
						setControllers((short[])source);
					else
						fillFields((short[])source);
				}
			}

			private void fillFields(short[] source) {
				logger.debug("fillFields(Object {})", source);

				byte status = (byte) (source[2]&7);

				short alarm = source[0];
				switch(alarm){
				case PLL_OUT_OF_LOCK:
					setAlarm(lblPllOutOffLock, status);
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
				}
			}

			private void setControllers(short[] source) {
				logger.trace("setControllers(source={})", source);

				setHardwareController();

				for(short sh:source)
					switch(sh){
					case PLL_OUT_OF_LOCK:
						setPllOutOffLockController();
						break;
					case OWER_CURRENT:
						setAlarmOwerCurrentController();
						break;
					case UNDER_CURRENT:
						setAlarmUnderCurrentController();
						break;
					case OWER_TEMPERATURE:
						setAlarmOwerTemperatureController();
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
		};
	}

	private void startController(DefaultController controller, JLabel label) {

		Thread t = new Thread(controller);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		label.setEnabled(true);
	}

	private void setAlarmOwerTemperatureController() {
		logger.trace("setAlarmOwerTemperatureController({})", lblOwerTemperature);
		lblOwerTemperature.setEnabled(true);
		alarmController1 = new DefaultController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE, OWER_TEMPERATURE),
															Style.CHECK_ALWAYS){

															@Override
															protected ValueChangeListener addGetterValueChangeListener() {
																return AlarmsController.this.valueChangeListener;
															}};
		startController(alarmController1, lblOwerTemperature);
	}

	private void setAlarmUnderCurrentController() {
		logger.trace("setAlarmUnderCurrentController({})", lblUnderCurrent);
		lblUnderCurrent.setEnabled(true);
		alarmController2 = new DefaultController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT, UNDER_CURRENT)
														{@Override
														public Integer getPriority() {
															return PRIORITY;
														}}, Style.CHECK_ALWAYS){

			@Override
			protected ValueChangeListener addGetterValueChangeListener() {
				return AlarmsController.this.valueChangeListener;
			}};
		startController(alarmController2, lblUnderCurrent);
	}

	private void setAlarmOwerCurrentController() {
		logger.trace("setAlarmOwerCurrentController({})", lblOwerCurrent);
		lblOwerCurrent.setEnabled(true);
		alarmController3 = new DefaultController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_OWER_CURRENT, OWER_CURRENT)
														{@Override
														public Integer getPriority() {
															return PRIORITY;
														}}, Style.CHECK_ALWAYS){

			@Override
			protected ValueChangeListener addGetterValueChangeListener() {
				return AlarmsController.this.valueChangeListener;
			}};
		startController(alarmController3, lblOwerCurrent);
	}

	private void setPllOutOffLockController() {
		logger.trace("setPllOutOffLockController({})", lblPllOutOffLock);
		lblPllOutOffLock.setEnabled(true);
		alarmController4 = new DefaultController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK, PLL_OUT_OF_LOCK)
														{@Override
														public Integer getPriority() {
															return PRIORITY;
														}}, Style.CHECK_ALWAYS){

			@Override
			protected ValueChangeListener addGetterValueChangeListener() {
				return AlarmsController.this.valueChangeListener;
			}};
		startController(alarmController4, lblPllOutOffLock);
	}

	private void setHardwareController() {
		logger.trace("setHardwareController({})", lblHardware);
		lblHardware.setEnabled(true);
		alarmController5 = new DefaultController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT, HW_FAULT)
														{@Override
														public Integer getPriority() {
															return PRIORITY;
														}}, Style.CHECK_ALWAYS){

			@Override
			protected ValueChangeListener addGetterValueChangeListener() {
				return AlarmsController.this.valueChangeListener;
			}};
		startController(alarmController5, lblHardware);
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
		if(alarmController1!=null)
			alarmController1.setRun(false);
		if(alarmController2!=null)
			alarmController2.setRun(false);
		if(alarmController3!=null)
			alarmController3.setRun(false);
		if(alarmController4!=null)
			alarmController4.setRun(false);
		if(alarmController5!=null)
			alarmController5.setRun(false);
		lblPllOutOffLock = null;
		lblOwerCurrent = null;
		lblUnderCurrent = null;
		lblOwerTemperature = null;
		lblHardware = null;
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
		default:
			name = "Alarm id="+alarm;
		}
		return name;
	}
}
