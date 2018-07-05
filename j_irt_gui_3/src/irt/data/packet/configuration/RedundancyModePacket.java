
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class RedundancyModePacket extends ConfifurationPacket {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.filter(bb->bb.remaining()==1)
																										.map(ByteBuffer::get)
																										.flatMap(
																												i->{
																													final int index = i&1;
																													return Optional
																															.of( RedundancyMode.values())
																															.map(v->v[index]);
																												});

	public RedundancyModePacket(byte linkAddr, RedundancyMode redundancyMode) {
		super(
				linkAddr,
				PacketIDs.CONFIGURATION_REDUNDANCY_MODE,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_MODE,
				Optional.ofNullable(redundancyMode).map(v->redundancyMode.toBytes()).orElse(null));
	}

	public RedundancyModePacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		final RedundancyMode[] values = RedundancyMode.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum RedundancyMode {
		COLD_STANDBY("Cold Standby"),
		HOT_STANDBY("Hot Standby");

		private String mode;

		private RedundancyMode(String mode){
			this.mode = mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
		@Override
		public String toString() {
			return mode;
		}
	}
}
