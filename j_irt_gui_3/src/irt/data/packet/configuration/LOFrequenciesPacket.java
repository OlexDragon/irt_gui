package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.IdValueFreq;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.value.ValueFrequency;

public class LOFrequenciesPacket  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.map(toIdValueFreq());

	public LOFrequenciesPacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.CONFIGURATION_LO_FREQUENCIES,
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
		return parseValueFunction.apply(this);
	}

	private static Function<ByteBuffer, List<IdValueFreq>> toIdValueFreq() {
		return bb->{
			int size = bb.remaining();

			if(size%9 == 0)
				return biasBoardLOs(bb);

			return converterLOs(bb);
		};
	}

	private static List<IdValueFreq> converterLOs(ByteBuffer bb) {
		List<IdValueFreq> l = new ArrayList<>();

		final LongBuffer longBuffer = bb.asLongBuffer();

		while(longBuffer.position()<longBuffer.capacity()) {
			final long value = longBuffer.get();
			l.add(new IdValueFreq((byte) longBuffer.position(), new ValueFrequency(value, value, value)));
		}

		return l;
	}

	private static List<IdValueFreq> biasBoardLOs(ByteBuffer bb) {
		List<IdValueFreq> l = new ArrayList<>();
		while(bb.position()<bb.capacity()) {
			final byte freqId = bb.get();
			final long freq = bb.getLong();
			l.add(new IdValueFreq(freqId, new ValueFrequency(freq, freq, freq)));
		}
		return l;
	}
}
