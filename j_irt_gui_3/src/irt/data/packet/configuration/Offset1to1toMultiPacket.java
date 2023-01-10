
package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class Offset1to1toMultiPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.filter(pl->pl.getParameterHeader().getSize()>0)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.map(ByteBuffer::asShortBuffer)
																										.map(sb->{
																											short[] array = new short[sb.remaining()];
																											sb.get(array);
																											return array;
																										});

	/**
	 * 
	 * @param linkAddr	- unit address
	 * @param ofsetIndex - index of offset value, if null return all available indexes
	 * @param value - value for specified offset index. if null return all available indexes
	 */
	public Offset1to1toMultiPacket(Byte linkAddr, Byte ofsetIndex, Short value) {
		super(
				linkAddr,
				Optional.ofNullable(ofsetIndex).flatMap(i->Optional.ofNullable(value)).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketID.CONFIGURATION_OFFSET_1_TO_MULTI,
				PacketGroupIDs.CONFIGURATION,
				PacketImp.PARAMETER_CONFIG_BUC_OFFSET_1_TO_MULTI,
				Optional.ofNullable(ofsetIndex).map(i->Optional.ofNullable(value).map(v->ByteBuffer.allocate(3).put(i).putShort(v)).orElse(ByteBuffer.allocate(3).put(i)).array()).orElse(null),
				Optional.ofNullable(ofsetIndex).flatMap(i->Optional.ofNullable(value)).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}
}
