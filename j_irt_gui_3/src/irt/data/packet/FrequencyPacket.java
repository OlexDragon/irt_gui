
package irt.data.packet;

import java.nio.ByteBuffer;

import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;

public class FrequencyPacket extends PacketAbstract {

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
				.map(ByteBuffer::wrap)
				.map(ByteBuffer::getLong)
				.map(fr->new ValueFrequency(fr, fr, fr));
	}
}
