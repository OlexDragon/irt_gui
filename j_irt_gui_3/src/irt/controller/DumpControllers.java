package irt.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.ToHex;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.tools.panel.head.IrtPanel;

public class DumpControllers{

//	private static final int MAX_FILE_QUANTITY = 50;
//	private static final int MAX_FILE_SIZE = 5000;
//	private static final String DUMP = "dump";

	private static final int PRIORITY = 50;

	public static final String DUMP_WAIT = "DUMP_WAIT";

	private static LoggerContext ctx = setSysSerialNumber(null);
	private final Logger logger = (Logger) LogManager.getLogger();
	public static final Logger dumper = (Logger) LogManager.getLogger("dumper");
	public static final Marker marker = MarkerManager.getMarker("FileWork");

	private List<DefaultController> dumpsList = new ArrayList<>();
	private LinkHeader linkHeader;

	private DeviceInfo deviceInfo;

	private Object hwFault;
	private Object underCurrent;
	private Object outOfLock;
	private Object owerTemt;
	private Object owerCurr;
	private Object redundantFault;
	private Object summary;
	private Object redundancyStat;
	private Object register100;
	private Object register10;
	private Object deviceInfo0;
	private Object deviceInfo1;
	private Object deviceInfo2;
	private Object deviceInfo3;
	private Object deviceInfo4;
	private Object register1;
	private Object register201;
	private Object register2;
	private Object register202;
	private Object register7;
	private Object register207;
	private Object register220;
	private Object register3;
	private Object register4;
	private Object register5;
	private Object register6;

