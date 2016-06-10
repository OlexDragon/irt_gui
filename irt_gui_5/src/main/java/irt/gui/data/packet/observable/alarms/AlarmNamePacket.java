
package irt.gui.data.packet.observable.alarms;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.errors.PacketParsingException;

public class AlarmNamePacket extends AlarmStatusPacket implements AlarmPacket {

	public static final PacketId PACKET_ID = PacketId.ALARM_NAME;

	public AlarmNamePacket(short alarmId) throws PacketParsingException {
		super(PACKET_ID, "Get Alarm Name", alarmId);
	}

	public AlarmNamePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, Optional.ofNullable(hasAcknowledgment).orElse(false));
	}

}
