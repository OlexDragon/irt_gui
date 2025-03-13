package irt.data.packet.protocol;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.controller.serial_port.RecommendedStandard;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class PacketTranceverMode  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getByte)
																										.flatMap(b->RecommendedStandard.valueOf(b))
																										.map(Object.class::cast);

	public PacketTranceverMode(byte linkAddr, Byte mode) {
		super(
				linkAddr,
				Optional.ofNullable(mode).map(rn->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				Optional.ofNullable(mode).map(rn->PacketID.PROTO_TRANCEIVER_MODE_SET).orElse(PacketID.PROTO_TRANCEIVER_MODE),
				PacketGroupIDs.PROTOCOL,
				PacketImp.PARAMETER_TRANCEIVER_MODE,
				Optional.ofNullable(mode).map(b->new byte[]{b}).orElse(null),
				Optional.ofNullable(mode).map(rn->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public PacketTranceverMode() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		return getPayloads()
				.stream()
				.findAny()
				.map(pl->pl.getByte())
				.orElse(null);
	}
}
