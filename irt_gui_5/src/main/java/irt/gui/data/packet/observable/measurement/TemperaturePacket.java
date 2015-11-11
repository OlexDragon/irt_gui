package irt.gui.data.packet.observable.measurement;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class TemperaturePacket extends PacketAbstract implements AlarmPacket{

	public static final PacketId PACKET_ID = PacketId.MEASUREMENT_TEMPERATURE;

	public TemperaturePacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Temperature"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public TemperaturePacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}
}
