package irt.gui.data.packet.observable.alarms;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.RegirterAbstractPacket;
import irt.gui.errors.PacketParsingException;

public class AlarmSummaryStatusPacket extends RegirterAbstractPacket{

	public static final PacketId PACKET_ID = PacketId.ALARM_SUMMARY_STATUS;

	public AlarmSummaryStatusPacket() throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(PACKET_ID, "Get Summary Status"),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID)));
	}

	public AlarmSummaryStatusPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	public enum AlarmSeverities{
		NO_ALARM,
		INFO,
		WARNING,
		MINOR,
		MAJOR,
		CRITICAL
	}

	@Override
	public PacketId getPacketId() {
		return PACKET_ID;
	}
}
