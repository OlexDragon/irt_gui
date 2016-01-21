
package irt.gui.data.packet.observable.configuration;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class FrequencyRangePacket extends PacketAbstract implements RangePacket{

	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_FREQUENCY_RANGE;

	public FrequencyRangePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(
								PACKET_ID, "Get Frequency range"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(PACKET_ID),
						null));
	}

	public FrequencyRangePacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
