
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class RedundancyStatusPacket extends ConfifurationPacket {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.filter(bb->bb.remaining()==1)
																										.flatMap(
																												bb->{
																													final int index = bb.get()&3;
																													return Optional
																															.of( RedundancyStatus.values())
																															.filter(vs->index<vs.length)
																															.map(vs->vs[index]);
																												});

	public RedundancyStatusPacket(byte linkAddr, RedundancyStatus redundancyStatus) {
		super(
				linkAddr,
				PacketIDs.CONFIGURATION_REDUNDANCY_STATUS,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_STATUS,
				Optional.ofNullable(redundancyStatus).map(v->redundancyStatus.toBytes()).orElse(null));
	}

	public RedundancyStatusPacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyStatus[] values = RedundancyStatus.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyStatus{
		UNKNOWN,
		ONLINE,
		STANDBY;
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
	}
}
