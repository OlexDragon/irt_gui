
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class LnbSwitchPacket extends ConfigurationPacket {

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
																															.of( LnbPosition.values())
																															.map(v->v[index]);
																												});

	public LnbSwitchPacket(byte linkAddr, LnbPosition redundancyEnable) {
		super(
				linkAddr,
				PacketID.CONFIGURATION_DLRS_WGS_SWITCHOVER,
				PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER,
				Optional.ofNullable(redundancyEnable).map(v->redundancyEnable.toBytes()).orElse(null));
	}

	public LnbSwitchPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		final LnbPosition[] values = LnbPosition.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum LnbPosition{
		UNKNOWN,
		LNB1,
		LNB2;

		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
	}
}
