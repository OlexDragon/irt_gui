
package irt.gui.data.packet.interfaces;

import java.util.List;
import java.util.Observer;

import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;

public interface LinkedPacket extends Comparable<LinkedPacket>{

	PacketId 		getPacketId();

	LinkHeader		getLinkHeader();
	void 			setLinkHeaderAddr(byte addr);
	PacketHeader	getPacketHeader();
	List<Payload>	getPayloads();
	byte[]			toBytes();

	byte[] 			getAnswer();
	void 			setAnswer(byte[] data);
	void			clearAnswer();

	byte[] 			getAcknowledgement();

	void 			addObserver	(Observer observer);
	void 			deleteObserver	(Observer observer);
	void 			deleteObservers();
	Observer[] 		getObservers	() throws Exception;

	public static final int PACKET_ACKNOWLEDGEMENT_SIZE = 11;
}
