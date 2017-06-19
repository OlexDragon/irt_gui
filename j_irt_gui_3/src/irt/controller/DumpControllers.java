package irt.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;

import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmStatusPacket;
import irt.data.packet.DeviceDebugPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;

public class DumpControllers implements PacketListener, Runnable{

	public static final long DUMP_TIME = TimeUnit.MINUTES.toSeconds(15);

	private static LoggerContext ctx = setSysSerialNumber(null);
	private final Logger logger = LogManager.getLogger();
	public static final Logger dumper = LogManager.getLogger("dumper");
	public static final Marker marker = MarkerManager.getMarker("FileWork");

	private List<DefaultController> dumpsList = new ArrayList<>();

	private DeviceInfo deviceInfo;

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final ArrayList<DeviceDebugPacket> packets = new ArrayList<>();

	public DumpControllers(DeviceInfo deviceInfo) {

		this.deviceInfo = deviceInfo;

		synchronized (dumper) {
			dumper.info(marker, "\n******************** Start New Dump Block for ********************\n{}", deviceInfo);
		}

		final byte addr = Optional.ofNullable(deviceInfo.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0);

		packets.add( new DeviceDebugPacket(addr, 0, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_0, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 1, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_1, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 2, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_2, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 3, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_3, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 4, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_4, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 10, 	PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_10, 	PacketImp.PARAMETER_DEVICE_DEBAG_INFO));
		packets.add( new DeviceDebugPacket(addr, 1, 	PacketWork.PACKET_ID_DUMP_REGISTER_1, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 2, 	PacketWork.PACKET_ID_DUMP_REGISTER_2, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 3, 	PacketWork.PACKET_ID_DUMP_REGISTER_3, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 4, 	PacketWork.PACKET_ID_DUMP_REGISTER_4, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 5, 	PacketWork.PACKET_ID_DUMP_REGISTER_5, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 6, 	PacketWork.PACKET_ID_DUMP_REGISTER_6, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 7, 	PacketWork.PACKET_ID_DUMP_REGISTER_7, 					PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		packets.add( new DeviceDebugPacket(addr, 100, 	PacketWork.PACKET_ID_DUMP_REGISTER_100, 				PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));

		if(	deviceInfo.hasSlaveBiasBoard()){
			packets.add( new DeviceDebugPacket(addr, 201, 	PacketWork.PACKET_ID_DUMP_REGISTER_201, 		PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
			packets.add( new DeviceDebugPacket(addr, 202, 	PacketWork.PACKET_ID_DUMP_REGISTER_202, 		PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
			packets.add( new DeviceDebugPacket(addr, 207, 	PacketWork.PACKET_ID_DUMP_REGISTER_207, 		PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
			packets.add( new DeviceDebugPacket(addr, 220, 	PacketWork.PACKET_ID_DUMP_REGISTER_220, 		PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
			packets.add( new DeviceDebugPacket(addr, 24, 	PacketWork.PACKET_ID_DUMP_POWER, 				PacketImp.PARAMETER_DEVICE_DEBAG_DUMP));
		}

		logger.trace(packets);

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 5, 1, TimeUnit.SECONDS);
	}

	public static LoggerContext setSysSerialNumber(String serialNumber) {

		if(serialNumber==null)
			serialNumber ="UnknownSerialNumber";
		else
			serialNumber = serialNumber.replaceAll("[:\\\\/*?|<>]", "x");

		String sysSerialNumber = System.getProperty("serialNumber");

		if(sysSerialNumber==null || !sysSerialNumber.equals(serialNumber)){

			if(ctx!=null)
				synchronized (dumper) {
					dumper.info(marker, "\n***** filename changed to {} *****", serialNumber);
				}

			System.setProperty("serialNumber", serialNumber);

			ctx = (LoggerContext) LogManager.getContext(false);
			ctx.reconfigure();

			if(sysSerialNumber!=null)
				synchronized (dumper) {
					dumper.info(marker, "\n***** continuation... beginning in the File {} *****", sysSerialNumber);
				}
		}

		return ctx;
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

	public synchronized void stop() throws Throwable {

		final Iterator<DefaultController> iterator = dumpsList.iterator();

		while(iterator.hasNext()) 
			iterator.next().stop();

		dumpsList.clear();
		finalize();
	}

	@Override
	protected void finalize() throws Throwable {

		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);

		synchronized (service) {
			if(!service.isShutdown())
				service.shutdownNow();
		}

		synchronized (dumper) {
			dumper.warn("Communication Lost: {} ", deviceInfo);
		}
	}

	public synchronized void setWaitTime(int waitTime) {
		logger.trace("setWaitTime(waitTime={})", waitTime);

		for (DefaultController dc:dumpsList)
			dc.setWaitTime(waitTime);
	}

	public static String parseId(int id) {
		String str = ""+id;

		switch(id){
		case PacketWork.PACKET_ID_ALARMS_SUMMARY:
			str = "*** SUMMARY ALARM (PaketWork ID="+id+ ") ***";
			break;
		case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
			str = "Alarm Redundant ("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
			str = "Alarm Hardware ("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
			str = "Alarm Ower Current ("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
			str = "Alarm Ower Temperarure ("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
			str = "Alarm PLL out of Lock ("+id+")";
			break;
		case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
			str = "Alarm Under Current ("+id+")";
			break;
		case PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_10:
			str = "1.10 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_100:
			str = "2.100 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_201:
			str = "2.201 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_202:
			str = "2.202 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_207:
			str = "2.207 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_DUMP_REGISTER_220:
			str = "2.220 (PaketWork ID="+id+ ")";
			break;
		case PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_STATUS:
			str = "Redundancy Status (PaketWork ID="+id+ ")";
			break;
		default:
			if(str.charAt(0)=='9')
				str = str.replace("9", "1.")+" (PaketWork ID="+id+ ")";
			else if(str.charAt(str.length()-1)>'0')
				str = str.replace("10", "2.")+" (PaketWork ID="+id+ ")";
		}

		return str;
	}

	public synchronized void notifyAllControllers() {
		final Iterator<DefaultController> iterator = dumpsList.iterator();
		while(iterator.hasNext()){
			final DefaultController next = iterator.next();
			synchronized(next){
				next.notify();
			}
		}
	}

	public synchronized void doDump() {

		lastValues.clear();
		count = 0;
		if(scheduledFuture!=null && !scheduledFuture.isCancelled())
			scheduledFuture.cancel(true);

		synchronized (service) {

			if(service.isShutdown())
				return;

			scheduledFuture = service.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
		}
	}

	private static List<Consumer<String>> actionsWhenDump = new ArrayList<>();
	public static void addActionWhenDump(Consumer<String> consumer){
		actionsWhenDump.add(consumer);
	}

	public void doDump(Optional<String> o) {
		logger.trace(o);

		o.ifPresent(text->{
			final StringBuilder sb = new StringBuilder()
											.append("addr=")	.append(Optional.ofNullable(deviceInfo.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0)&0xFF)	.append("; ")
											.append("SN: ")		.append(deviceInfo.getSerialNumber())				.append("; ")
											.append(System.lineSeparator())
											.append(text);
			synchronized (dumper) {
				dumper.info(marker, sb);
			}

			actionsWhenDump.parallelStream().forEach(c->c.accept(sb.toString()));
		});
	}

	private final Map<Short, Object> lastValues = new HashMap<>();

	@Override
	public void onPacketRecived(final Packet packet) {

		if(!isPacketToDump(packet))
			return;

		final short packetId = packet.getHeader().getPacketId();
		final Object lastValue = lastValues.get(packetId);

		final Optional<? extends PacketAbstract> cast = Packets.cast(packet);

		if(!cast.isPresent())
			return;

		//filter AlarmStatusPackets (we need only alarm status packet )
		if(!isNotAlarmOrIsAlarmStatus(cast))
			return;


		final PacketAbstract pa = cast.get();
		final Object value = pa.getValue();

		if(value!=null && (lastValue==null || !lastValue.equals(value))){

			if(packetId==PacketWork.PACKET_ID_ALARMS_SUMMARY)
				doDump();

			synchronized (this) {
				lastValues.put(packetId, value);
			}

			doDump(Optional.ofNullable(pa.getValue()).map(v->"\n" + pa.getHeader().getPacketIdStr() + ": " + v));
		}
	}

	private Boolean isNotAlarmOrIsAlarmStatus(final Optional<? extends PacketAbstract> cast) {

		final Optional<? extends PacketAbstract> isAlarmAtatus = cast.filter(AlarmStatusPacket.class::isInstance);

		return !isAlarmAtatus.isPresent() || isAlarmAtatus
														.map(PacketAbstract::getPayloads)
														.map(pls->pls.stream())
														.orElse(Stream.empty())
														.map(Payload::getParameterHeader)
														.map(ParameterHeader::getCode)
														.map(code->code==PacketImp.ALARM_STATUS)
														.findAny()
														.orElse(false);
	}

	private static final List<Byte> groupsToDump =
			Arrays.asList(
					new Byte[]{
							PacketImp.GROUP_ID_ALARM,
							PacketImp.GROUP_ID_DEVICE_DEBAG,
							PacketImp.GROUP_ID_CONFIGURATION
					});

	private boolean isPacketToDump(Packet packet) {

		return Optional
				.ofNullable(packet)
				.map(Packet::getHeader)
				.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
				.filter(h->groupsToDump.contains(h.getGroupId()))
				.map(h->true)
				.orElse(false);
	}

	private long count;

	@Override
	public synchronized void run() {

		if(--count<0)
		try{
			count = DUMP_TIME;
			dumper.info(marker, deviceInfo);

			packets.stream().forEach(GuiControllerAbstract.getComPortThreadQueue()::add);

		}catch (Exception e) {
			logger.catching(e);
		}
	}
}
