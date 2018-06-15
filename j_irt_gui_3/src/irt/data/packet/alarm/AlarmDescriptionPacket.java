package irt.data.packet.alarm;

import irt.data.packet.PacketImp;

public class AlarmDescriptionPacket extends AlarmStatusPacket {

	public AlarmDescriptionPacket(byte addr, short alarmId) {
		super(addr, PacketImp.ALARM_DESCRIPTION, alarmId);
	}

}
