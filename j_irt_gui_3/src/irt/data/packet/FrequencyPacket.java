
package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.interfaces.ValueToString;
import irt.data.value.ValueFrequency;

public class FrequencyPacket extends PacketAbstract implements ValueToString{

	public FrequencyPacket(Byte linkAddr, Long value) {
		super(
				linkAddr,
				value!=null
				? PacketImp.PACKET_TYPE_COMMAND
						: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	public FrequencyPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Payload::getBuffer)
				.filter(b->b.length==16)
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
