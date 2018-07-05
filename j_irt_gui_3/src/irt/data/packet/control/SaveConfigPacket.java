package irt.data.packet.control;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;

public class SaveConfigPacket extends PacketSuper {

	public SaveConfigPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_COMMAND, PacketIDs.STORE_CONFIG, PacketImp.GROUP_ID_CONTROL, PacketImp.PACKET_ID_CONFIG_PROFILE_SAVE, null, Priority.COMMAND);
	}

	public SaveConfigPacket() {
		this((byte) 0);
	}
}
