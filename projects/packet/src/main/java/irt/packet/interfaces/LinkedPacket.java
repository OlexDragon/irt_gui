
package irt.packet.interfaces;

import java.util.List;

import irt.packet.PacketHeader;
import irt.packet.Payload;
import irt.packet.enums.PacketId;

public interface LinkedPacket extends PacketToSend{

	PacketId 		getPacketId();

	PacketHeader	getPacketHeader();
	List<Payload>	getPayloads();

	void 			deleteObservers();

	public static final int PACKET_ACKNOWLEDGEMENT_SIZE = 11;
}
