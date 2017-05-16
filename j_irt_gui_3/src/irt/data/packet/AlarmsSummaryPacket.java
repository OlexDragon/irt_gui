package irt.data.packet;

import irt.data.packet.interfaces.DumpPacket;
import irt.data.packet.interfaces.PacketWork;

public class AlarmsSummaryPacket extends PacketAbstract implements DumpPacket{

	public AlarmsSummaryPacket(byte linkAddr){
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_ALARMS_SUMMARY, PacketImp.GROUP_ID_ALARM, PacketImp.ALARM_SUMMARY_STATUS, null, Priority.ALARM);
	}
}
