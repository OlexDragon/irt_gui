package irt.gui.web.services;

import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

public interface IrtSerialPort {

	List<String> getSerialPortNames	();
	SerialPort open(String port, Integer baudrate);
	byte[] send(String serialPort, Integer timeout, byte[] bytes);
	byte[] read(String portName, Integer timeout);
	void shutdown();
}
