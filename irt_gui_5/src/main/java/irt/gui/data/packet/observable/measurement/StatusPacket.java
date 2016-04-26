package irt.gui.data.packet.observable.measurement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class StatusPacket extends PacketAbstract{

//	private static final Logger l = LogManager.getLogger();

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_STATUS;

	public StatusPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get status byte"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						null));
	}

	public StatusPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, hasAcknowledgment);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
