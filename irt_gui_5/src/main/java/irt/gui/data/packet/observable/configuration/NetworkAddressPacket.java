
package irt.gui.data.packet.observable.configuration;

import irt.gui.data.NetworkAddress;
import irt.gui.data.PacketIdDetails;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.ParameterHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;

public class NetworkAddressPacket extends PacketAbstract{


	public static final PacketId PACKET_ID = PacketId.CONFIGURATION_NETWORK_ADDRESS;

	public NetworkAddressPacket(NetworkAddress networkAddress) throws PacketParsingException {
		super(
				new PacketHeader(
						networkAddress==null ? PacketType.REQUEST : PacketType.COMMAND,
						new PacketIdDetails(PACKET_ID, networkAddress==null ? "Get network address" : "Set network address value to "+networkAddress),
						PacketErrors.NO_ERROR),
				new Payload(
						new ParameterHeader(
								PACKET_ID),
						networkAddress!=null ? networkAddress.toBytes() : null));
	}

	public NetworkAddressPacket(byte[] answer) throws PacketParsingException {
		super(PACKET_ID, answer);
	}
}
