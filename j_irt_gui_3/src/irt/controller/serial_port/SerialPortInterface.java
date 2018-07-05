package irt.controller.serial_port;

import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;

public interface SerialPortInterface {

	boolean isOpened();
	Packet send(PacketWork packetWork);
	String getPortName();
	boolean openPort() throws Exception;
	boolean closePort();
	void setBaudrate(int baudrate);
	int getBaudrate();
	byte[] readBytes(int size) throws Exception;
}
