package irt.controller.serial_port;

import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;

public interface SerialPortInterface {

	boolean isOpened();
	Packet send(PacketWork packetWork);
	String getPortName();
	boolean openPort() throws Exception;
	boolean closePort();
	byte[] getFromBuffer(int size) throws Exception;
	byte[] readBytes(int size) throws Exception;
	void setBaudrate(Baudrate baudrate);
	void clear() throws Exception;
	boolean writeBytes(byte[] data) throws Exception;
	int bytesAvailable() throws Exception;
}
