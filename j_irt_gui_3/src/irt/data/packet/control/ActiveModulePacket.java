package irt.data.packet.control;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class ActiveModulePacket extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																							.ofNullable(packet)
																							.map(Packet::getPayloads)
																							.map(List::stream)
																							.flatMap(Stream::findAny)
																							.map(Payload::getBuffer)
																							.map(ByteBuffer::wrap)
																							.filter(bb->bb.remaining()==1)
																							.map(ByteBuffer::get)
																							.map(b->b&0xFF);

	public ActiveModulePacket(Byte linkAddr, Byte value) {
		super(
				linkAddr,
				Optional.ofNullable(value).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketID.CONTROL_ACTIVE_MODULE,
				PacketGroupIDs.CONTROL,
				PacketImp.PACKET_ID_CONFIG_ACTIVE_MODULE_INDEX,
				Optional.ofNullable(value).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(value).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
