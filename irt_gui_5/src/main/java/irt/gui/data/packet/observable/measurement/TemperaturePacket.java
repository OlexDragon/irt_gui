package irt.gui.data.packet.observable.measurement;

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
import irt.gui.data.packet.interfaces.ValuePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class TemperaturePacket extends PacketAbstract implements ValuePacket{

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_TEMPERATURE;

	public TemperaturePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Temperature"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public TemperaturePacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public String getTitle() {
		return "Temperature";
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
		return " C";
	}
}
