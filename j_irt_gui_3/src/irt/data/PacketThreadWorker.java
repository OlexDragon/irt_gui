package irt.data;

import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public interface PacketThreadWorker{

	LinkHeader getLinkHeader();
	void 	join() throws InterruptedException;
	void 	join(long i) throws InterruptedException;
	Packet 	getPacket();
	byte[] 	getData();
	boolean isReadyToSend();
	void 	start();
	Object 	getValue();
	void 	setData(byte[] d);
	void 	preparePacket();
	void 	preparePacket(byte packetParameterHeaderCode, Object value);
	void 	setDataPacketTypeCommand();
	void 	setValue(Object source);
	void 	setPriority(int priority);
	void 	preparePacket(byte value);

}
