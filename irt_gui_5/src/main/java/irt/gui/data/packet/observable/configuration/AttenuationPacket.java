package irt.gui.data.packet.observable.configuration;

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
import irt.gui.data.packet.interfaces.ConfigurationGroup;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class AttenuationPacket extends PacketAbstract5 implements ConfigurationGroup{

	public static final PacketId PACKET_ID 		= PacketId.CONFIGURATION_ATTENUATION;
	public static final PacketId FCM_PACKET_ID 	= PacketId.CONFIGURATION_ATTENUATION_FCM;

	public AttenuationPacket() throws PacketParsingException {
		this((Short)null);
	}

	public AttenuationPacket(Short value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, value==null ? "Get Attenuation value" : "Set Attenuation value to "+value),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						value!=null ? Packet.toBytes(value) : null));
	}

	public AttenuationPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore public PacketId getPacketId() {
		final byte addr = getLinkHeader().getAddr();
		return addr==-1 ? FCM_PACKET_ID : PACKET_ID;
	}

	@Override synchronized public boolean setLinkHeaderAddr(byte addr) {

		if(!super.setLinkHeaderAddr(addr))
			return false;

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
			return false;
		}
		return true;
	}
}
