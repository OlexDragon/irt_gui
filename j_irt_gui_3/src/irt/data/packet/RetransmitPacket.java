package irt.data.packet;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.interfaces.Packet;

public class RetransmitPacket  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getByte);

	public RetransmitPacket(byte linkAddr, Byte retransmitsNumbet) {
		super(
				linkAddr,
				Optional.ofNullable(retransmitsNumbet).map(rn->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.PROTO_RETRANSNIT,
				PacketGroupIDs.PROTO,
				PacketImp.PARAMETER_ID_RETRANSMIT,
				Optional.ofNullable(retransmitsNumbet).map(b->new byte[]{b}).orElse(null),
				Optional.ofNullable(retransmitsNumbet).map(rn->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public RetransmitPacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(pl->pl.getByte())
				.orElse(null);
	}
}
