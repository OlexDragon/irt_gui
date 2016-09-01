package irt.controllers;

import java.util.Optional;

import irt.data.packets.PacketIdDetails;
import irt.data.packets.PacketParsingException;
import irt.data.packets.core.Packet;
import irt.data.packets.core.PacketAbstract;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.PacketProperties;
import irt.data.packets.core.ParameterHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketId;
import irt.data.packets.enums.PacketType;
import irt.data.packets.interfaces.ConfigurationGroup;

public class FrequencyPacket extends PacketAbstract implements ConfigurationGroup{

	public static final PacketId PACKET_ID 		= PacketId.CONFIGURATION_FREQUENCY;
	public static final PacketId FCM_PACKET_ID 	= PacketId.CONFIGURATION_FREQUENCY_FCM;

	public FrequencyPacket() throws PacketParsingException {
		this((Long)null);
	}

	public FrequencyPacket(Long value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, value==null ? "Get ToolsFrequency value" : "Set ToolsFrequency value to "+value),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						value!=null ? Packet.toBytes(value) : null));
	}

	public FrequencyPacket( byte[] answer,  Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}


	@Override public PacketId getPacketId() {
		final byte addr = getLinkHeader().getAddr();
		return addr==-1 ? FCM_PACKET_ID : PACKET_ID;
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
