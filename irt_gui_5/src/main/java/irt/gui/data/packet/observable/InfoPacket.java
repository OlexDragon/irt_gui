package irt.gui.data.packet.observable;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.errors.PacketParsingException;

//*********************************************   InfoPacket   ****************************************************************
public class InfoPacket extends PacketAbstract implements WaitTime{

	public static final PacketId PACKET_ID = PacketId.DEVICE_INFO;

	public InfoPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID,
								null),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public InfoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override
	public int getWaitTime() {
		return LinkedPacketSender.STANDARD_WAIT_TIME;
	}
}