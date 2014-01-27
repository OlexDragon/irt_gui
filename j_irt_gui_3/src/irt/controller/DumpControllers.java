package irt.controller;

import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.head.UnitsContainer;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class DumpControllers extends ValueChangeListenerClass {

//	private static final int MAX_FILE_QUANTITY = 50;
//	private static final int MAX_FILE_SIZE = 5000;
//	private static final String DUMP = "dump";

	public static final String DUMP_WAIT = "DUMP_WAIT";

	private volatile String info;

	private static LoggerContext ctx = setSysSerialNumber(null);
	private final Logger logger = (Logger) LogManager.getLogger();
	public static final Logger dumper = (Logger) LogManager.getLogger("dumper");
	public static final Marker marker = MarkerManager.getMarker("FileWork");

	private List<DefaultController> dumpsList = new ArrayList<>();

	private volatile static Map<Integer, String> variables = new HashMap<>();

	private ValueChangeListener valueChangeListener = new ValueChangeListener() {
		@Override
		public void valueChanged(ValueChangeEvent valueChangeEvent) {
			logger.debug("valueChanged({})", valueChangeEvent);
			new DumpWorker(valueChangeEvent);
		}
	};

	public DumpControllers(UnitsContainer unitsPanel, LinkHeader linkHeader, DeviceInfo deviceInfo) {

		setSysSerialNumber(deviceInfo.getSerialNumber().toString());
		setInfo(deviceInfo);

		int dumpWaitMinuts = GuiController.getPrefs().getInt(DUMP_WAIT, 10);
		int waitTime = 1000*60*dumpWaitMinuts;

		logger.trace("new DumpControllers({}, {}, {}, waitTime={} msec({} min))", unitsPanel, linkHeader, deviceInfo, waitTime, dumpWaitMinuts);
		dumper.info(marker, "******************** Start New Dump Block ********************");

//		this.parent = unitsPanel;

//		createNewFile(serialNumber);
//		deviceInfoStr = deviceInfo.toString();
//		new DumpToFile(unitsPanel, file, "Start", deviceInfoStr);

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0, 0) { @Override public Integer getPriority() { return 14; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_0");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1, 1) { @Override public Integer getPriority() { return 13; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_1");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_2, 2) { @Override public Integer getPriority() { return 12; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_2");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_3,3) { @Override public Integer getPriority() { return 11; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_3");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_4, 4) { @Override public Integer getPriority() { return 10; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_4");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_INFO,
				PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10, 10) { @Override public Integer getPriority() { return 9; }
		}, waitTime, "DUMP_DEVICE_DEBAG_DEVICE_INFO_10");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_1, 1) { @Override public Integer getPriority() { return 8; }
		}, waitTime, "DUMP_REGISTER_1");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_2, 2) { @Override public Integer getPriority() { return 7; }
		}, waitTime, "DUMP_REGISTER_2");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_7, 7) { @Override public Integer getPriority() { return 6; }
		}, waitTime, "DUMP_REGISTER_7");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_3, 3) { @Override public Integer getPriority() { return 5; }
		}, waitTime, "DUMP_REGISTER_3");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_4, 4) { @Override public Integer getPriority() { return 4; }
		}, waitTime, "DUMP_REGISTER_4");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_5, 5) { @Override public Integer getPriority() { return 3; }
		}, waitTime, "DUMP_REGISTER_5");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_6, 6) { @Override public Integer getPriority() { return 2; }
		}, waitTime, "DUMP_REGISTER_6");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_DEVICE_DEBAG, Packet.IRT_SLCP_PARAMETER_DEVICE_DEBAG_DUMP,
				PacketWork.PACKET_ID_DUMP_REGISTER_100, 100) { @Override public Integer getPriority() { return 1; }
		}, waitTime, "DUMP_REGISTER_100");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_SUMMARY_STATUS,
				PacketWork.PACKET_ID_ALARMS_SUMMARY) { @Override public Integer getPriority() { return 50; }
		}, 1000, "ALARMS_SUMMARY");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_STATUS,
				PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE, AlarmsController.OWER_TEMPERATURE) { @Override public Integer getPriority() { return 50; }
		}, waitTime, "ALARMS_OWER_TEMPERATURE");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_STATUS,
				PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT, AlarmsController.HW_FAULT) { @Override public Integer getPriority() { return 50; }
		}, waitTime, "ALARMS_HARDWARE_FAULT");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_STATUS,
				PacketWork.PACKET_ID_ALARMS_OWER_CURRENT, AlarmsController.OWER_CURRENT) { @Override public Integer getPriority() { return 50; }
		}, waitTime, "ALARMS_OWER_CURRENT");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_STATUS,
				PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK, AlarmsController.PLL_OUT_OF_LOCK) { @Override public Integer getPriority() { return 50; }
		}, waitTime, "ALARMS_PLL_OUT_OF_LOCK");

		addDumpController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_STATUS,
				PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT, AlarmsController.UNDER_CURRENT) { @Override public Integer getPriority() { return 50; }
		}, waitTime, "ALARMS_UNDER_CURRENT");
	}

	public void setInfo(DeviceInfo deviceInfo) {
		info = "\n! SN: "+deviceInfo.getSerialNumber();
		info += "\n! "+deviceInfo.getUnitName();
		info += "\n! Version: "+deviceInfo.getFirmwareVersion();
		info += "\n! Built Date: "+deviceInfo.getFirmwareBuildDate();
		info += "\n! Type: "+deviceInfo.getType();
		info += "\n! Subtype: "+deviceInfo.getSubtype();
		info += "\n! Revision: "+deviceInfo.getRevision();
		info += "\n! count: "+deviceInfo.getFirmwareBuildCounter();
		logger.debug("deviceInfo: "+info);
	}

	public static LoggerContext setSysSerialNumber(String serialNumber) {

		if(serialNumber==null)
			serialNumber ="UnknownSerialNumber";

		String sysSerialNumber = System.getProperty("serialNumber");

		if(sysSerialNumber==null || !sysSerialNumber.equals(serialNumber)){
			System.setProperty("serialNumber", serialNumber.replaceAll("[:\\\\/*?|<>]", "x"));

			ctx = (LoggerContext) LogManager.getContext(false);
			ctx.reconfigure();
		}

		return ctx;
	}

	private void addDumpController(Getter getter, int waitTime, String threadName){

		DefaultController dumpController = new DefaultController(threadName, getter, Style.CHECK_ONCE)
		{ @Override protected ValueChangeListener addGetterValueChangeListener() { return DumpControllers.this.valueChangeListener; }};

		dumpController.setWaitTime(waitTime);

		Thread t = new Thread(dumpController, threadName);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();

		dumpsList.add(dumpController);
	}

	public void stop() {
		logger.trace("stop()");
		for(DefaultController dc:dumpsList)
			dc.setRun(false);
	}

	@Override
	protected void finalize() throws Throwable {
		fireValueChangeListener(new ValueChangeEvent(0, PacketWork.PACKET_ID_ALARMS));
		dumper.info("Communication Lost");
		stop();
	}

	public void setWaitTime(int waitTime) {
		logger.trace("setWaitTime(waitTime={})", waitTime);

		for (DefaultController dc:dumpsList)
			dc.setWaitTime(waitTime);
	}

	public static String parseId(int id) {
		String str = ""+id;

		if(id==PacketWork.PACKET_ID_ALARMS_SUMMARY)
			str = "*** SUMMARY ALARM(PaketWork ID="+id+ ") ***";
		else if(id==PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10)
			str = "1.10(PaketWork ID="+id+ ")";
		else if(id==PacketWork.PACKET_ID_DUMP_REGISTER_100)
			str = "2.100(PaketWork ID="+id+ ")";
		else if(str.charAt(0)=='9')
			str = str.replace("9", "1.")+"(PaketWork ID="+id+ ")";
		else if(str.charAt(str.length()-1)>'0')
			str = str.replace("10", "2.")+"(PaketWork ID="+id+ ")";

		return str;
	}

	//******************* class DumpWorker *******************************
	private class DumpWorker extends Thread{

		private AWTEvent valueChangeEvent;

		public DumpWorker(ValueChangeEvent valueChangeEvent) {
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
			setPriority(priority-1);
			start();
		}

		@Override
		public void run() {
			int id = valueChangeEvent.getID();
			Object source = valueChangeEvent.getSource();
			String sourceStr = source.toString();

			String value = variables.get(id);
			if (value == null || !value.equals(sourceStr)) {
				if(source instanceof Integer){
					int integer = (Integer) source;
					sourceStr = summaryAlarm(id, integer);
				}else if(source instanceof short[]){
					byte status = (byte) (((short[])source)[2]&7);
					switch(id){
					case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
					case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
					case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
					case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
					case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
						sourceStr = "*** Alarm "+AlarmsController.getAlarmName((short) id)+" - "+AlarmsController.alarmStatusToString(status)+" ***";
					}
				}
				variables.put(id, sourceStr);
				dumper.info(marker, "{}:{}\n{}",parseId(id), info, sourceStr);
			}
		}
		

		private String summaryAlarm(int id, int integer) {
			String sourceStr = null;
			switch(id){
			case PacketWork.PACKET_ID_ALARMS_SUMMARY:
				notifyAllControllers();
				fireValueChangeListener(new ValueChangeEvent(integer, id));
				sourceStr = AlarmsController.alarmStatusToString((byte) integer);
			}
			return "Summary Alarm - "+sourceStr;
		}

		private void notifyAllControllers() {
			for(DefaultController d:dumpsList)
				synchronized(d){
					d.notify();
				}
		}
	}
}
