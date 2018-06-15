package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.IdValueFreq;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;

public class LOFrequenciesPacket  extends PacketAbstract{

	public LOFrequenciesPacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES,
				PacketImp.GROUP_ID_CONFIGURATION,
				linkAddr!=0 ? PacketImp.PARAMETER_ID_CONFIGURATION_LO_FREQUENCIES : PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY_RANGE,
				null,
				Priority.RANGE);
	}

	public LOFrequenciesPacket() {
		this((byte) 0);
	}

	@Override
	public Object getValue() {
		return Optional
				.ofNullable(getPayloads())
				.map(List::stream)
				.flatMap(Stream::findAny)
				.map(Payload::getBuffer)
				.map(ByteBuffer::wrap)
				.map(toIdValueFreq());
	}

	private Function<ByteBuffer, List<IdValueFreq>> toIdValueFreq() {
		return bb->{
			final byte addr = getLinkHeader().getAddr();

			if(addr != 0)
				return biasBoardLOs(bb);

			return onverterLOs(bb);
		};
	}

	private List<IdValueFreq> onverterLOs(ByteBuffer bb) {
		List<IdValueFreq> l = new ArrayList<>();

		final LongBuffer asLongBuffer = bb.asLongBuffer();

		while(asLongBuffer.position()<asLongBuffer.capacity()) {
			final long value = asLongBuffer.get();
			l.add(new IdValueFreq((byte) asLongBuffer.position(), new ValueFrequency(value, value, value)));
		}

		return l;
	}

	private List<IdValueFreq> biasBoardLOs(ByteBuffer bb) {
		List<IdValueFreq> l = new ArrayList<>();
		while(bb.position()<bb.capacity()) {
			final byte freqId = bb.get();
			final long freq = bb.getLong();
			l.add(new IdValueFreq(freqId, new ValueFrequency(freq, freq, freq)));
		}
		return l;
	}
}
