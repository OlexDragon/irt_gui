
package irt.data.packet.configuration;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;

public class OffsetRange extends PacketSuper {

	protected OffsetRange(Byte linkAddr, byte packetType, short packetId, byte groupId, byte parameterHeaderCode, byte[] payloadData, Priority priority) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.CONFIGURATION_OFFSET_RANGE, groupId, parameterHeaderCode, payloadData, priority);
	}

}
