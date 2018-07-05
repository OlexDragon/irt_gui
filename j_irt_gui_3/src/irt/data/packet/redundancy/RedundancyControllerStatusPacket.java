package irt.data.packet.redundancy;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;

public class RedundancyControllerStatusPacket extends PacketSuper{

	public RedundancyControllerStatusPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.CONFIGURATION_REDUNDANCY_STATUS, PacketImp.GROUP_ID_REDUNDANCY_CONTROLLER, PacketImp.REDUNDANCY_CONTROLLER_STATUS, null, Priority.REQUEST);
	}

}
