package irt.data.packet;

import irt.data.PacketWork;

public class LOPacket  extends PacketAbstract{

	public LOPacket(byte linkAddr) {
		this(linkAddr, null);
	}

	public LOPacket(byte linkAddr, Byte id) {
		super(
				linkAddr,
				id!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_CONFIGURATION_LO, PacketImp.GROUP_ID_CONFIGURATION,
						PacketImp.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_SET,
						id,
						id!=null ? Priority.COMMAND : Priority.REQUEST);
	}
}
