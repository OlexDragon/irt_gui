package irt.gui.data.packet.observable.configuration;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class FrequencyPacket extends PacketAbstract {

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_FREQUENCY;

	public FrequencyPacket() throws PacketParsingException {
		this((Short)null);
	}

	public FrequencyPacket(Short value) throws PacketParsingException {
		super(
				new PacketHeader(
						value==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, value==null ? "Get Frequency value" : "Set Frequency value to "+value),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						value!=null ? Packet.toBytes(value) : null));
	}

	public FrequencyPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
