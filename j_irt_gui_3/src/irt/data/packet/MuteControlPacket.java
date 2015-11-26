
package irt.data.packet;

import irt.data.PacketWork;

public class MuteControlPacket extends PacketAbstract {

	public MuteControlPacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				value!=null
					? PacketImp.PACKET_TYPE_COMMAND
					: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_MUTE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_PICOBUC_CONFIGURATION_MUTE,
				value,
				value!=null
					? Priority.COMMAND
					: Priority.REQUEST);
	}
}
