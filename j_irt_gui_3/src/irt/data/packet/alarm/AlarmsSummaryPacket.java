package irt.data.packet.alarm;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.interfaces.Packet;

public class AlarmsSummaryPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.filter(pl->pl.getParameterHeader().getSize()==4)
																										.map(pl->pl.getInt(0)&7)
																										.map(index->AlarmSeverities.values()[index]);

	public AlarmsSummaryPacket() {
		this((byte) 0);
	}

	public AlarmsSummaryPacket(byte linkAddr){
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketIDs.ALARMS_SUMMARY,
				PacketGroupIDs.ALARM,
				PacketImp.ALARM_SUMMARY_STATUS,
				null,
				Priority.ALARM);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
