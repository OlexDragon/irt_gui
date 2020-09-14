
package irt.data.packet.configuration;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;

public class OffsetRange extends PacketSuper {

	public OffsetRange(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.CONFIGURATION_OFFSET_RANGE, PacketGroupIDs.CONFIGURATION, PacketImp.PARAMETER_CONFIG_BUC_OFFSET_RANGE, null, Priority.RANGE);
	}

}
