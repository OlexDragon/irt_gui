package irt.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.DeviceDebugPacketIds;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketID;
import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.FrequencyPacket;
import irt.data.packet.configuration.GainPacket;
import irt.data.packet.configuration.LOPacket;
import irt.data.packet.configuration.MuteControlPacket;
import irt.data.packet.denice_debag.DeviceDebugHelpPacket;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.denice_debag.DeviceDebugHelpPacket.HelpValue;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.denice_debag.DeviceDebugPacket.DeviceDebugType;
import irt.data.packet.denice_debag.DeviceDebugReadWritePacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.network.NetworkAddressPacket;
import irt.tools.panel.subpanel.DebagInfoPanel;

public class DumpControllerFull  implements PacketListener, Runnable, Dumper{

	private  static LoggerContext ctx = setSysSerialNumber(null);

	public final static List<Class<? extends PacketSuper>> packetsToControl = Arrays.asList(
																								AttenuationPacket.class,
																								FrequencyPacket.class,
																								GainPacket.class,
																								LOPacket.class,
																								MuteControlPacket.class,
																								DeviceDebugReadWritePacket.class,
																								NetworkAddressPacket.class);

	public static final Logger logger = LogManager.getLogger();
	public static final Logger dumper = LogManager.getLogger("dumper");
	public static final Marker marker = MarkerManager.getMarker("FileWork");

	private	final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

	private DeviceInfo deviceInfo;
	private final byte addr;

	private final 	ScheduledExecutorService service 	= Executors.newScheduledThreadPool(1, new ThreadWorker("DumpControllerFull"));
	private 	 	ScheduledFuture<?> 		scheduleAtFixedRate;
	private final 	DeviceDebugHelpPacket helpPacket;

	private int[] deviceIndexes;
	private int[] dumpIndexes;

	private final Map<PacketID, Object> oldValues = new HashMap<>();

	public DumpControllerFull(DeviceInfo deviceInfo) {

		synchronized (dumper) {
			dumper.info(marker, "\n******************** Start New DeviceDebugType Block for ********************{}\n", deviceInfo);
		}

		this.deviceInfo = deviceInfo;
		addr = deviceInfo.getLinkHeader().getAddr();
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		helpPacket = new DeviceDebugHelpPacket(addr);
		scheduleAtFixedRate = service.scheduleAtFixedRate(this, 20, 10, TimeUnit.SECONDS);
	}

	int count;
	@Override
	public void run() {

		if(deviceIndexes==null) {
			comPortThreadQueue.add(helpPacket);
			return;
		}

		if(count>=Math.max(deviceIndexes.length, dumpIndexes.length)){

			synchronized (oldValues) { oldValues.clear(); }

			count = 0;
			Optional.of(scheduleAtFixedRate).filter(sf->!sf.isDone()).ifPresent(sf->sf.cancel(false));
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 60*60, 10, TimeUnit.SECONDS);
			return;
		}

		Optional.ofNullable(deviceIndexes)
		.filter(di->count<di.length)
		.map(di->di[count])
		.flatMap(
				di->{
					final Optional<DeviceDebugPacketIds> valueOf = DeviceDebugPacketIds.valueOf(DeviceDebugType.DEVICE, di);
					return valueOf;
					})
		.ifPresent(deviceDebugPacketId->comPortThreadQueue.add(new DeviceDebugPacket(addr, deviceDebugPacketId)));

		Optional.ofNullable(dumpIndexes)
		.filter(di->count<di.length)
		.map(di->di[count])
		.flatMap(deviceIndex->DeviceDebugPacketIds.valueOf(DeviceDebugType.INFO, deviceIndex))
		.ifPresent(deviceDebugPacketId->comPortThreadQueue.add(new DeviceDebugPacket(addr, deviceDebugPacketId)));

