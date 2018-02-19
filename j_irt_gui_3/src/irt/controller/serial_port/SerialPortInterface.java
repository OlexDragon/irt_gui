package irt.controller.serial_port;

import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;

public interface SerialPortInterface {

	boolean isOpened();
	Packet send(PacketWork packetWork);
	void setRun(boolean b, String string);
	String getPortName();
	boolean openPort() throws Exception;
	boolean closePort();
	void setBaudrate(int baudrate);
	int getBaudrate();
}
