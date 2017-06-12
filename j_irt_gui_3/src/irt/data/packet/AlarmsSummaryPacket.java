package irt.data.packet;

import java.util.Optional;

import irt.data.packet.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.interfaces.PacketWork;

public class AlarmsSummaryPacket extends PacketAbstract {

	public AlarmsSummaryPacket() {
		this((byte) 0);
	}

	public AlarmsSummaryPacket(byte linkAddr){
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				PacketWork.PACKET_ID_ALARMS_SUMMARY,
				PacketImp.GROUP_ID_ALARM,
				PacketImp.ALARM_SUMMARY_STATUS,
				null,
				Priority.ALARM);
	}

	@Override
	public Object getValue() {
		return Optional
				.ofNullable(getPayloads())
				.filter(pls->!pls.isEmpty())
				.map(pls->pls.parallelStream())
				.map(stream->{
					return stream
							.filter(pl->pl.getParameterHeader().getSize()==4)
							.map(pl->pl.getInt(0)&7)
							.findAny();
				})
				.filter(o->o.isPresent())
				.map(o->o.get())
				.map(index->AlarmSeverities.values()[index])	
				.map(Object.class::cast)
				.orElse(getHeader().getOptionStr());
	}
}
