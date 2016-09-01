
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
import irt.data.packets.interfaces.RangePacket;

public class LoFrequenciesPacket extends PacketAbstract implements RangePacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_LO_FREQUENCIES;

	public LoFrequenciesPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get Lo Frequencies"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public LoFrequenciesPacket( byte[] answer,  Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
