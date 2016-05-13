
package irt.gui.data.packet.interfaces;

import java.util.List;

import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;

public interface LinkedPacket extends PacketToSend{

	PacketId 		getPacketId();

	void 			setLinkHeaderAddr(byte addr);
	PacketHeader	getPacketHeader();
	List<Payload>	getPayloads();

	void 			deleteObservers();

	public static final int PACKET_ACKNOWLEDGEMENT_SIZE = 11;
}
