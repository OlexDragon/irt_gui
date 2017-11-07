package irt.data.packet;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import irt.data.packet.interfaces.PacketWork;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCode;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBitsBUC;
import irt.tools.fx.MonitorPanelFx.StatusBitsFCM;

public class MeasurementPacket extends PacketAbstract{

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
				PacketWork.PACKET_ID_MEASUREMENT_ALL,
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
		final Optional<Object[]> oStatusBits = collect
											.get(true)
											.stream()
											.map(pl->pl.getInt(0))
											.map(statusBits->(Object[])(isConverter ? StatusBitsFCM.parse(statusBits) : StatusBitsBUC.parse(statusBits)))
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
