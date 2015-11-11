package irt.data.packet;

import java.util.List;

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
