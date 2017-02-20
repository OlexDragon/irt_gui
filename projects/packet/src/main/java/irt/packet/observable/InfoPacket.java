package irt.packet.observable;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import irt.packet.Packet;
import irt.packet.PacketHeader;
import irt.packet.PacketIdDetails;
import irt.packet.PacketParsingException;
import irt.packet.PacketProperties;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.data.DeviceInfo;
import irt.packet.enums.PacketErrors;
import irt.packet.enums.PacketId;
import irt.packet.enums.PacketType;
import irt.packet.interfaces.WaitTime;

//*********************************************   InfoPacket   ****************************************************************
public class InfoPacket extends PacketAbstract implements WaitTime{

	public static final PacketId PACKET_ID = PacketId.DEVICE_INFO;

	public InfoPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID,
								null),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public InfoPacket(@JsonProperty("asBytes") byte[] answer, @JsonProperty(defaultValue="false", value="v") Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override
	public int getWaitTime() {
		return Packet.STANDARD_WAIT_TIME;
	}

	@Override
	public Optional<? extends Object> getPacketValue() {
		return Optional
				.of(payloads)
				.filter(pls->!pls.isEmpty())
				.map(b->new DeviceInfo(InfoPacket.this));
	}
}