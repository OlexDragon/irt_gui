package irt.gui.controllers.serial_port;

import com.fazecast.jSerialComm.SerialPort;

import irt.gui.controllers.components.ComboBoxSerialPort;
import irt.gui.controllers.enums.Baudrate;
import irt.gui.data.packet.interfaces.PacketToSend;

public class PacketSenderJSerialComm implements IrtSerialPort {

	private final SerialPort serialPort;
	private final PacketSender packetSender;

	public PacketSenderJSerialComm(String portName) {
		serialPort = SerialPort.getCommPort(portName);
		packetSender = new PacketSender(this);
	}

	@Override
	public String getPortName() {
		return serialPort.getSystemPortName();
	}

	@Override
	public void send(PacketToSend packet) throws Exception {
		packetSender.send(packet);
	}

	@Override
	public boolean isOpened() {
		return serialPort.isOpen();
	}

	@Override
	public boolean closeSerialPort() throws Exception {
		return serialPort.closePort();
	}

	@Override
	public boolean openSerialPort() throws Exception {
		return serialPort.openPort();
	}

	@Override
	public void setBaudrate(Baudrate baudrate) {
		ComboBoxSerialPort.SERIAL_PORT_PARAMS.setBaudrate(baudrate);
		serialPort.setBaudRate(baudrate.getValue());
	}

	@Override
	public int getParity() {
		return serialPort.getParity();
	}

	@Override
	public void setParity(int parity) {
		serialPort.setParity(parity);
	}

	@Override
	public void setParams() throws Exception {
		SerialPortParams params = ComboBoxSerialPort.SERIAL_PORT_PARAMS;
		serialPort.setComPortParameters(params.getBaudrate().getValue(), params.getDataBits(), params.getStopBits(), params.getParity());
	}

	@Override
	public int getAvailableBytes() {
		return serialPort.bytesAvailable();
	}

}
