
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class ALCEnablePacket extends PacketAbstract {

	public ALCEnablePacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				value!=null
					? PacketImp.PACKET_TYPE_COMMAND
					: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_ALC_ENABLE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_ENABLE,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
					: Priority.REQUEST);
	}
}