	public DumpControllers(LinkHeader linkHeader, DeviceInfo deviceInfos) {

		this.linkHeader = linkHeader;
		this.deviceInfo = deviceInfos;

		int dumpWaitMinuts = 0;

		Properties p = IrtPanel.PROPERTIES;
		for (Object s : p.keySet()) {
			String str = (String) s;
			if(str.equalsIgnoreCase("dumptime")){
				String waitTime = p.getProperty(str);
				if(waitTime !=null && !(waitTime = waitTime.replaceAll("\\D", "")).isEmpty()){
					dumpWaitMinuts = Integer.parseInt(waitTime);
					GuiController.getPrefs().putInt(DumpControllers.DUMP_WAIT, dumpWaitMinuts);
				}
			}
		}

		if(dumpWaitMinuts==0)
			dumpWaitMinuts = GuiController.getPrefs().getInt(DUMP_WAIT, 10);

		int waitTime = 1000*60*dumpWaitMinuts;

		logger.trace("new DumpControllers({}, {}, waitTime={} msec({} min))", linkHeader, waitTime, dumpWaitMinuts);
		dumper.info(marker, "\n******************** Start New Dump Block for {} ********************", linkHeader);

//		this.parent = unitsPanel;

//		createNewFile(serialNumber);
//		deviceInfoStr = deviceInfo.toString();
//		new DumpToFile(unitsPanel, file, "Start", deviceInfoStr);

		addDumpController(
				new DefaultController(
						deviceInfo.getType(),
						"ALARMS_SUMMARY",
						new Getter(linkHeader,
								PacketImp.GROUP_ID_ALARM,
								AlarmsController.ALARMS_SUMMARY_STATUS,
								PacketWork.PACKET_ID_ALARMS_SUMMARY)
						{
							private int p = PRIORITY;
							@Override public int getPriority() {
								return p;
							}
						},
						Style.CHECK_ALWAYS)
				{
					@Override
					protected PacketListener getNewPacketListener() {
						return new PacketListener() {
							@Override
							public void packetRecived(Packet packet) {
								byte groupId;

								if(getPacketWork().isAddressEquals(packet) &&
										((	groupId = packet.getHeader().getGroupId())==PacketImp.GROUP_ID_ALARM ||
											groupId==PacketImp.GROUP_ID_DEVICE_DEBAG ||
											groupId==PacketImp.GROUP_ID_DEVICE_INFO))

									new DumpWorker(packet);
							}
						};
					}
				},
				3000);

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE,
						AlarmsController.OWER_TEMPERATURE,
						PRIORITY),
				waitTime,
				"ALARMS_OWER_TEMPERATURE");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT,
						AlarmsController.HW_FAULT,
						PRIORITY),
				waitTime,
				"ALARMS_HARDWARE_FAULT");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_OWER_CURRENT,
						AlarmsController.OWER_CURRENT,
						PRIORITY),
				waitTime,
				"ALARMS_OWER_CURRENT");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK,
						AlarmsController.PLL_OUT_OF_LOCK_OR_OUTDOOR_FCM_HARDWARE_FAULT,
						PRIORITY),
				waitTime,
				"ALARMS_PLL_OUT_OF_LOCK");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT,
						AlarmsController.UNDER_CURRENT,
						PRIORITY),
				waitTime,
				"ALARMS_UNDER_CURRENT");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_ALARM,
						AlarmsController.ALARMS_STATUS,
						PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT,
						AlarmsController.REDUNDANT_FAULT_OR_OUTDOOR_FCM_PLL_OUT_OF_LOCK,
						PRIORITY),
				waitTime,
				"ALARMS_REDUNDANT_FAULT");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0,
						0,
						PRIORITY-1),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_0");


		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1,
						1,
						PRIORITY-2),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_1");


		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_2,
						2,
						PRIORITY-3),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_2");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_3,
						3,
						PRIORITY-4),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_3");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_4,
						4,
						PRIORITY-5),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_4");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_INFO,
						PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10,
						10,
						PRIORITY-6),
				waitTime,
				"DUMP_DEVICE_DEBAG_DEVICE_INFO_10");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_1,
						1,
						PRIORITY-7),
				waitTime,
				"DUMP_REGISTER_1");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_2,
						2,
						PRIORITY-9),
				waitTime,
				"DUMP_REGISTER_2");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_7,
						7,
						PRIORITY-11),
				waitTime,
				"DUMP_REGISTER_7");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_3,
						3,
						PRIORITY-13),
				waitTime,
				"DUMP_REGISTER_3");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_4,
						4,
						PRIORITY-14),
				waitTime,
				"DUMP_REGISTER_4");

		addDumpController(
				newGetter(linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_5,
						5,
						PRIORITY-15),
				waitTime,
				"DUMP_REGISTER_5");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_6,
						6,
						PRIORITY-16),
				waitTime,
				"DUMP_REGISTER_6");

		addDumpController(
				newGetter(
						linkHeader,
						PacketImp.GROUP_ID_DEVICE_DEBAG,
						PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
						PacketWork.PACKET_ID_DUMP_REGISTER_100,
						100,
						PRIORITY-17),
				waitTime,
				"DUMP_REGISTER_100");

		addDumpController(
				newGetter(linkHeader,
						PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_STAT,
						PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT,
						PRIORITY-18),
				waitTime,
				"REDUNDANCY_STAT");

		if(	deviceInfo.hasSlaveBiasBoard()){

			addDumpController(
					newGetter(
							linkHeader,
							PacketImp.GROUP_ID_DEVICE_DEBAG,
							PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
							PacketWork.PACKET_ID_DUMP_REGISTER_201,
							201,
							PRIORITY-8),
					waitTime,
					"DUMP_REGISTER_201");

			addDumpController(
					newGetter(
							linkHeader,
							PacketImp.GROUP_ID_DEVICE_DEBAG,
							PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
							PacketWork.PACKET_ID_DUMP_REGISTER_202,
							202,
							PRIORITY-10),
					waitTime,
					"DUMP_REGISTER_202");

			addDumpController(
					newGetter(
							linkHeader,
							PacketImp.GROUP_ID_DEVICE_DEBAG,
							PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
							PacketWork.PACKET_ID_DUMP_REGISTER_207,
							207,
							PRIORITY-12),
					waitTime,
					"DUMP_REGISTER_207");

			addDumpController(
					newGetter(
							linkHeader,
							PacketImp.GROUP_ID_DEVICE_DEBAG,
							PacketImp.PARAMETER_DEVICE_DEBAG_DUMP,
							PacketWork.PACKET_ID_DUMP_REGISTER_220,
							220,
							PRIORITY-19),
					waitTime,
					"DUMP_REGISTER_220");
		}
	}

	private Getter newGetter(LinkHeader linkHeader, byte irtSlcpPacketId, byte irtSlcpParameter, short packetId, int value, final int priority) {
		return new Getter(
				linkHeader,
				irtSlcpPacketId,
				irtSlcpParameter,
				packetId,
				value)
		{
			private int p = priority;
			@Override public int getPriority() {
				return p;
			}
			@Override
			public boolean set(Packet packet) {
				return true;
			}
		};
	}

	private Getter newGetter(LinkHeader linkHeader, byte irtSlcpPacketId, byte irtSlcpParameter, short packetId, final int priority) {
		return new Getter(
				linkHeader,
				irtSlcpPacketId,
				irtSlcpParameter,
				packetId)
		{
			private int p = priority;
			@Override public int getPriority() {
				return p;
			}
			@Override
			public boolean set(Packet packet) {
				return true;
			}
		};
	}

	public static LoggerContext setSysSerialNumber(String serialNumber) {

		if(serialNumber==null)
			serialNumber ="UnknownSerialNumber";
		else
			serialNumber = serialNumber.replaceAll("[:\\\\/*?|<>]", "x");

		String sysSerialNumber = System.getProperty("serialNumber");

		if(sysSerialNumber==null || !sysSerialNumber.equals(serialNumber)){

			if(ctx!=null)
				dumper.info(marker, "\n***** filename changed to {} *****", serialNumber);

			System.setProperty("serialNumber", serialNumber);

			ctx = (LoggerContext) LogManager.getContext(false);
			ctx.reconfigure();

			if(sysSerialNumber!=null)
				dumper.info(marker, "\n***** continuation... beginning in the File {} *****", sysSerialNumber);
		}

		return ctx;
	}

	private void addDumpController(Getter getter, int waitTime, String threadName){

		DefaultController dumpController = new DefaultController(deviceInfo.getType(), threadName, getter, Style.CHECK_ALWAYS){

			@Override
			protected PacketListener getNewPacketListener() {
				return null;
			}};

		addDumpController(dumpController, waitTime);
	}

	public void addDumpController(DefaultController dumpController, int waitTime) {
		dumpController.setWaitTime(waitTime);

		startThread(new Thread(dumpController, "DumpControllers."+dumpController.getName()+"-"+new RundomNumber().toString()));
		dumpsList.add(dumpController);
	}

	private void startThread(Thread thread) {
		int priority = thread.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(priority-1);
		thread.setDaemon(true);
		thread.start();
	}

	public void stop() {
		for(DefaultController dc:dumpsList)
			dc.stop();
		dumpsList.clear();
	}

	@Override
	protected void finalize() throws Throwable {
		int uptimeCounter = -1;
		if(deviceInfo!=null) {
			uptimeCounter = deviceInfo.getUptimeCounter();
		}
		dumper.warn("\n\t{}\n\tUptime Counter = {}\n\tCommunication Lost", linkHeader, uptimeCounter);
	}

	public void setWaitTime(int waitTime) {
		logger.trace("setWaitTime(waitTime={})", waitTime);

		for (DefaultController dc:dumpsList)
			dc.setWaitTime(waitTime);
	}

	public static String parseId(int id) {
		String str = ""+id;

		switch(id){
		case PacketWork.PACKET_ID_ALARMS_SUMMARY:
			str = "*** SUMMARY ALARM(PaketWork ID="+id+ ") ***";
			break;
		case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
			str = "Alarm Redundant("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
			str = "Alarm Hardware("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
			str = "Alarm Ower Current("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
			str = "Alarm Ower Temperarure("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
			str = "Alarm PLL out of Lock("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
			str = "Alarm Under Current("+id+")";
			break;
		case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10:
			str = "1.10(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_100:
			str = "2.100(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_201:
			str = "2.201(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_202:
			str = "2.202(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_207:
			str = "2.207(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_220:
			str = "2.220(PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT:
			str = "Redundancy Status(PaketWork ID="+id+ ")";
			break;
		default:
			if(str.charAt(0)=='9')
				str = str.replace("9", "1.")+"(PaketWork ID="+id+ ")";
			else if(str.charAt(str.length()-1)>'0')
				str = str.replace("10", "2.")+"(PaketWork ID="+id+ ")";
		}

		return str;
	}

	//******************* class DumpWorker *******************************
	private class DumpWorker extends Thread{
		private Packet packet;

		public DumpWorker(Packet packet) {
			this.packet = packet;

			startThread(this);
		}

		@Override
		public void run() {
			logger.trace("\n{}", packet);

			PacketHeader header = packet.getHeader();
			short packetId = header.getPacketId();
			byte error = header.getOption();

				switch(packetId){

				case PacketWork.PACKET_DEVICE_INFO:
						deviceInfo = new DeviceInfo(packet);
					break;

				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_0:
					if(error==0)
						deviceInfo0 = dump(deviceInfo0);
					else
						deviceInfo0 = dumpError(deviceInfo0);
					break;
				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_1:
					if(error==0)
						deviceInfo1 = dump(deviceInfo1);
					else
						deviceInfo1 = dumpError(deviceInfo1);
					break;
				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_2:
					if(error==0)
						deviceInfo2 = dump(deviceInfo2);
					else
						deviceInfo2 = dumpError(deviceInfo2);
					break;
				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_3:
					if(error==0)
						deviceInfo3 = dump(deviceInfo3);
					else
						deviceInfo3 = dumpError(deviceInfo3);
					break;
				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_4:
					if(error==0)
						deviceInfo4 = dump(deviceInfo4);
					else
						deviceInfo4 = dumpError(deviceInfo4);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_1:
					if(error==0)
						register1 = dump(register1);
					else
						register1 = dumpError(register1);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_201:
					if(error==0)
						register201 = dump(register201);
					else
						register201 = dumpError(register201);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_2:
					if(error==0)
						register2 = dump(register2);
					else
						register2 = dumpError(register2);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_202:
					if(error==0)
						register202 = dump(register202);
					else
						register202 = dumpError(register202);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_7:
					if(error==0)
						register7 = dump( register7);
					else
						register7 = dumpError(register7);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_207:
					if(error==0)
						register207 = dump( register207);
					else
						register207 = dumpError(register207);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_220:
					if(error==0)
						register220 = dump( register220);
					else
						register220 = dumpError(register220);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_3:
					if(error==0)
						register3 = dump(register3);
					else
						register3 = dumpError(register3);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_4:
					if(error==0)
						register4 = dump(register4);
					else
						register4 = dumpError(register4);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_5:
					if(error==0)
						register5 = dump(register5);
					else
						register5 = dumpError(register5);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_6:
					if(error==0)
						register6 = dump(register6);
					else
						register6 = dumpError(register6);
					break;
				case PacketWork.PACKET_ID_DUMP_DEVICE_DEBAG_DEVICE_INFO_10:
					if(error==0)
						register10 = dump(register10);
					else
						register10 = dumpError(register10);
					break;
				case PacketWork.PACKET_ID_DUMP_REGISTER_100:
					if(error==0)
						register100 = dump(register100);
					else
						register100 = dumpError(register100);
					break;
				case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STAT:
					if(error==0)
						redundancyStat = dump(redundancyStat);
					else
						redundancyStat = dumpError(redundancyStat);
					break;
				case PacketWork.PACKET_ID_ALARMS_SUMMARY:
					if (error == 0) {
						Object dump = dumpHex(summary);
//						logger.debug("\n\tdump ={}\n\tsummary ={}", dump, summary);
						if (summary != null && !summary.equals(dump)) {
							logger.debug("notifyAllControllers();");
							notifyAllControllers();
						}
						summary = dump;
					}else
						summary = dumpError(summary);
					break;
				case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
					if(error==0)
						hwFault = dumpHex(hwFault);
					else
						hwFault = dumpError(hwFault);
					break;
				case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
					if(error==0)
						owerCurr = dumpHex(owerCurr);
					else
						owerCurr = dumpError(owerCurr);
					break;
				case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
					if(error==0)
						owerTemt = dumpHex(owerTemt);
					else
						owerTemt = dumpError(owerTemt);
					break;
				case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
					if(error==0)
						outOfLock = dumpHex(outOfLock);
					else
						outOfLock = dumpError(outOfLock);
					break;
				case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
					if(error==0)
						underCurrent = dumpHex(underCurrent);
					else
						underCurrent = dumpError(underCurrent);
					break;
				case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
					if(error==0)
						redundantFault = dumpHex(redundantFault);
					else
						redundantFault = dumpError(redundantFault);
				}
		}

		private Object dumpError(Object obj) {

			PacketHeader header = packet.getHeader();
			if(obj==null || !obj.equals(header)){
				int uptimeCounter = -1;
				if(deviceInfo!=null) {
					uptimeCounter = deviceInfo.getUptimeCounter();
				}
				dumper.warn(marker, "\n\t{}\n\tUptime Counter = {}\n\t{}\n", linkHeader, uptimeCounter, header);
			}
			return header;
		}

		private Object dumpHex(Object obj) {

			logger.debug("\n\t obj = '{}'", obj);

			Payload payload = packet.getPayload(0);

			if(payload!=null){
				byte[] buffer = payload.getBuffer();
				obj = dump(obj, ToHex.bytesToHex(buffer));
//				if(buffer!=null && (buffer.length==4 || buffer.length==6))
//					obj = AlarmsController.alarmStatusToString(buffer[buffer.length-1])+" ( "+obj+")";
			}else
				logger.warn("packet.getPayload(0)==null");

			return obj;
		}

		private Object dump(Object obj) {

			logger.debug("\n\t obj = '{}'", obj);

			Payload payload = packet.getPayload(0);

			if(payload!=null){
				Object o = payload.getStringData();
				obj = dump(obj, o);
			}else
				logger.warn("packet.getPayload(0)==null");

			return obj;
		}

		private Object dump(Object obj, Object o) {

			logger.debug("\n\t obj = '{}',\n\t o = '{}'", obj, o);

			if((obj==null || !obj.equals(o))){
				int uptimeCounter = -1;
				if(deviceInfo!=null) {
					uptimeCounter = deviceInfo.getUptimeCounter();
				}
				dumper.info(marker, "\n\t{};\n\t{};\n\t{};\n\tUptime Counter = {};\n{};\n", linkHeader, deviceInfo, packet.getHeader(), uptimeCounter, o);
			}
			return o;
		}

//		private String summaryAlarm(int id, int integer) {
//			String sourceStr = null;
//			switch(id){
//			case PacketWork.PACKET_ID_ALARMS_SUMMARY:
//				notifyAllControllers();
		
////				fireValueChangeListener(new ValueChangeEvent(integer, id));
//				sourceStr = AlarmsController.alarmStatusToString((byte) integer);
//			}
//			return "Summary Alarm - "+sourceStr;
//		}
	}

	public void notifyAllControllers() {
		for(DefaultController d:dumpsList)
			synchronized(d){
				d.notify();
			}
	}

	public void doDump(String text) {
		dumper.info(marker, text);
	}
}
