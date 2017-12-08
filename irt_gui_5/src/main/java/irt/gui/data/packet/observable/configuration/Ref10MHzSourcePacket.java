
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
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class Ref10MHzSourcePacket extends PacketAbstract5{

	public static final PacketId PACKET_ID 			= PacketId.CONFIGURATION_10MHZ_SOURCE;

	public Ref10MHzSourcePacket() throws PacketParsingException {
		this(null);
	}

	public Ref10MHzSourcePacket(Byte value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(
								PACKET_ID, "Get 10 MHz source "),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						Optional.ofNullable(value).map(b->new byte[]{b}).orElse(null)));
	}

	public Ref10MHzSourcePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {

		return PACKET_ID;
	}

	public void setValue(Object value) {

		if(value instanceof Number)
			getPayloads().get(0).setBuffer(((Number)value).byteValue());
	}
}
