package irt.data.packet.redundancy;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class RedundancyControllerStatusPacket extends PacketAbstract{

	public RedundancyControllerStatusPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_REDUNDANCY_CONTROLLER_STATUS , PacketImp.GROUP_ID_REDUNDANCY_CONTROLLER, PacketImp.REDUNDANCY_CONTROLLER_STATUS, null, Priority.REQUEST);
	}

}
