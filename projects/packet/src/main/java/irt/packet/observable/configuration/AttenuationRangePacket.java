
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

public class AttenuationRangePacket extends PacketAbstract implements RangePacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_ATTENUATION_RANGE;

	public AttenuationRangePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get Attenuation range"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public AttenuationRangePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
