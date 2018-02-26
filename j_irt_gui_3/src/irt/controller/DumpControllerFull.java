package irt.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;

import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.AlarmsSummaryPacket;
import irt.data.packet.AttenuationPacket;
import irt.data.packet.DeviceDebugHelpPacket;
import irt.data.packet.DeviceDebugHelpPacket.HelpValue;
import irt.data.packet.DeviceDebugPacket;
import irt.data.packet.DeviceDebugPacket.Dump;
import irt.data.packet.DeviceDebugReadWritePacket;
import irt.data.packet.FrequencyPacket;
import irt.data.packet.GainPacket;
import irt.data.packet.LOPacket;
import irt.data.packet.LinkHeader;
import irt.data.packet.MuteControlPacket;
import irt.data.packet.NetworkAddressPacket;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;

public class DumpControllerFull  implements PacketListener, Runnable, Dumper{

	private static LoggerContext ctx = setSysSerialNumber(null);

	public static final List<Class<? extends PacketAbstract>> packetsToControl = Arrays.asList(
																								AttenuationPacket.class,
																								FrequencyPacket.class,
																								GainPacket.class,
																								LOPacket.class,
																								MuteControlPacket.class,
																								DeviceDebugReadWritePacket.class,
																								NetworkAddressPacket.class);

	public static final Logger dumper = LogManager.getLogger("dumper");
	public static final Marker marker = MarkerManager.getMarker("FileWork");

	private Timer infoTimer;
	private DeviceInfo deviceInfo;
	private final byte addr;

	private final 	ScheduledExecutorService service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private final 	ScheduledFuture<?> 		scheduleAtFixedRate;
	private final 	DeviceDebugHelpPacket helpPacket;

	private int[] deviceIndexes;
	private int[] dumpIndexes;

