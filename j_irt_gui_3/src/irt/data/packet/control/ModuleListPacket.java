package irt.data.packet.control;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class ModuleListPacket extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(bytes->{

																											// find end points
																											final int[] array = IntStream.range(0, bytes.length).filter(b->bytes[b]==0).toArray();

																											Map<Byte, String> map = new HashMap<>();

																											int start = 0;
																											for(int end=0; ;){
																												map.put(bytes[start], new String(Arrays.copyOfRange(bytes, ++start, array[end])));

																												start = array[end] + 1;

																												if(++end >= array.length)
																													break;
																											}
																											return map;
																										});

	public ModuleListPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.CONTRO_MODULE_LIST, PacketImp.GROUP_ID_CONTROL, PacketImp.PACKET_ID_CONFIG_MODULE_LIST, null, Priority.REQUEST);
	}

}
