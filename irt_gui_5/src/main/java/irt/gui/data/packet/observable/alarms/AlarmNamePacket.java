
package irt.gui.data.packet.observable.alarms;

import irt.gui.errors.PacketParsingException;

public class AlarmNamePacket extends AlarmStatusPacket {

	public static final PacketId PACKET_ID = PacketId.ALARM_NAME;

	public AlarmNamePacket(short alarmId) throws PacketParsingException {
		super(PACKET_ID, "Get Alarm Name", alarmId);
	}

	public AlarmNamePacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

}
