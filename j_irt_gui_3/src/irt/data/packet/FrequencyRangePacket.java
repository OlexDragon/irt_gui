package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;

public class FrequencyRangePacket extends PacketAbstract implements RangePacket{

	public FrequencyRangePacket( byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY_RANGE, null, Priority.RANGE);
	}

	public FrequencyRangePacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Payload::getBuffer)
				.map(ByteBuffer::wrap)
				.map(ByteBuffer::asLongBuffer)
				.map(lb->{
					final long[] dst = new long[lb.capacity()];
					lb.get(dst);
					return dst;
				})
				.map(Arrays::stream)
				.orElse(LongStream.empty())
				.mapToObj(fr->new ValueFrequency(fr, fr, fr))
				.collect(Collectors.toList());
	}
}
