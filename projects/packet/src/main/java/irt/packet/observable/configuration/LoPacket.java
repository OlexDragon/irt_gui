
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
import irt.packet.observable.PacketAbstract;

public class LoPacket extends PacketAbstract{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_LO;

	public LoPacket() throws PacketParsingException {
		this(null);
	}

	public LoPacket(Byte value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(
								PACKET_ID, "Get Lo Frequencies"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						Optional.ofNullable(value).map(b->new byte[]{b}).orElse(null)));
	}

	public LoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	public void setValue(byte value) {
		getPayloads().get(0).setBuffer(value);
	}
}
