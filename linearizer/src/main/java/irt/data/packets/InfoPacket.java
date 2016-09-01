package irt.data.packets;

import java.util.Optional;

import irt.controllers.serial_port.LinkedPacketSender;
import irt.data.packets.core.PacketAbstract;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.PacketProperties;
import irt.data.packets.core.ParameterHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketId;
import irt.data.packets.enums.PacketType;
import irt.data.packets.interfaces.WaitTime;

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

	public InfoPacket(byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(Optional.ofNullable(hasAcknowledgment).orElse(false)), answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override
	public int getWaitTime() {
		return LinkedPacketSender.STANDARD_WAIT_TIME;
	}
}