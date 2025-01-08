package irt.data.packet.protocol;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class PacketUnitAddress  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getByte);

	public PacketUnitAddress(byte linkAddr, Byte retransmitsNumbet) {
		super(
				linkAddr,
				Optional.ofNullable(retransmitsNumbet).map(rn->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				Optional.ofNullable(retransmitsNumbet).map(rn->PacketID.PROTO_UNIT_ADDRESS_SET).orElse(PacketID.PROTO_UNIT_ADDRESS),
				PacketGroupIDs.PROTOCOL,
				PacketImp.PARAMETER_ADDRESS,
				Optional.ofNullable(retransmitsNumbet).map(b->new byte[]{b}).orElse(null),
				Optional.ofNullable(retransmitsNumbet).map(rn->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public PacketUnitAddress() {
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
