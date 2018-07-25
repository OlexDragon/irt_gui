package irt.data.packet.redundancy;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.module.ControlPanelIrPcFx.SwitchoverModes;

public class SwitchoverModePacket extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.filter(bb->bb.remaining()==1)
																										.map(ByteBuffer::get)
																										.map(Byte::intValue)
																										.map(SwitchoverModes::parse);

	public SwitchoverModePacket(Byte linkAddr, SwitchoverModes switchoverModes) {
		super(
				linkAddr,
				Optional.ofNullable(switchoverModes).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PacketIDs.REDUNDANCY_SWITCHOVER_MODE,
				PacketGroupIDs.REDUNDANCY,
				PacketImp.PARAMETER_ID_REDUNDANCY_CONTROLLER_SWITCHOVER_MODE,
				Optional.ofNullable(switchoverModes).map(SwitchoverModes::ordinal).map(Integer::byteValue).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(switchoverModes).map(v->Priority.COMMAND).orElse(Priority.REQUEST));

	}

}
