package irt.gui.data.packet.observable.alarms;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class AlarmIDsPacket extends PacketAbstract implements AlarmPacket{

	public static final PacketId PACKET_ID = PacketId.ALARMS;

	public AlarmIDsPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get available alarms"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public AlarmIDsPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

}
