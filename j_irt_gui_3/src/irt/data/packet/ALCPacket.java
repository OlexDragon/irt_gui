
package irt.data.packet;

import irt.data.PacketWork;

public class ALCPacket extends PacketAbstract {

	public ALCPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				value!=null
					? PacketImp.PACKET_TYPE_COMMAND
					: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_ALC_LEVEL,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_LEVEL,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
					: Priority.REQUEST);
	}
}
