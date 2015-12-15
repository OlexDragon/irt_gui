package irt.gui.data.packet.observable.configuration;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class AttenuationPacket extends PacketAbstract {

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_ATTENUATION;

	public AttenuationPacket() throws PacketParsingException {
		this((Short)null);
	}

	public AttenuationPacket(Short value) throws PacketParsingException {
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

	public AttenuationPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
