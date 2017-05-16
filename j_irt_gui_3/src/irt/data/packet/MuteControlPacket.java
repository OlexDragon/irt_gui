
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class MuteControlPacket extends PacketAbstract {

	public MuteControlPacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				value!=null
					? PacketImp.PACKET_TYPE_COMMAND
					: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_MUTE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_MUTE,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
					: Priority.REQUEST);
	}
}
