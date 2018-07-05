package irt.data.packet.alarm;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import irt.data.StringData;

import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class AlarmDescriptionPacket extends AlarmStatusPacket {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getStringData)
																										.map(StringData::toString);

	public AlarmDescriptionPacket(byte addr, AlarmsPacketIds alarmsPacketId) {
		super(addr, PacketImp.ALARM_DESCRIPTION, alarmsPacketId);
	}

}
