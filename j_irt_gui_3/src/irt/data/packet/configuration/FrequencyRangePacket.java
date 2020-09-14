package irt.data.packet.configuration;

import irt.data.Range;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.RangePacket;

public class FrequencyRangePacket extends PacketSuper implements RangePacket{

	public FrequencyRangePacket( byte linkAddr) {
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.CONFIGURATION_FREQUENCY_RANGE,
				PacketGroupIDs.CONFIGURATION,
				linkAddr!=0 ? PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY_RANGE : PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY_RANGE,
				null,
				Priority.RANGE);
	}

	public FrequencyRangePacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(Range::new);
//		return getPayloads()
//				.stream()
//				.findAny()
//				.map(Payload::getBuffer)
//				.map(ByteBuffer::wrap)
//				.map(ByteBuffer::asLongBuffer)
//				.map(lb->{
//					final long[] dst = new long[lb.capacity()];
//					lb.get(dst);
//					return dst;
//				})
//				.map(Arrays::stream)
//				.orElse(LongStream.empty())
//				.mapToObj(fr->new ValueFrequency(fr, fr, fr))
//				.collect(Collectors.toList());
	}
}
