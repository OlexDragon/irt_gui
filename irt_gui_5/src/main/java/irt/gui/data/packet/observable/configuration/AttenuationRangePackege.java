
package irt.gui.data.packet.observable.configuration;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class AttenuationRangePackege extends PacketAbstract implements LinkedPacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_ATTENUATION_RANGE;

	public AttenuationRangePackege() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get Attenuation range"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public AttenuationRangePackege(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

}