
package irt.gui.data.packet.observable;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.errors.PacketParsingException;

public class ConverterStoreConfigPacket extends PacketAbstract5 {

	public static final PacketId PACKET_ID 		= PacketId.SAVE_CONFIG_PROFILE;

	public ConverterStoreConfigPacket() throws PacketParsingException {
		super(new PacketHeader(PacketType.COMMAND,
								new PacketIdDetails(PACKET_ID, "Save converter configuration"),
								PacketErrors.NO_ERROR),
				new Payload(new ParameterHeader(PACKET_ID), null));
	}

	public ConverterStoreConfigPacket(byte[] answer) throws PacketParsingException {
		super(new PacketProperties(PACKET_ID).setHasAcknowledgment(true), answer);
	}

	@Override public PacketId getPacketId() { return PACKET_ID; }
}
