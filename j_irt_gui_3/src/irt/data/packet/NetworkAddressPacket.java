package irt.data.packet;

import irt.data.PacketWork;
import irt.data.network.NetworkAddress;

public class NetworkAddressPacket  extends PacketAbstract{

	public NetworkAddressPacket(byte linkAddr, NetworkAddress networkAddress) {
		super(
				linkAddr,
				networkAddress!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketWork.PACKET_ID_NETWORK_ADDRESS,
						PacketImp.GROUP_ID_NETWORK,
						PacketImp.PARAMETER_ID_NETWORK_ADDRESS,
						networkAddress!=null ? networkAddress.toBytes() : null,
						networkAddress!=null ? Priority.COMMAND : Priority.REQUEST);
	}
}
