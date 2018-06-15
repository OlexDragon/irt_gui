package irt.data.packet.configuration;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.interfaces.RangePacket;

public class ALCRangePacket extends PacketAbstract implements RangePacket{

	public ALCRangePacket( byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_ALC_RANGE,
				PacketImp.GROUP_ID_CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_APC_RANGE,
				null,
				Priority.RANGE);
	}
}
