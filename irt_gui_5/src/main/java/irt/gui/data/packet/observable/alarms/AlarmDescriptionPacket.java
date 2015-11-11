
package irt.gui.data.packet.observable.alarms;

import irt.gui.errors.PacketParsingException;

public class AlarmDescriptionPacket extends AlarmStatusPacket {

	public static final PacketId PACKET_ID = PacketId.ALARM_DESCRIPTION;

	public AlarmDescriptionPacket(short alarmId) throws PacketParsingException {
		super(PACKET_ID, "Get Alarm Description", alarmId);
	}

	public AlarmDescriptionPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

}
