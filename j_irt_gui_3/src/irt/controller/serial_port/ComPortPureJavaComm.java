
package irt.controller.serial_port;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PureJavaSerialPort;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class ComPortPureJavaComm implements SerialPortInterface {
	private final static Logger logger = LogManager.getLogger();

	public static final int MAX_WAIT_TIME = 1500;

    public static final int BAUDRATE_115200 = 115200;

	private final CommPortIdentifier PORT_IDENTIFIER;

	private boolean opened ;
	private PureJavaSerialPort serialPort;

	private InputStream inputStream;
	private OutputStream outputStream;

	private SerialPortListener serialPortListener;

	private long timeout;

	public ComPortPureJavaComm(String portName) throws NoSuchPortException {
		PORT_IDENTIFIER = CommPortIdentifier.getPortIdentifier(portName);
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public synchronized Packet send(PacketWork packetWork){

		Packet packet = packetWork.getPacketThread().getPacket();

		if(packet == null)
			return null;

		timeout = packet.getTimeout();
		return SerialPortWorker.send(this, packet);
	}

	public void clear(){
		serialPortListener.clear();
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
	public String getPortName() {
		return PORT_IDENTIFIER.getName();
	}

	@Override
	public synchronized boolean openPort() throws Exception {
		logger.traceEntry("{}", this);

		if(opened)
			return true;

		serialPort = (PureJavaSerialPort) PORT_IDENTIFIER.open(ComPortPureJavaComm.class.getName(), ComPortPureJavaComm.MAX_WAIT_TIME);
		setBaudrate(Baudrate.getDefaultBaudrate().getValue());
		serialPort.enableReceiveThreshold(ComPortPureJavaComm.MAX_WAIT_TIME);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();

		serialPort.notifyOnDataAvailable(true);
		serialPortListener = new SerialPortListener(this);
		serialPort.addEventListener(serialPortListener);

		return opened = true;
	}

	@Override
	public synchronized boolean closePort() {

		if(!opened)
			return true;

		opened = false;

		try {
			if(inputStream!=null)
				inputStream.close();
		} catch (IOException e) {
			logger.catching(e);
		}

		try {
			if(outputStream!=null)
				outputStream.close();
		} catch (IOException e) {
			logger.catching(e);
		}

		if(serialPort!=null){
			serialPort.removeEventListener();
			serialPort.close();
		}

		serialPortListener.clear();

		return true;
	}

	public void setBaudrate(Baudrate baudrate) {

		Baudrate.setDefaultBaudrate(baudrate);
		setBaudrate(baudrate.getValue());
	}

	private void setBaudrate(final int value) {
		try {
			serialPort.setSerialPortParams(value, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			logger.catching(e);
		}
	}

	public static List<String> getPortNames(){

		List<String> portsList = new ArrayList<>();

		final Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

		while(portIdentifiers.hasMoreElements())
			portsList.add(portIdentifiers.nextElement().getName());

		Collections.sort(portsList, (a, b)->Integer.parseInt(a.replaceAll("\\D", "")) - Integer.parseInt(b.replaceAll("\\D", "")));

		return portsList;
	}

	public static byte[] byteStuffing(byte[] readBytes) {

		int index = 0;
		if(readBytes!=null){

			for(int i=0; i<readBytes.length; i++)

				if(readBytes[i]==PacketImp.CONTROL_ESCAPE){
					if(++i<readBytes.length)
						readBytes[index++] = (byte) (readBytes[i]^0x20);
				}else
					readBytes[index++] = readBytes[i];
		}

		return readBytes==null ? null : index==readBytes.length ? readBytes : Arrays.copyOf(readBytes, index);
	}

	public int getBaudrate() {
		return serialPort.getBaudRate();
	}

	@Override
	public boolean writeBytes(byte[] data) throws Exception {
		outputStream.write(data);
		return true;
	}

	@Override
	public int bytesAvailable() throws Exception {
		return inputStream.available();
	}

	@Override
	public byte[] readBytes(int size) throws Exception {

		byte[] bytes = new byte[size];

		if(inputStream.read(bytes)>0)
			return bytes;

		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + Optional.ofNullable(PORT_IDENTIFIER).map(CommPortIdentifier::getName).orElse(null);
	}
}
