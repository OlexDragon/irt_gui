package irt.data.packet.measurement;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceType;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCode;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeLNB;
import irt.tools.fx.MonitorPanelFx.StatusBits;

public class TemperaturePacket extends PacketSuper{
	private final static Logger logger = LogManager.getLogger();

	private static Optional<DeviceType> deviceType = Optional.empty(); public static void setDeviceType(DeviceType deviceType) { TemperaturePacket.deviceType = Optional.ofNullable(deviceType); }

	public final static Function<Packet, Optional<Object>> parseValueFunction =

			packet-> Optional.ofNullable(packet)

			.map(
					p->{

						if(deviceType.map(dt->dt==DeviceType.LNB_REDUNDANCY_1x2).orElse(false)) 
							return ParameterHeaderCodeLNB.class;

						final Integer typeId = deviceType.map(DeviceType::getTypeId).orElse(0);
						if(typeId>=500)
							return ParameterHeaderCodeFCM.class;

						return ParameterHeaderCodeBUC.class;
					})

			.map(
					parameterHCodeClass->{
						try {
							return parameterHCodeClass.getMethod("valueOf", Byte.class);
						} catch (NoSuchMethodException | SecurityException e) {
							logger.catching(e);
						}
						return null;
					})
			.map(
					method->
					packet
					.getPayloads()
					.stream()
					.map(
							pl->{
								try {
									byte code = pl.getParameterHeader().getCode();
									return ((Optional<?>) method.invoke(null, code))

											.map(ParameterHeaderCode.class::cast)
											.map(
													phc->{
														// status flags
														if(phc.getStatus().getCode()==code && deviceType.map(dt->dt!=DeviceType.LNB_REDUNDANCY_1x2).orElse(true)){

															int statusBits = pl.getInt(0);
															List<StatusBits> parseStatusBits = phc.parseStatusBits(statusBits);

															return new AbstractMap.SimpleEntry<>(phc.name(), parseStatusBits);
														}

														return phc.toEntry(pl.getBuffer());
													})
											.orElse(null);

								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									logger.catching(e);
								}
								return null;
							})
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue,  (a,b)->a,  TreeMap::new)));

	/**
	 *  Converter request packet
	 */
	public TemperaturePacket() {
		this((byte)0);
	}

	/**
	 *  BIAS Board request packet
	 * @param linkAddr
	 */
	public TemperaturePacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketID.MEASUREMENT_TEMPERATURE,
				PacketGroupIDs.MEASUREMENT,
				PacketImp.PARAMETER_MEASUREMENT_TEMPERATURE,
				null,
				Priority.REQUEST);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}

	public class Measurements{

		private final Map<Object, Object> measurements;

		public Measurements(Map<Object, Object> measurements) {
			this.measurements = measurements;
		}

		public Map<Object, Object> getMeasurements() {
			return measurements;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();

			final Stream<Entry<Object, Object>> measurementStream = Optional .ofNullable(measurements) .map(m->m.entrySet().stream()) .orElse(Stream.empty());

			measurementStream.forEach(es->{

				if(sb.length()>0)
					sb.append(", ");

				final Object value = es.getValue();

				sb.append(es.getKey()).append("=").append(value.getClass().isArray() ? Arrays.toString((Object[])value) : value);
			});

			return "Measurements [" + sb + "]";
		} 
	}
}
