package irt.gui.data.packet.observable.alarms;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class AlarmStatusPacket extends PacketAbstract implements AlarmPacket{

	public static final PacketId PACKET_ID = PacketId.ALARM_STATUS;

	public AlarmStatusPacket(short alarmId) throws PacketParsingException {
		this(PACKET_ID, "Get Summary Status", alarmId);
	}

	protected AlarmStatusPacket(PacketId packetId, String detils, short alarmId) throws PacketParsingException {
		super(
				new PacketHeader(
						PacketType.REQUEST,
						new PacketIdDetails(packetId, detils),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								packetId),
						Packet.shortToBytes(alarmId)));
	}

	public AlarmStatusPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}

	protected AlarmStatusPacket(PacketId packetId, byte[] answer) throws PacketParsingException {
		super(packetId, answer);
	}

	public enum AlarmSeverities{
		NO_ALARM,
		INFO,
		WARNING,
		MINOR,
		MAJOR,
		CRITICAL
	}
}
