
package irt.gui.data.packet.observable.configuration;

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
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

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

	public LoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(hasAcknowledgment), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	public void setValue(byte value) {
		getPayloads().get(0).setBuffer(value);
	}
}
