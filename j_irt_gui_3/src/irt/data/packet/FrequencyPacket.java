
package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class FrequencyPacket extends PacketAbstract {

	public FrequencyPacket(Byte linkAddr, Long value) {
		super(
				linkAddr,
				value!=null
				? PacketImp.PACKET_TYPE_COMMAND
						: PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY,
				value!=null ? PacketImp.toBytes(value) : null,
				value!=null ? Priority.COMMAND : Priority.REQUEST);
	}
}
