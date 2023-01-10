
package irt.data.packet.configuration;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketSuper;

public class RedundancySetOnlinePacket extends PacketSuper {

	public RedundancySetOnlinePacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_COMMAND,
				PacketID.CONFIGURATION_REDUNDANCY_SET_ONLINE,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_SET_ONLINE,
				null,
				Priority.COMMAND);
	}
}
