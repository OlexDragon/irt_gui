package irt.data.packet;

import irt.data.PacketWork;

public class AttenuationRangePacket extends PacketAbstract implements RangePacket{

	public AttenuationRangePacket( byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_CONFIGURATION_ATTENUATION_RANGE, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_CONFIG_ATTENUATION_RANGE, null, Priority.RANGE);
	}

}
