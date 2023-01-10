
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.ValueToString;
import irt.data.value.ValueFrequency;

public class LnbSwitchOverPacket extends PacketSuper implements ValueToString{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.filter(b->b!=null && b.length==1)
																										.map(b->b[0]&0xff);

	public LnbSwitchOverPacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketID.CONFIGURATION_LNB_SWITCH_OVER,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER,
				PacketImp.toBytes(value),
				Optional.ofNullable(value).filter(la->la!=0).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public LnbSwitchOverPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Payload::getBuffer)
				.filter(b->b.length==8)
				.map(ByteBuffer::wrap)
				.map(ByteBuffer::getLong);
	}

	@Override
	public void setValue(Object value) {

		if(value instanceof Number)
			value = ((Number)value).longValue();
		if(value instanceof String) {
			final String str = (String)value;
			value = new ValueFrequency(str, str, str).getValue();
		}

		super.setValue(value);
	}

	@Override
	public String valueToString() {

		final Optional<?> optional = (Optional<?>)getValue();

		if(!optional.isPresent())
			return "N/A";

		return valueToString((long)optional.get());
	}

	@Override
	public String valueToString(Number value) {
		long v = value.longValue();
		return new ValueFrequency(v, v, v).toString();
	}
}
