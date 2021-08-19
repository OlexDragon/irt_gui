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

import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCode;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBits;

public class MeasurementPacket extends PacketSuper{
	private final static Logger logger = LogManager.getLogger();

	public final static Function<Packet, Optional<Object>> parseValueFunction =

			packet-> Optional.ofNullable(packet)
			.map(
					p->
					Optional.of(p)
					.filter(LinkedPacket.class::isInstance)
					.map(LinkedPacket.class::cast)
					.map(LinkedPacket::getLinkHeader)
					.map(LinkHeader::getAddr)
					.orElse((byte)0)!=0)	// not a converter

			.map(b-> b ? ParameterHeaderCodeBUC.class : ParameterHeaderCodeFCM.class)

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
													pc->{

														// status flags
														if(pc.getStatus().getCode()==code){
															int statusBits = pl.getInt(0);
															List<StatusBits> parseStatusBits = pc.parseStatusBits(statusBits);
															return new AbstractMap.SimpleEntry<>(pc.name(), parseStatusBits);
														}

														return pc.toEntry(pl.getBuffer());
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
	public MeasurementPacket() {
		this((byte)0);
	}

	/**
	 *  BIAS Board request packet
	 * @param linkAddr
	 */
	public MeasurementPacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.MEASUREMENT_ALL,
				PacketGroupIDs.MEASUREMENT,
				PacketImp.PARAMETER_ALL,
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
