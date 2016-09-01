
package irt.data.packets;

import java.util.Optional;

import irt.data.packets.core.PacketAbstract;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.PacketProperties;
import irt.data.packets.core.ParameterHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketId;
import irt.data.packets.enums.PacketType;

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

	public LoPacket( byte[] answer,  Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	public void setValue(byte value) {
		getPayloads().get(0).setBuffer(value);
	}
}
