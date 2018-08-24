
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class RedundancyNamePacket extends ConfigurationPacket {

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
																															.of( RedundancyName.values())
																															.map(v->v[index]);
																												});

	public RedundancyNamePacket(byte linkAddr, RedundancyName redundancyName) {
		super(
				linkAddr,
				PacketIDs.CONFIGURATION_REDUNDANCY_NAME,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_NAME,
				Optional.ofNullable(redundancyName).map(v->redundancyName.toBytes()).orElse(null));
	}


	public RedundancyNamePacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyName[] values = RedundancyName.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyName {
		NO_NAME(null),
		BUC_A("BUC A"),
		BUC_B("BUC B");

		private String name;

		private RedundancyName(String name){
			this.name = name;
		}
		public byte[] toBytes() {
			return new byte[]{(byte) ordinal()};
		}
		@Override
		public String toString() {
			return name;
		}
	}
}
