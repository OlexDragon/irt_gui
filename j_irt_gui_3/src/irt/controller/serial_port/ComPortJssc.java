package irt.controller.serial_port;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.LoggerWorker;
import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.head.IrtPanel;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ComPortJssc extends SerialPort implements SerialPortInterface {
	private final Logger logger = LogManager.getLogger();

	private static final String LOGGER_NAME = "comPort";


	private SerialPortListener serialPortListener;

	private long timeout;

	public ComPortJssc(String portName) {
		super(portName);

		LoggerWorker.setLoggerLevel(LOGGER_NAME, Level.toLevel(IrtPanel.PROPERTIES.getProperty("dump_serialport"), Level.ERROR));
	}

	public Packet send(PacketWork packetWork){

		if(!isOpened())
			try {
				openPort();
			} catch (Exception e) {
				logger.catching(e);
				return packetWork.getPacketThread().getPacket();
			}

		Packet packet = packetWork.getPacketThread().getPacket();

		if(packet == null)
			return null;

		timeout = packet.getTimeout();

//		timeout = packet.getTimeout();
		return SerialPortWorker.send(this, packet);
	}

	public byte[] readByte(int byteCount) throws Exception {
		return readBytes(byteCount, 1000);
	}

	@Override
	public synchronized boolean openPort() throws SerialPortException {
		logger.traceEntry("{}", this);

		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		if (isOpened()) return true;
		
		if(!super.openPort()) return false;

		serialPortListener = new SerialPortListener(this);
		addEventListener(serialPortListener);

		setBaudrate();

		logger.debug("Serial port {} is OPEND", this);
		return true;
	}

	@Override
	public synchronized boolean closePort(){
		logger.traceEntry("{}", this);

		//Show stack trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		if (!isOpened()) return true;

		try { removeEventListener(); } catch (SerialPortException e) { logger.catching(Level.DEBUG, e); }
		try { return super.closePort(); } catch (Exception e) { logger.catching(e); }

		return !isOpened();
	}

	public void setBaudrate(Baudrate baudrate){

		Baudrate.setDefaultBaudrate(baudrate);

		try {
			setBaudrate();
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void setBaudrate() throws SerialPortException {
		setParams((int) Baudrate.getDefaultBaudrate().getValue(), DATABITS_8, STOPBITS_1, PARITY_NONE);
	}

	@Override
	public int bytesAvailable() throws SerialPortException {
		return getInputBufferBytesCount();
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
		return getClass().getSimpleName() + "  - "+ getPortName();
	}

	@Override
	public void clear() throws Exception {
		serialPortListener.clear();
	}

	public static List<String> getPortNames(){
		return Arrays.asList(SerialPortList.getPortNames());
	}
}
