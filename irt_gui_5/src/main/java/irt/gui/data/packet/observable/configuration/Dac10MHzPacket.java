package irt.gui.data.packet.observable.configuration;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.ConfigurationGroup;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class Dac10MHzPacket extends PacketAbstract5 implements ConfigurationGroup{

	public static final PacketId PACKET_ID 		= PacketId.CONFIGURATION_10MHZ_DAC;

	public Dac10MHzPacket() throws PacketParsingException {
		this((Integer)null);
	}

	public Dac10MHzPacket(Integer value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, value==null ? "Get Attenuation value" : "Set Attenuation value to "+value),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						value!=null ? Packet.toBytes(value) : null));
	}

	public Dac10MHzPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override synchronized public boolean setLinkHeaderAddr(byte addr) {

		if(!super.setLinkHeaderAddr(addr))
			return false;

		final PacketHeader 	packetHeader	 = getPacketHeader();
		final PacketType 	packetType		 = packetHeader.getPacketType();
		final PacketIdDetails packetIdDetails = packetHeader.getPacketIdDetails();

		setPacketHeader(new PacketHeader(
							packetType,
						new PacketIdDetails(PACKET_ID, packetIdDetails.getPacketDetails()),
						PacketErrors.NO_ERROR));

		final Payload 			payload			 = getPayloads().get(0);

		try {

			payload.setParameterHeader( new ParameterHeader(PACKET_ID));

		} catch (PacketParsingException e) {
			logger.catching(e);
			return false;
		}
		return true;
	}

	@Override
	public void setAnswer(byte[] answer) {
		super.setAnswer(answer);
	}
}
