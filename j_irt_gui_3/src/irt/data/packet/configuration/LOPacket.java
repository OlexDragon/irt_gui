package irt.data.packet.configuration;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.IdValueFreq;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.value.ValueFrequency;

public class LOPacket extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.map(bb->{
																											final int capacity = bb.capacity();
																											switch (capacity) {
																											case 1:
																												return bb.get();

																											case 8:
																												final long fr = bb.getLong();
																												return new ValueFrequency(fr, fr, fr);

																											default:
																												return bb;
																											}
																										});

	/**
	 *  Converter request packet
	 */
	public LOPacket() {
		this((byte) 0, null);
	}

	/**
	 * BIAS Board command packet
	 * @param linkAddr
	 * @param loId - select LO
	 */
	public LOPacket(Byte linkAddr, IdValueFreq idValueFreq) {
		super(
				linkAddr,
				Optional.ofNullable(idValueFreq).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST) ,
				PacketIDs.CONFIGURATION_LO,
				PacketGroupIDs.CONFIGURATION,
				Optional.ofNullable(linkAddr).filter(la->la!=0).map(v->PacketImp.PARAMETER_ID_CONFIGURATION_LO_SET).orElse(PacketImp.PARAMETER_CONFIG_FCM_FREQUENCY) ,
				Optional.ofNullable(idValueFreq).map(
						id->
						Optional
						.ofNullable(linkAddr)
						.filter(addr->addr!=0)
						.map(b->idValueFreq.getId())
						.map(Object.class::cast)
						.orElse(idValueFreq.getValueFrequency().getValue()))
				.map(v->PacketImp.toBytes(v))
				.orElse(null) ,
				Optional.ofNullable(idValueFreq).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
