package irt.gui.data.packet.observable.measurement;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.ValuePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class InputPowerPacket extends PacketAbstract implements ValuePacket{

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_INPUT_POWER;
	public static final PacketId FCM_PACKET_ID = PacketId.MEASUREMENT_INPUT_POWER_FCM;

	public InputPowerPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Input ToolsPower"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public InputPowerPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public String getTitle() {
		return "Input ToolsPower";
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		final byte addr = linkHeader.getAddr();
		return addr==-1 ? FCM_PACKET_ID : PACKET_ID;
	}

	@Override @JsonIgnore
	public int getPrecision() {
		return 1;
	}

	@Override @JsonIgnore
	public String getPrefix() {
		return " dBm";
	}

	@Override synchronized public void setLinkHeaderAddr(byte addr) {

		if(addr == getLinkHeader().getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		final PacketHeader 	packetHeader	 = getPacketHeader();
		final PacketType 	packetType		 = packetHeader.getPacketType();
		final PacketIdDetails packetIdDetails = packetHeader.getPacketIdDetails();
		final PacketId 		packetId		 = addr==-1 ? FCM_PACKET_ID : PACKET_ID;

		setPacketHeader(new PacketHeader(
							packetType,
						new PacketIdDetails(packetId, packetIdDetails.getPacketDetails()),
						PacketErrors.NO_ERROR));

		final Payload 			payload			 = getPayloads().get(0);

		try {

			payload.setParameterHeader( new ParameterHeader(packetId));

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}
}
