package irt.gui.web.services;

import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

import irt.gui.web.exceptions.IrtSerialPortIOException;

public interface IrtSerialPort {

	List<String> getSerialPortNames	();
	SerialPort open(String port, Integer baudrate) throws IrtSerialPortIOException;
	byte[] send(String serialPort, Integer timeout, byte[] bytes, Integer baudrate) throws IrtSerialPortIOException;
	byte[] read(String portName, Integer timeout, Integer baudrate) throws IrtSerialPortIOException;
	void shutdown();
	boolean close(String spName);
}
