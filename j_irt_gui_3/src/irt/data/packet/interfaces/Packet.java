package irt.data.packet.interfaces;

import java.util.List;

import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

public interface Packet {

	PacketHeader 	getHeader();
	Payload 		getPayload(byte parameterId);
	Payload 		getPayload(int index);
	List<Payload> 	getPayloads();
	byte[] 			toBytes();

	void setHeader	(PacketHeader packetHeader);
	void setPayloads(List<Payload> payloadsList);
	void set		(byte[] data);
}
