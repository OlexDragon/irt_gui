package irt.data.packet.alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class AlarmsIDsPacket extends PacketSuper{
	final static Logger logger = LogManager.getLogger();

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getArrayShort)
																										.map(MyArrayList::new);

	public AlarmsIDsPacket() {
		this((byte) 0);
	}

	public AlarmsIDsPacket(byte linkAddr){
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.ALARMS_ALL_IDs, PacketGroupIDs.ALARM, PacketImp.ALARMS_IDs, null, Priority.RANGE);
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
