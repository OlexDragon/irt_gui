
package irt.data.packet;

import irt.data.PacketWork;

public class RedundancySetOnlinePacket extends PacketAbstract {

	public RedundancySetOnlinePacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_COMMAND,
				PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE,
				null,
				Priority.COMMAND);
	}
}
