package irt.data.packet.redundancy;

import java.nio.ByteBuffer;
import java.util.Optional;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class SwitchoverPacket extends PacketAbstract{

	public static final short PACKET_ID = PacketWork.PACKET_ID_SWITCHOVER;

	public SwitchoverPacket(Byte linkAddr, Integer switchNumber) {
		super(
				linkAddr,
				Optional.ofNullable(switchNumber).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PACKET_ID,
				PacketImp.GROUP_ID_REDUNDANCY_CONTROLLER,
				PacketImp.REDUNDANCY_CONTROLLER_SWITCHOVER,
				Optional.ofNullable(switchNumber).map(i->ByteBuffer.allocate(4).putInt(i)).map(ByteBuffer::array).orElse(null),
				Optional.ofNullable(switchNumber).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
