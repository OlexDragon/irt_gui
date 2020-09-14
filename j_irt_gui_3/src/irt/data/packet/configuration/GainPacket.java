package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

import irt.controller.translation.Translation;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.ValueToString;

public class GainPacket extends PacketSuper implements ValueToString{

	public GainPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.CONFIGURATION_GAIN,
				PacketGroupIDs.CONFIGURATION,
				Optional.ofNullable(linkAddr).filter(b->b!=0).map(b->PacketImp.PARAMETER_ID_CONFIGURATION_GAIN).orElse(PacketImp.PARAMETER_CONFIG_FCM_GAIN),
				Optional.ofNullable(value).map(v->PacketImp.toBytes(value)).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public GainPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Payload::getBuffer)
				.map(ByteBuffer::wrap)
				.map(ByteBuffer::getShort);
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
