package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class SaveConfigPacket extends PacketAbstract {

	public SaveConfigPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_COMMAND, PacketWork.PACKET_ID_STORE_CONFIG, PacketImp.GROUP_ID_CONFIG_PROFILE, PacketImp.PACKET_ID_CONFIG_PROFILE_SAVE, null, Priority.COMMAND);
	}

	public SaveConfigPacket() {
		this((byte) 0);
	}

}
