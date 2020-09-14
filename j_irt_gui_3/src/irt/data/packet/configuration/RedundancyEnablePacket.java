
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class RedundancyEnablePacket extends ConfigurationPacket {

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
																													final int index = bb.get()&1;
																													return Optional
																															.of( RedundancyEnable.values())
																															.map(v->v[index]);
																												});

	public RedundancyEnablePacket(byte linkAddr, RedundancyEnable redundancyEnable) {
		super(
				linkAddr,
				PacketIDs.CONFIGURATION_REDUNDANCY_ENABLE,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_ENABLE,
				Optional.ofNullable(redundancyEnable).map(v->redundancyEnable.toBytes()).orElse(null));
	}

	public RedundancyEnablePacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyEnable[] values = RedundancyEnable.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyEnable{
		DISABLE("Desable"),
		ENABLE("Enable");
		
		private String redundancy;

		private RedundancyEnable(String redundancy){
			this.redundancy = redundancy;
		}
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
		public void setRedundancy(String redundancy) {
			this.redundancy = redundancy;
		}
		@Override
		public String toString() {
			return redundancy;
		}
	}
}
