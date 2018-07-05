package irt.data.packet.measurement;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCode;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBits;
import irt.tools.fx.MonitorPanelFx.StatusBitsBUC;
import irt.tools.fx.MonitorPanelFx.StatusBitsFCM;

public class MeasurementPacket extends PacketSuper{
	private final static Logger logger = LogManager.getLogger();

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(p->p instanceof LinkedPacket && ((LinkedPacket)p).getLinkHeader().getAddr()!=0)	//is not a converter
																										.map(b-> b ? ParameterHeaderCodeBUC.class : ParameterHeaderCodeFCM.class)
																										.map(parameterHCodeClass->{
																											try {
																												return parameterHCodeClass.getMethod("valueOf", Byte.class);
																											} catch (NoSuchMethodException | SecurityException e) {
																												logger.catching(e);
																											}
																											return null;
																										})
																										.map(method->{

																											return packet
																													.getPayloads()
																													.stream()
																													.map(
																															pl->{
																																try {

																																	byte code = pl.getParameterHeader().getCode();
																																	final Optional<?> optional = (Optional<?>) method.invoke(null, code);

																																	return optional
																																			.map(ParameterHeaderCode.class::cast)
																																			.map(
																																					pc->{

																																					// status flags
																																						if(pc.getStatus().getCode()==code){
																																							int statusBits = pl.getInt(0);
																																							return new AbstractMap.SimpleEntry<>(pc, pc.parseStatusBits(statusBits));
																																						}

																																						return new AbstractMap.SimpleEntry<>(pc, pc.toString(pl.getBuffer()));
																																					})
																																			.orElse(null);

																																} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
																																	logger.catching(e);
																																}
																																return null;
																															})
																													.collect(Collectors.toList());
																										});

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
				PacketImp.GROUP_ID_MEASUREMENT,
				PacketImp.PARAMETER_ALL,
				null,
				Priority.REQUEST);
	}

	@Override
	public Object getValue() {


		if(getHeader().getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE)
			return this;

		final boolean 									isConverter 		= MonitorPanelFx.CONVERTER == getLinkHeader().getAddr();
		final ParameterHeaderCode 						status 				= isConverter ? ParameterHeaderCodeFCM.STATUS : ParameterHeaderCodeBUC.STATUS;

		// true  -> status bits,
		// false -> measurement values
		final Map<Boolean, List<Payload>> collect = Optional
														.ofNullable(getPayloads())
														.map(pls->pls.parallelStream())
														.orElse(Stream.empty())
														.collect(Collectors.partitioningBy(pl->pl.getParameterHeader().getCode()==status.getCode()));

		//status
		final Optional<List<StatusBits>> oStatusBits = collect
											.get(true)
											.stream()
											.map(pl->pl.getInt(0))
											.map(statusBits->isConverter ? StatusBitsFCM.parse(statusBits) : StatusBitsBUC.parse(statusBits))
											.findAny();

		//values
		final Map<Object, Object> result = collect
										.get(false)
										.parallelStream()
										.map(
												pl->{

													final byte code = pl.getParameterHeader().getCode();
													final Optional<? extends ParameterHeaderCode> 	oParameterHeaderCode = isConverter ? ParameterHeaderCodeFCM.valueOf(code) : ParameterHeaderCodeBUC.valueOf(code);

													return oParameterHeaderCode
													.map(phc->{
														return  new AbstractMap.SimpleEntry<>(phc, phc.toString(pl.getBuffer()));
													}).orElse(null);
												})
										.filter(m->m!=null)
										.collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue()));

		oStatusBits
		.ifPresent(sb->result.put(status, sb));

		return new Measurements(result);
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
