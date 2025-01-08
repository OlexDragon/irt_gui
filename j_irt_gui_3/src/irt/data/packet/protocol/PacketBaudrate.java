package irt.data.packet.protocol;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.controller.serial_port.Baudrate;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class PacketBaudrate  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getLong)
																										.flatMap(Baudrate::valueOf)
																										.map(Object.class:: cast);

	public PacketBaudrate(byte linkAddr, Long baudrate) {
		super(
				linkAddr,
				Optional.ofNullable(baudrate).map(rn->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				Optional.ofNullable(baudrate).map(rn->PacketID.PROTO_BAUDRATE_SET).orElse(PacketID.PROTO_BAUDRATE),
				PacketGroupIDs.PROTOCOL,
				PacketImp.PARAMETER_BAUDRATE,
				Optional.ofNullable(baudrate).map(PacketImp::toBytes).orElse(null),
				Optional.ofNullable(baudrate).map(rn->Priority.COMMAND).orElse(Priority.REQUEST));
	}

	public PacketBaudrate() {
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

	@Override
	public int getMaxSize() {
		return 8;
	}
}
