package irt.gui.controllers;

import irt.gui.controllers.enums.Baudrate;
import irt.gui.data.packet.interfaces.PacketToSend;

public interface IrtSerialPort {

	String getPortName();
	void send(PacketToSend packet) throws Exception;
	boolean isOpened();
	boolean closeSerialPort() throws Exception;
	boolean openSerialPort() throws Exception;
	void setBaudrate(Baudrate valueOf);
	int getParity();
	void setParity(int parityEven);
	void setParams() throws Exception;
	int getAvailableBytes() throws Exception;
}
