package irt.gui.data.packet.observable.measurement;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.ValuePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class InputPowerPacket extends PacketAbstract implements ValuePacket{

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_INPUT_POWER;

	public InputPowerPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Temperature"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public InputPowerPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	@Override
	public String getTitle() {
		return "Input Power";
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}

	@Override
	public int getPrecision() {
		return 1;
	}

	@Override
	public String getPrefix() {
		return " dBm";
	}
}
