
package irt.data.packets.interfaces;

import java.util.List;

import irt.data.packets.core.PacketHeader;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketId;

public interface LinkedPacket extends PacketToSend{

	PacketId 		getPacketId();

	PacketHeader	getPacketHeader();
	List<Payload>	getPayloads();

	void 			deleteObservers();

	public static final int PACKET_ACKNOWLEDGEMENT_SIZE = 11;
}
