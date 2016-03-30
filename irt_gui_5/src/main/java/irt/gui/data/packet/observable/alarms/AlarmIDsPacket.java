package irt.gui.data.packet.observable.alarms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class AlarmIDsPacket extends RegirterAbstractPacket{

	public static final PacketId PACKET_ID = PacketId.ALARMS;

	public AlarmIDsPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get available alarms"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public AlarmIDsPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, hasAcknowledgment);
	}


	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
