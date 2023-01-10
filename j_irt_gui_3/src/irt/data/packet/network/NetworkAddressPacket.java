package irt.data.packet.network;

import java.util.Optional;
import java.util.function.Function;

import irt.data.network.NetworkAddress;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.interfaces.Packet;

public class NetworkAddressPacket  extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(p->new NetworkAddress().set(p));

	public NetworkAddressPacket(byte linkAddr, NetworkAddress networkAddress) {
		super(
				linkAddr,
				networkAddress!=null ? PacketImp.PACKET_TYPE_COMMAND : PacketImp.PACKET_TYPE_REQUEST,
						PacketID.NETWORK_ADDRESS,
						PacketGroupIDs.NETWORK,
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
