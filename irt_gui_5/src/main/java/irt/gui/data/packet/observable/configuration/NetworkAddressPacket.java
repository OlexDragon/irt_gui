
package irt.gui.data.packet.observable.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.NetworkAddress;
import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

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

	public NetworkAddressPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(hasAcknowledgment), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
