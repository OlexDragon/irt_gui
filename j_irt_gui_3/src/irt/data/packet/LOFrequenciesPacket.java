package irt.data.packet;

import irt.data.packet.interfaces.PacketWork;

public class LOFrequenciesPacket  extends PacketAbstract{

	public LOFrequenciesPacket(byte linkAddr) {
		super(
				linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES,
				PacketImp.GROUP_ID_CONFIGURATION,
				linkAddr!=0 ? PacketImp.PARAMETER_ID_CONFIGURATION_LO_FREQUENCIES : PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY_RANGE,
				null,
				Priority.RANGE);
	}
}
