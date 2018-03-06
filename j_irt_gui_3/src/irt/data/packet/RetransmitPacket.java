package irt.data.packet;

import java.util.Optional;

import irt.data.packet.interfaces.PacketWork;

public class RetransmitPacket  extends PacketAbstract{

	public RetransmitPacket(byte linkAddr, Byte retransmitsNumbet) {
		super(
				linkAddr,
				Optional.ofNullable(retransmitsNumbet).map(rn->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketWork.PACKET_ID_PROTO_RETRANSNIT,
				PacketImp.GROUP_ID_PROTO,
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
