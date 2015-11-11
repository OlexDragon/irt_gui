package irt.data.packet;

public class AlarmDescriptionPacket extends AlarmStatusPacket {

	public AlarmDescriptionPacket(byte addr, short alarmId) {
		super(addr, PacketImp.ALARM_DESCRIPTION, alarmId);
	}

}
