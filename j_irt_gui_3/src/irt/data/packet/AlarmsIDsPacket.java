package irt.data.packet;

import irt.data.PacketWork;

public class AlarmsIDsPacket extends PacketAbstract{

	public AlarmsIDsPacket(byte linkAddr){
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_ALARMS_IDs, PacketImp.GROUP_ID_ALARM, PacketImp.ALARMS_IDs, null, Priority.ALARM);
	}
}
