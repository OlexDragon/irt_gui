
package irt.data.packet;

import irt.data.PacketWork;

public class AttenuationPacket extends PacketAbstract {

	public AttenuationPacket(Byte linkAddr, Short value) {
		super(
				linkAddr,
				value!=null
				? PacketImp.PACKET_TYPE_COMMAND
						: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_PICOBUC_CONFIGURATION_ATTENUATION,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null
					? Priority.COMMAND
						: Priority.REQUEST);
	}
}
