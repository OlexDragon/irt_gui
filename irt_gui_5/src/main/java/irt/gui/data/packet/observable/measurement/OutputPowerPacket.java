package irt.gui.data.packet.observable.measurement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.ValuePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class OutputPowerPacket extends PacketAbstract implements ValuePacket{

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_OUTPUT_POWER;

	public OutputPowerPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Output Power"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public OutputPowerPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") boolean hasAcknowledgment) throws PacketParsingException {
		super(PACKET_ID, answer, hasAcknowledgment);
	}

	@Override @JsonIgnore
	public String getTitle() {
		return "Output Power";
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override @JsonIgnore
	public int getPrecision() {
		return 1;
	}

	@Override @JsonIgnore
	public String getPrefix() {
		return " dBm";
	}
}