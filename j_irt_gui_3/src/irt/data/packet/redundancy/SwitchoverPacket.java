package irt.data.packet.redundancy;

import java.nio.ByteBuffer;
import java.util.Optional;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;

public class SwitchoverPacket extends PacketSuper{

	public static final PacketIDs PACKET_ID = PacketIDs.REDUNDANCY_SWITCHOVER;

	public SwitchoverPacket(Byte linkAddr, Integer switchNumber) {
		super(
				linkAddr,
				Optional.ofNullable(switchNumber).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PACKET_ID,
				PacketGroupIDs.REDUNDANCY,
				PacketImp.PARAMETER_ID_REDUNDANCY_CONTROLLER_SWITCHOVER,
				Optional.ofNullable(switchNumber).map(i->ByteBuffer.allocate(4).putInt(i)).map(ByteBuffer::array).orElse(null),
				Optional.ofNullable(switchNumber).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
