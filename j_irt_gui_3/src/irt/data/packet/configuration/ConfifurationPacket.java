
package irt.data.packet.configuration;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;

public class ConfifurationPacket extends PacketAbstract {

	public ConfifurationPacket(byte linkAddr, short packetIdConfiguration, byte parameterIdConfiguration, byte[] data) {
		super(
				linkAddr,
				data!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						packetIdConfiguration,
						PacketImp.GROUP_ID_CONFIGURATION,
						parameterIdConfiguration,
						data,
						data!=null ? Priority.COMMAND : Priority.REQUEST);
	}

	public ConfifurationPacket() {
		this((byte) 0,(short) 0, (byte) 0, null);
	}
}
