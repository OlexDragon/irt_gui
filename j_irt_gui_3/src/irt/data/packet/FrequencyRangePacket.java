package irt.data.packet;

import irt.data.PacketWork;

public class FrequencyRangePacket extends PacketAbstract implements RangePacket{

	public FrequencyRangePacket( byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_CONFIGURATION_FREQUENCY_RANGE, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_USER_FREQUENCY_RANGE, null, Priority.RANGE);
	}

}
