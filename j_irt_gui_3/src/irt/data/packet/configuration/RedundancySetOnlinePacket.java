
package irt.data.packet.configuration;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;

public class RedundancySetOnlinePacket extends PacketSuper {

	public RedundancySetOnlinePacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_COMMAND,
				PacketIDs.CONFIGURATION_REDUNDANCY_SET_ONLINE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE,
				null,
				Priority.COMMAND);
	}
}
