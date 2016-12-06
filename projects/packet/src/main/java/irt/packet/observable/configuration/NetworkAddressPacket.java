
package irt.packet.observable.configuration;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.data.NetworkAddress;
import irt.packet.PacketHeader;
import irt.packet.PacketIdDetails;
import irt.packet.PacketParsingException;
import irt.packet.PacketProperties;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.enums.PacketErrors;
import irt.packet.enums.PacketId;
import irt.packet.enums.PacketType;
import irt.packet.observable.PacketAbstract;

public class NetworkAddressPacket extends PacketAbstract{


	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_NETWORK_ADDRESS;

	public NetworkAddressPacket(NetworkAddress networkAddress) throws PacketParsingException {
		super(
				new PacketHeader(
						networkAddress==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, networkAddress==null ? "Get network address" : "Set network address value to "+networkAddress),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						networkAddress!=null ? networkAddress.toBytes() : null));
	}

	public NetworkAddressPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