		count++;
	}

	@Override
	public void stop(){

		if(!Optional.ofNullable(scheduleAtFixedRate).map(sch->!sch.isDone()).isPresent())
			return;

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);

		synchronized (dumper) {
			dumper.info(marker, "\n^^^^^^^^^^^^^^^^^^^ Stop DeviceDebugType Block for ^^^^^^^^^^^^^^^^^{}\n", deviceInfo);
		}
	}

	@Override
	public void onPacketReceived(final Packet packet) {

		final PacketHeader header = packet.getHeader();

		if(header==null || header.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE)
			return;

		new ThreadWorker(
				()->{

					final short packetId = header.getPacketId();
					PacketID.valueOf(packetId)
					.ifPresent(
							pId->{

								if(header.getError()!=PacketImp.ERROR_NO_ERROR) {
									dump(pId, header.getErrorStr());
									return;
								}

								final Optional<Object> oValue = pId.valueOf(packet);

								if(!oValue.isPresent()){
									logger.warn("{} - have to add parseValueFunction. {}", pId, packet);
									return;
								}

								oValue
								.ifPresent(
										value->{

											// parse indexes from DeviceDebugHelpPacket and return
											if(pId.equals(PacketID.DEVICE_DEBUG_HELP)){

												setIndexes((String) value);

												logger.trace(value);

												Optional.of(scheduleAtFixedRate).filter(sf->!sf.isDone()).ifPresent(sf->sf.cancel(false));
												scheduleAtFixedRate = service.scheduleAtFixedRate(this, 0, 10, TimeUnit.SECONDS);
												return;
											}

											final Optional<Map<PacketID, Object>> oOldValues = Optional.ofNullable(oldValues);

											if(pId.equals(PacketID.MEASUREMENT_ALL)) {

												boolean noDump = oOldValues
														.map(ovs->ovs.get(PacketID.MEASUREMENT_ALL))
														.flatMap(this::castToMap)
														.map(m->m.entrySet())
														.map(Set::stream)
														.map(
																stream->
																stream
																.map(compareValues(castToMap(value)))
																.filter(bool->bool==false)// check if need dump
																.findAny()
																.orElse(true))
														.orElse(false);

												if(noDump) return;
											}

											if(pId.equals(PacketID.DEVICE_INFO)) {

												boolean noDump = oOldValues
														.map(ovs->ovs.get(PacketID.DEVICE_INFO))
														.flatMap(this::castToOptional)
														.flatMap(
																oldDeviveInfo->
																Optional
																.ofNullable(value)
																.flatMap(this::castToOptional)
																.map(newDeviceInfo->oldDeviveInfo.equals(newDeviceInfo)))
														.orElse(false);

												if(noDump) return;
											}

											if(pId.equals(PacketID.ALARMS_SUMMARY)) {

												boolean noDump = oOldValues
														.map(ovs->ovs.get(PacketID.ALARMS_SUMMARY))
														.flatMap(this::castToOptional)
														.flatMap(
																oldSummaryAlarm->
																Optional
																.ofNullable(value)
																.flatMap(this::castToOptional)
																.map(newSummaryAlarm->oldSummaryAlarm.equals(newSummaryAlarm)))
														.orElse(false);

												if(noDump)
													return;

												else{
													final DeviceDebugInfoPacket p = new DeviceDebugInfoPacket(addr, (byte) 1);
													p.getHeader().setPacketId(PacketID.DEVICE_DEBUG_INFO_FOR_DUMP.getId());
													p.setValue(4);
													GuiControllerAbstract.getComPortThreadQueue().add(p);
												}
											}

											dump(pId, value);
										});
			});
		}, "DumpControllerFull.onPacketReceived()");
	}

	/**
	 * @return True if don't need dump
	 */
	private Function<Entry<?,?>, Boolean> compareValues(Optional<Map<?, ?>> oMap) {
		return entry->{
			final Optional<?> oValue = oMap.map(m->m.get(entry.getKey()));
			return oValue
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.flatMap(this::getDouble)
					.flatMap(
							newValue->
							Optional
							.ofNullable(entry.getValue())
							.filter(String.class::isInstance)
							.map(String.class::cast)
							.flatMap(oldValue->getDouble(oldValue))
							.map(oldValue->newValue-oldValue))
					.map(Math::abs)
					.map(abs->abs<1)	//no dump if less then one.
					.orElseGet(()->oValue.filter(newValue->entry.getValue().equals(newValue)).isPresent());
		};
	}

	private Optional<Map<?,?>> castToMap(Object values) {
		return Optional
				.of(values)
				.filter(Map.class::isInstance)
				.map(Map.class::cast);
	}

	private Optional<?> castToOptional(Object values) {

		if(values instanceof Optional)
			return (Optional<?>) values;

		return Optional.of(values);
	}

	public Optional<Double> getDouble(String value) {
		return Optional.ofNullable(value).filter(v->!v.replaceAll("\\D", "").isEmpty()).map(v->v.replaceAll("[^\\d-.]", "")).map(Double::parseDouble);
	}

	private void dump(PacketID pId, Object value) {

		final Object oldValue = oldValues.get(pId);

		if(oldValue==null || !oldValue.equals(value)){

			logger.trace(value);

			synchronized (oldValues) { oldValues.put(pId, value); }

			doDump(pId + ": " + value);
		}
	}

