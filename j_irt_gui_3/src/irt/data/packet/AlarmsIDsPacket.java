package irt.data.packet;

public class AlarmsIDsPacket extends PacketAbstract{

	public AlarmsIDsPacket(byte linkAddr){
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketImp.GROUP_ID_ALARM, PacketImp.ALARMS_IDs, null, 100);
	}
}
