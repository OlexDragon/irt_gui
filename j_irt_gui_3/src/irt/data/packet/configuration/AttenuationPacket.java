
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.controller.translation.Translation;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.ValueToString;

public class AttenuationPacket extends PacketSuper implements ValueToString{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(pl->pl.getBuffer())
																										.filter(b->b.length==2)
																										.map(ByteBuffer::wrap)
																										.map(ByteBuffer::getShort);

	public AttenuationPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONFIGURATION_ATTENUATION,
				PacketGroupIDs.CONFIGURATION,
				Optional.ofNullable(linkAddr).filter(b->b!=0).map(b->PacketImp.PARAMETER_ID_CONFIGURATION_ATTENUATION).orElse(PacketImp.PARAMETER_CONFIG_FCM_ATTENUATION),
				Optional.ofNullable(value).map(v->PacketImp.toBytes(value)).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public AttenuationPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {

		return parseValueFunction.apply(this);
	}

	@Override
	public void setValue(Object value) {

		if(value instanceof Number)
			value = ((Number)value).shortValue();
		if(value instanceof String) {
			final Optional<String> optional = Optional.of(((String)value).replaceAll("[^\\d.E-]", "")).filter(text->!text.isEmpty());

			if(!optional.isPresent())
				return;

			final String str = optional.get();
			double v = Optional.of(Double.parseDouble(str)).filter(sp->sp>=0.1).orElse(0.1);
			value = (short) (v*10);
		}

		super.setValue(value);
	}

	@Override
	public String valueToString() {

		final Optional<?> optional = (Optional<?>)getValue();

		if(!optional.isPresent())
			return "N/A";

		return valueToString((short)optional.get());
	}

	private NumberFormat df = new DecimalFormat("0.0");
	@Override
	public String valueToString(Number value) {
		double v = value.shortValue()/10.0;
		return df.format(v) + " " + Translation.getValue(String.class, "db", "dB");
	}
}
