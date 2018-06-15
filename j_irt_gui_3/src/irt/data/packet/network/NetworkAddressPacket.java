package irt.data.packet.network;

import java.util.Optional;

import irt.data.network.NetworkAddress;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

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

	public NetworkAddressPacket() {
		this((byte) 0, null);
	}

	@Override
	public Object getValue() {
		return Optional
				.of(new NetworkAddress().set(this));
	}
}
