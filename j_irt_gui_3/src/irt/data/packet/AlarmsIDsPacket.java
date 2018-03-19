package irt.data.packet;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.interfaces.PacketWork;

public class AlarmsIDsPacket extends PacketAbstract{
	final static Logger logger = LogManager.getLogger();

	public AlarmsIDsPacket() {
		this((byte) 0);
	}

	public AlarmsIDsPacket(byte linkAddr){
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PacketIDs.ALARMS_IDs.getId(), PacketImp.GROUP_ID_ALARM, PacketImp.ALARMS_IDs, null, Priority.ALARM);
	}

	@Override
	public Object getValue() {
		return Optional
				.ofNullable(getPayloads())
				.flatMap(pls->pls.stream().filter(pl->pl.getBuffer()!=null).map(Payload::getArrayShort).findAny())
				.map(MyArrayList::new)
				.map(Object.class::cast)
				.orElse("N/A");
	}

	public static class MyArrayList extends ArrayList<Integer>{
		private static final long serialVersionUID = -8574611953700905786L;

		public MyArrayList(short[] bytes) {
			IntStream.range(0, bytes.length).forEach(index->add(bytes[index]&0xFF));
		}

		@Override
		public String toString() {
			return stream().map(Object::toString).collect(Collectors.joining(", "));
		}
		
	}
}
