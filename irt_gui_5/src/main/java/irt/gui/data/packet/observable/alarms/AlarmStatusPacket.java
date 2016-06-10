package irt.gui.data.packet.observable.alarms;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class AlarmStatusPacket extends RegirterAbstractPacket implements AlarmPacket{

	public static final PacketId PACKET_ID = PacketId.ALARM_STATUS;

	public AlarmStatusPacket(short alarmId) throws PacketParsingException {
		this(PACKET_ID, "Get Status", alarmId);
	}

	protected AlarmStatusPacket(PacketId packetId, String detils, short alarmId) throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(packetId, detils),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								packetId),
						Packet.shortToBytes(alarmId)));
	}

	public AlarmStatusPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	protected AlarmStatusPacket(PacketId packetId, byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	public enum AlarmSeverities{
		NO_ALARM	( "no_alarm"),
		INFO		( "info"),
		WARNING		( "warning"	),
		MINOR		( "warning"	),
		MAJOR		( "alarm"	),
		CRITICAL	( "alarm"	);

		private String styleClass; 		public String getStyleClass() { return styleClass; }

		private AlarmSeverities(String className){
			this.styleClass = className;
		}
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
