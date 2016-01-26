package irt.data.packet;

import irt.data.PacketWork;

public class LOFrequenciesPacket  extends PacketAbstract{

	public LOFrequenciesPacket(byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_LO_FREQUENCIES, null, Priority.RANGE);
	}
}
