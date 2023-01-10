package irt.data.packet.control;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;

public class SaveConfigPacket extends PacketSuper {

	public SaveConfigPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_COMMAND, PacketID.STORE_CONFIG, PacketGroupIDs.CONTROL, PacketImp.PACKET_ID_CONFIG_PROFILE_SAVE, null, Priority.COMMAND);
	}

	public SaveConfigPacket() {
		this((byte) 0);
	}
}
