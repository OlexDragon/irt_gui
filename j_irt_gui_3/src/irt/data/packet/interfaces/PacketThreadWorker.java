package irt.data.packet.interfaces;

import irt.data.packet.LinkHeader;
import irt.data.packet.PacketSuper.Priority;

public interface PacketThreadWorker extends ValuePacket{

	LinkHeader getLinkHeader();
	void 	join() throws InterruptedException;
	void 	join(long i) throws InterruptedException;
	Packet 	getPacket();
	byte[] 	getData();
	boolean isReadyToSend();
	void 	start();
	void 	setData(byte[] d);
	void 	preparePacket();
	void 	preparePacket(byte packetParameterHeaderCode, Object value);
	void 	setDataPacketTypeCommand();
	void 	setValue(Object source);
	void 	setPriority(Priority priority);		//function of java.lang.Thread
	void 	preparePacket(byte value);

}