	public DumpControllerFull(DeviceInfo deviceInfo) {
		synchronized (dumper) {
			dumper.info(marker, "\n******************** Start New Dump Block for ********************{}\n", deviceInfo);
		}

		this.deviceInfo = deviceInfo;
		addr = deviceInfo.getLinkHeader().getAddr();
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		infoTimer = new Timer((int) TimeUnit.HOURS.toMillis(24), e->{synchronized (this) { dumpDoneFor.clear(); }});
		infoTimer.start();

		helpPacket = new DeviceDebugHelpPacket(addr);
		scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 20, TimeUnit.SECONDS);
	}

	int devicesCount;
	int dumpsCount;
	@Override
	public void run() {
		if(deviceIndexes==null) {
			GuiControllerAbstract.getComPortThreadQueue().add(helpPacket);
			return;
		}

		if(devicesCount>=deviceIndexes.length)
			devicesCount = 0;

		if(dumpsCount>=dumpIndexes.length)
			dumpsCount = 0;

		final DeviceDebugPacket devicePacket = new DeviceDebugPacket(addr, deviceIndexes[devicesCount], Dump.DEVICE);
		GuiControllerAbstract.getComPortThreadQueue().add(devicePacket);
		GuiControllerAbstract.getComPortThreadQueue().add(new DeviceDebugPacket(addr, dumpIndexes[dumpsCount], Dump.INFO));
	}

	@Override
	public void stop(){
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		scheduleAtFixedRate.cancel(true);
		infoTimer.stop();
		synchronized (dumper) {
			dumper.info(marker, "\n^^^^^^^^^^^^^^^^^^^ Stop Dump Block for ^^^^^^^^^^^^^^^^^{}\n", deviceInfo);
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {
		Optional
		.of(packet)
		.filter(checkAddress())
		.flatMap(p->Packets.cast(p))
		.filter(haveToDoDump())
		.ifPresent(p->{

			checkSummaryAlarm(p);
			Object value = p.getValue();
			final PacketHeader header = p.getHeader();

			if(deviceIndexes==null && p.getClass() == DeviceDebugHelpPacket.class)
				getIndexxes(value);

			// Remove unused indexes
			else if(p.getClass() == DeviceDebugPacket.class) {

				if(Optional.ofNullable(value).filter(v->v.toString().trim().equals("Invalid index value")).map(v->true).orElse(false)) {
					value = ((DeviceDebugPacket)p).getParsePacketId() + ": " + value;
					removeUnusedIndexes(header);

				}else if(header.getOption()!=PacketImp.ERROR_NO_ERROR) {
					value = ((DeviceDebugPacket)p).getParsePacketId() + " - " + header.getOptionStr();	//Dump error message
					removeUnusedIndexes(header);
				}else
					value = ((DeviceDebugPacket)p).getParsePacketId() + ": " + value;

			}else if(header.getOption()!=PacketImp.ERROR_NO_ERROR)
				value = header.getOptionStr();	//Dump error message

			if(value instanceof Optional)
				value = ((Optional<?>)value).map(Object::toString).orElse("N/A");

//			dumper.error(c);
			doDump(p.getClass().getSimpleName() + " - " + value);
		});
	}

	private void removeUnusedIndexes(final PacketHeader header) {
		int[] is;
		final short packetId = header.getPacketId();
		final short shift;
		boolean isDevices = packetId>=PacketWork.DEVICES;

		if(isDevices) {
			is = deviceIndexes;
			shift = PacketWork.DEVICES;
		}else {
			is = dumpIndexes;
			shift = PacketWork.DUMPS;
		}

		Optional
		.ofNullable(is)
		.ifPresent(a->{

			int parameterIndex 	= (packetId - shift);
			int position 		= indexOf(is, parameterIndex);

			if(shift==PacketWork.DEVICES)
				deviceIndexes = remove(position, deviceIndexes);
			else
				dumpIndexes = remove(position, dumpIndexes);
		});
	}

	private synchronized int[] remove(int index, int[] array) {

		if(!Optional.ofNullable(array).filter(a->a.length>0).filter(a->index>=0).isPresent()) {
			return null;
		}

		int length = array.length-1;
		int[] a = Arrays.copyOf(array, length);

		System.arraycopy(array, index+1, a, index, length-index);

		return a;
	}

	private synchronized int indexOf(int[] array, int value) {

		return Optional
				.ofNullable(array)
				.map(a->{
					int indexOf = -1;
					for(int i=0; i<a.length; i++)
						if(a[i]==value) {
							indexOf = i;
							break;
						}
					return indexOf;
				})
				.orElse(-1);
	}

	private void getIndexxes(Object value) {
		IntStream[] intStreams = Optional
									.ofNullable(value)
									.filter(HelpValue.class::isInstance)
									.map(HelpValue.class::cast)
									.map(HelpValue::parse)
									.get();

		deviceIndexes = intStreams[DeviceDebugHelpPacket.DEVICES].toArray();
		dumpIndexes 	= intStreams[DeviceDebugHelpPacket.DUMP].toArray();
		if(dumpIndexes.length==0)
			dumpIndexes = new int[] {0, 1, 2, 3, 4, 10, 11};

		getAllDumps(deviceIndexes, Dump.DEVICE);
		getAllDumps(dumpIndexes, Dump.INFO);
	}

	private void getAllDumps(int[] dumps, Dump dump) {
		Optional
		.ofNullable(dumps)
		.ifPresent(d->{
			for(int i=0; i<dumps.length; i++)
				GuiControllerAbstract.getComPortThreadQueue().add(new DeviceDebugPacket(addr, d[i], dump));				
		});
	}

	private final Map<Short, Object> oldValues = new HashMap<>();
	/** @return TRUE if packet is from 'packetsToControl' list and the packet value changed */
	private boolean dumpControlled(PacketAbstract packet) {

		Class<? extends PacketAbstract> pc = packet.getClass();

		boolean packetIsUnderControl = packetsToControl
											.parallelStream()
											.filter(clazz->clazz==pc)
											.findAny()
											.isPresent();

		if(!packetIsUnderControl)
			return false;

		final short packetId = packet.getHeader().getPacketId();

		if(hasException(packetId))
			return false;

		Object value = packet.getValue();

		Boolean haveToDump = Optional
				.ofNullable(oldValues.get(packetId))
				.filter(v->v.equals(value))
				.map(v->false)
				.orElse(true);

		if(haveToDump)
			oldValues.put(packetId, value);

		return haveToDump;
	}

	private short[] exceptions = {
			PacketWork.PACKET_ID_DEVICE_DEBUG_OUTPUT_POWER,
			PacketWork.PACKET_ID_DEVICE_DEBUG_TEMPERATURE,
			PacketWork.PACKET_ID_DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS,
			PacketWork.PACKET_ID_DEVICE_DEBUG_HS2_CURRENT,
			PacketWork.PACKET_ID_DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC1,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC1_FCM,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC2,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC2_FCM,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC3,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC3_FCM,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC4,
			PacketWork.PACKET_ID_DEVICE_CONVERTER_DAC4_FCM
			};

	private boolean hasException(short packetId) {

		boolean hasExceptions = false;

		for(int i=0; i<exceptions.length; i++) {
			if(packetId==exceptions[i]) {
				hasExceptions = true;
				break;
			}
		}

		return hasExceptions;
	}

	private Object summaryAlarm;
	private void checkSummaryAlarm(PacketAbstract packet) {
		if(!(packet instanceof AlarmsSummaryPacket))
			return;

		final Object value = ((AlarmsSummaryPacket)packet).getValue();
		
		if(!value.equals(summaryAlarm)){
			summaryAlarm = value;
			synchronized (this) {
				dumpDoneFor.clear();
				oldValues.clear();
			}
		}
	}

	private Predicate<? super Packet> checkAddress() {
		return p->{

			byte addr;

			if(p instanceof LinkedPacket)
				addr = Optional.ofNullable(((LinkedPacket)p).getLinkHeader()).map(LinkHeader::getAddr).orElse((byte)0);
			else
				addr = 0;

			return addr == this.addr;
		};
	}

	private final List<Short> dumpDoneFor = new ArrayList<>();
	private Predicate<? super PacketAbstract> haveToDoDump() {
		return p->{

			if(dumpControlled(p))
				return true;

			final short packetId = p.getHeader().getPacketId();

			final boolean doesNotContains = !dumpDoneFor.contains(packetId);

			if(doesNotContains)
				synchronized (this) { dumpDoneFor.add(packetId); }

			return doesNotContains;
		};
	}

	private void doDump(Object text) {

		final StringBuilder sb = new StringBuilder()
											.append("addr=")	.append(Optional.ofNullable(deviceInfo.getLinkHeader()).map(LinkHeader::getAddr).orElse((byte) 0)&0xFF)	.append("; ")
											.append("SN: ")		.append(deviceInfo.getSerialNumber().orElse(null))	.append("; ")
											.append("Uptime: ")	.append(deviceInfo.getUptimeCounter())				.append("; ")
											.append(System.lineSeparator())
											.append(text);
		synchronized (dumper) {
			dumper.info(marker, sb);
		}
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
}
