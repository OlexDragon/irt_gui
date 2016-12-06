
package irt.packet.observable.configuration;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.packet.PacketHeader;
import irt.packet.PacketIdDetails;
import irt.packet.PacketParsingException;
import irt.packet.PacketProperties;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.enums.PacketErrors;
import irt.packet.enums.PacketId;
import irt.packet.enums.PacketType;
import irt.packet.interfaces.RangePacket;
import irt.packet.observable.PacketAbstract;

public class FrequencyRangePacket extends PacketAbstract implements RangePacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_FREQUENCY_RANGE;
	public static final PacketId FCM_PACKET_ID = PacketId.CONFIGURATION_FREQUENCY_RANGE_FCM;

	public FrequencyRangePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get ToolsFrequency range"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public FrequencyRangePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}


	@Override @JsonIgnore public PacketId getPacketId() {
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
