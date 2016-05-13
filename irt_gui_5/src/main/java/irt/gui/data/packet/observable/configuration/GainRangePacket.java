
package irt.gui.data.packet.observable.configuration;

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
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class GainRangePacket extends PacketAbstract implements RangePacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_GAIN_RANGE;

	public GainRangePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get gain range"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public GainRangePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(hasAcknowledgment), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
