
package irt.data.packet.configuration;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

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
