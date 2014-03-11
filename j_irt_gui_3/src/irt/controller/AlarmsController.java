package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlarmsController extends ControllerAbstract {

	public static final String REDUNDANCY 		= "Redundancy";
	public static final String OTHER 			= "Other";
	public static final String OVER_TEMPERATURE = "Over-Temperature";
	public static final String UNDER_CURRENT2 	= "Under-Current";
	public static final String OVER_CURRENT 	= "Over-Current";
	public static final String PLL_OUT_OF_LOCK2 = "PLL Out Of Lock";

	private static final int PRIORITY = 20;
	public static final Color WARNING_COLOR = new Color(255, 204, 102);

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

	private volatile List<DefaultController> alarmControllers = new ArrayList<>();

	public AlarmsController(LinkHeader linkHeader, JPanel panel) {
		super("AlarmsController", new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_IDS, PacketWork.PACKET_ID_ALARMS_IDs), panel, Style.CHECK_ONCE);
		this.linkHeader = linkHeader;
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return null;
	}

	private void startController(DefaultController controller, JLabel label) {

		Thread t = new Thread(controller, "AlarmsController."+controller.getName()+"-"+new RundomNumber().toString());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		label.setEnabled(true);
	}

	private void setAlarmController(String name, JLabel label, Getter getter) {
		logger.entry("setAlarmOwerTemperatureController(name={}, {}, {})", name, label, getter);
		if(label!=null){
			label.setEnabled(true);
			DefaultController alarmController = new DefaultController(
															name,
															getter,
															Style.CHECK_ALWAYS){

															@Override
															protected ValueChangeListener addGetterValueChangeListener() {
																return null;
															}

															@Override
															protected PacketListener getNewPacketListener() {
																return null;
															}};
			startController(alarmController, label);
			alarmControllers.add(alarmController);
		}
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
					case PLL_OUT_OF_LOCK2:
						lblPllOutOfLock = (JLabel) c;
						break;
					case OVER_CURRENT:
						lblOwerCurrent = (JLabel) c;
						break;
					case UNDER_CURRENT2:
						lblUnderCurrent = (JLabel) c;
						break;
					case OVER_TEMPERATURE:
						lblOwerTemperature = (JLabel) c;
						break;
					case OTHER:
						lblHardware = (JLabel) c;
						break;
					case REDUNDANCY:
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
		synchronized (this) {
			for(DefaultController ac:alarmControllers)
				ac.stop();
		}

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

	@Override
	protected PacketListener getNewPacketListener() {
		return new PacketListener() {
			@Override
			public void packetRecived(Packet packet) {
				PacketWork packetWork = getPacketWork();
				if (	packetWork!=null &&
						packetWork.isAddressEquals(packet) &&
						packet.getHeader().getGroupId()==Packet.IRT_SLCP_PACKET_ID_ALARM)

					new ValueChangeWorker(packet);
			}
		};
	}

	//********************* class ControllerWorker *****************
	private class ValueChangeWorker extends Thread {

		private Packet packet;
		private byte outOfLock		= -1;
		private byte owerCurrent	= -1;
		private byte underCurrent	= -1;
		private byte owerTemperature= -1;
		private byte hardware		= -1;
		private byte redundant		= -1;

		public ValueChangeWorker(Packet packet){
			this.packet = packet;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {

			try {
				PacketHeader header = packet.getHeader();
				if (header.getOption() == 0) {
					short packetId = header.getPacketId();

					Payload payload = packet.getPayload(0);
					if (payload != null)
						switch (packetId) {
						case PacketWork.PACKET_ID_ALARMS_IDs:
							setControllers(payload.getArrayShort());
							break;
						case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
						case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
						case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
						case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
						case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
						case PacketWork.PACKET_ID_ALARMS_SUMMARY:
						case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
							short[] shorts = payload.getArrayShort();
							fillFields(shorts);
						}
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
			logger.exit();
		}

		private void fillFields(short[] source) {
			logger.trace("fillFields(short[] {})", source);

			if (source != null && source.length == 3) {
				byte ool = (byte) (source[2] & 7);

				short alarm = source[0];
				switch (alarm) {
				case PLL_OUT_OF_LOCK:
					if(ool!=outOfLock)
						setAlarm(lblPllOutOfLock, outOfLock=ool);
					break;
				case OWER_CURRENT:
					if(ool!=owerCurrent)
						setAlarm(lblOwerCurrent, owerCurrent=ool);
					break;
				case UNDER_CURRENT:
					if(ool!=underCurrent)
						setAlarm(lblUnderCurrent, underCurrent=ool);
					break;
				case OWER_TEMPERATURE:
					if(ool!=owerTemperature)
						setAlarm(lblOwerTemperature, owerTemperature=ool);
					break;
				case HW_FAULT:
					if(ool!=hardware)
						setAlarm(lblHardware, hardware=ool);
					break;
				case REDUNDANT_FAULT:
					if(ool!=redundant)
						setAlarm(lblRedundant, redundant=ool);
				}
			}
		}

		private void setControllers(short[] source) {

			if(alarmControllers.isEmpty()){
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
					if(lblRedundant!=null)
						lblRedundant.getParent().setVisible(true);
					setAlarmController("Alarm Redundant", lblRedundant, new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, ALARMS_STATUS, PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT, REDUNDANT_FAULT)
					{ @Override public Integer getPriority() { return PRIORITY; }});
				}

			setSend(false);
			}
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
				label.setBackground(WARNING_COLOR);
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
