package irt.controller.serial_port;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;

import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;

public class ComPortJSerialComm implements SerialPortInterface {
	private final Logger logger = LogManager.getLogger();

	public long timeout = 100;

	private final SerialPort serialPort;
	private SerialPortListener serialPortListener;

	public ComPortJSerialComm(String portName) {
		serialPort = SerialPort.getCommPort(portName);
	}

	@Override
	public Packet send(PacketWork packetWork) {
//		logger.error("{}", packetWork);
		logger.traceEntry("{}", packetWork);

		Packet packet = packetWork.getPacketThread().getPacket();

		if(packet == null)
			return null;

		timeout = packet.getTimeout();
		return SerialPortWorker.send(this, packet);
	}

	@Override
	public boolean isOpened() {
		return serialPort.isOpen();
	}

	public void clear() {
		Optional.ofNullable(serialPortListener).ifPresent(SerialPortListener::clear);
	}

	@Override
	public String getPortName() {
		return serialPort.getSystemPortName();
	}

	@Override
	public boolean openPort() throws Exception {
		logger.traceEntry("{}", this);

		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		if(serialPort.isOpen())
			return true;

		if(!serialPort.openPort())
			return false;

		serialPort.setBaudRate(Baudrate.getDefaultBaudrate().getValue());

		serialPortListener = new SerialPortListener(this);
		serialPort.addDataListener(serialPortListener);
		return true;
	}

	@Override
	public boolean closePort() {
		logger.traceEntry();

		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		return serialPort.closePort();
	}

	@Override
	public void setBaudrate(Baudrate baudrate) {
		logger.traceEntry("baudrate: {}", baudrate);

		Baudrate.setDefaultBaudrate(baudrate);
		serialPort.setBaudRate(baudrate.getValue());
	}

	public int getBaudrate() {
		return serialPort.getBaudRate();
	}

	@Override
	public byte[] getFromBuffer(int size) throws Exception {
		logger.traceEntry("size: {};", size);

		if(size<=0)
			size = Integer.MAX_VALUE;

		final long start = System.currentTimeMillis();
		long elapsed = 0;

		byte[] buffer;
		synchronized (this) {
			while(((buffer=serialPortListener.getBuffer())==null || buffer.length<size) && (elapsed=(System.currentTimeMillis()-start))<timeout) {
				wait(timeout - elapsed);
			}
		}

		logger.debug("waitTime: {} milles; elapsed time = {} milles; buffer: {};", timeout, elapsed, buffer);

		if(buffer==null || buffer.length<size) {
			logger.warn("No answer or it is to short. buffer= {}", buffer);
			return null;
		}

		byte[] result = serialPortListener.getBytes(size);

		logger.traceExit("{}", result);
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + Optional.ofNullable(serialPort).map(SerialPort::getSystemPortName).orElse(null);
	}

	@Override
	public boolean writeBytes(byte[] data) {
		return serialPort.writeBytes(data, data.length) > 0;
	}

	@Override
	public int bytesAvailable() {
		return serialPort.bytesAvailable();
	}

	@Override
	public byte[] readBytes(int size) throws Exception {

		byte[] buffer = new byte[size];

		if(serialPort.readBytes(buffer, size)>0)
			return buffer;

		return null;
	}

	public static List<String> getPortNames(){
		return Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).collect(Collectors.toList());
	}
}