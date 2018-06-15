package irt.data.packet.control;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class ModuleListPacket extends PacketAbstract{

	public ModuleListPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_MODULE_LIST, PacketImp.GROUP_ID_CONTROL, PacketImp.PACKET_ID_CONFIG_MODULE_LIST, null, Priority.REQUEST);
	}

}