//	private void removeUnusedIndexes(final short packetId) {
//
//		DeviceDebugPacketIds
//		.valueOf(packetId)
//		.ifPresent(id->{
//
//			final byte parameterCode = id.getParameterCode();
//			final Integer indexToremove = id.getIndex();
//
//			if(parameterCode==PacketImp.PARAMETER_DEVICE_DEBUG_INFO){
//				final int indexOf = indexOf(deviceIndexes, indexToremove);
//				if(indexOf<0)
//					return;
//				deviceIndexes = remove(indexOf, deviceIndexes);
//			}else if(parameterCode==PacketImp.PARAMETER_DEVICE_DEBUG_DUMP){
//				final int indexOf = indexOf(dumpIndexes, indexToremove);
//				dumpIndexes = remove(indexOf, dumpIndexes);
//			}
//		});
//	}

//	private synchronized int[] remove(int index, int[] array) {
//
//		if(!Optional.ofNullable(array).filter(a->a.length>0).filter(a->index>=0).isPresent()) {
//			return null;
//		}
//
//		int length = array.length-1;
//		int[] a = Arrays.copyOf(array, length);
//
//		System.arraycopy(array, index+1, a, index, length-index);
//
//		return a;
//	}
//
//	private synchronized int indexOf(int[] array, int value) {
//
//		return Optional
//				.ofNullable(array)
//				.map(a->{
//					int indexOf = -1;
//					for(int i=0; i<a.length; i++)
//						if(a[i]==value) {
//							indexOf = i;
//							break;
//						}
//					return indexOf;
//				})
//				.orElse(-1);
//	}

	private void setIndexes(String value) {
		logger.traceEntry("{}", value);

		Optional
		.ofNullable(value)
		.map(HelpValue::new)
		.map(HelpValue::parse)
		.ifPresent(
				ints->{

					deviceIndexes = ints[DeviceDebugHelpPacket.DEVICES].toArray();

					dumpIndexes = ints[DeviceDebugHelpPacket.DUMP].toArray();
					if(dumpIndexes.length==0)
						dumpIndexes = new int[] {0, 1, 2, 3, 4, 10, 11};

					dumper.info(marker, "\n\t deviceIndexes: {}\n\t dumpIndexes: {}", deviceIndexes, dumpIndexes);

					DebagInfoPanel.setIndexes(dumpIndexes, deviceIndexes);
					getAllDumps(deviceIndexes, DeviceDebugType.DEVICE);
					getAllDumps(dumpIndexes, DeviceDebugType.INFO);
				});
	}

	private void getAllDumps(int[] dumps, DeviceDebugType deviceDebugType) {
		Optional
		.ofNullable(dumps)
		.map(Arrays::stream)
		.orElse(IntStream.empty())
		.forEach(
				index->DeviceDebugPacketIds
				.valueOf(deviceDebugType, index)
				.ifPresent(
						pId->GuiControllerAbstract
						.getComPortThreadQueue()
						.add(new DeviceDebugPacket(addr, pId))));
	}

	/** @return TRUE if packet is from 'packetsToControl' list and the packet value changed */
//	private boolean dumpControlled(PacketSuper packet) {
//
//		Class<? extends PacketSuper> pc = packet.getClass();
//
//		boolean packetIsUnderControl = packetsToControl
//											.parallelStream()
//											.filter(clazz->clazz==pc)
//											.findAny()
//											.isPresent();
//
//		if(!packetIsUnderControl)
//			return false;
//
//		final short packetId = packet.getHeader().getPacketId();
//
//		if(hasException(packetId))
//			return false;
//
//		Object value = packet.getValue();
//
//		Boolean haveToDump = Optional
//				.ofNullable(oldValues.get(packetId))
//				.filter(v->v.equals(value))
//				.map(v->false)
//				.orElse(true);
//
//		if(haveToDump)
//			oldValues.put(packetId, value);
//
//		return haveToDump;
//	}

//	private short[] exceptions = {
//			PacketIDs.DEVICE_DEBUG_OUTPUT_POWER.getId(),
//			PacketIDs.DEVICE_DEBUG_TEMPERATURE.getId(),
//			PacketIDs.DEVICE_DEBUG_TEMPERATURE_REMOTE_BIAS.getId(),
//			PacketIDs.DEVICE_DEBUG_HS2_CURRENT.getId(),
//			PacketIDs.DEVICE_DEBUG_HS2_CURRENT_REMOTE_BIAS.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC1.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC1_FCM.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC2.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC2_FCM.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC3.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC3_FCM.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC4.getId(),
//			PacketIDs.DEVICE_CONVERTER_DAC4_FCM.getId()
//			};

//	private boolean hasException(short packetId) {
//
//		boolean hasExceptions = false;
//
//		for(int i=0; i<exceptions.length; i++) {
//			if(packetId==exceptions[i]) {
//				hasExceptions = true;
//				break;
//			}
//		}
//
//		return hasExceptions;
//	}

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
