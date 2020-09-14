package irt.data.packet.configuration;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.RangePacket;

public class ALCRangePacket extends PacketSuper implements RangePacket{

	public ALCRangePacket( byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.CONFIGURATION_ALC_RANGE,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_RANGE,
				null,
				Priority.RANGE);
	}
}
