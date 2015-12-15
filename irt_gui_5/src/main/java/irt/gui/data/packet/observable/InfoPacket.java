package irt.gui.data.packet.observable;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

//*********************************************   InfoPacket   ****************************************************************
public class InfoPacket extends PacketAbstract{

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

	public InfoPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}