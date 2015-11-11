package irt.gui.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class LinkedPacketSender extends SerialPort {

	private final Logger logger = LogManager.getLogger();

	public enum Baudrate{
		BAUDRATE_9600	(SerialPort.BAUDRATE_9600),
		BAUDRATE_19200	(SerialPort.BAUDRATE_19200),
		BAUDRATE_38400	(SerialPort.BAUDRATE_38400),
		BAUDRATE_57600	(SerialPort.BAUDRATE_57600),
		BAUDRATE_115200	(SerialPort.BAUDRATE_115200);

		private int baudrate;

		private Baudrate(int baudrate){
			this.baudrate = baudrate;
		}

		public int getBaudrate() {
			return baudrate;
		}

		@Override
		public String toString(){
			return ""+baudrate;
		}

		public static Baudrate valueOf(int baudrate) {
			Baudrate result = null;

			for(Baudrate b:values())
				if(b.getBaudrate()==baudrate){
					result = b;
					break;
				}

			return result;
		}
	}

	private static int baudrate = BAUDRATE_115200;

	volatile private boolean run = true;
	private int timeout = 3000;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private boolean isSerialPortEven;

	public LinkedPacketSender(String portName) {
		super(portName);
	}

	public void send(LinkedPacket packet){
		logger.entry(packet);

//	for Debugging		
//		if(packet.getPacketHeader().getPacketIdDetails().getPacketId()==PacketId.ALARMS)
//			logger.error(packet);

		if(!run) return;

		try {

			if(!isOpened())
				openPort();

			Timer timer = setTimer();

			writePacket(packet);
			byte[] readBytes = readBytes(20);
			packet.setAnswer(readBytes);
			timer.stop();

			//Send back acknowledgement
			byte[] acknowledgement = packet.getAcknowledgement();
			if(acknowledgement!=null)
				writeBytes(acknowledgement);


		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private Timer setTimer() {
		Timer timer = new Timer(timeout, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					closePort();
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		});
		timer.setRepeats(false);
		return timer;
	}

	private void writePacket(LinkedPacket packet) throws SerialPortException {

		clear();
		byte[] buffer = packet.toBytes();
		writeBytes(buffer);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected void finalize(){
			try {
				closePort();
			} catch (Exception e) {
				logger.catching(e);
			}
	}

	public byte[] clear() throws SerialPortException {
		byte[] readBytes = null;
		if (wait(1, 20)){
			do {
				readBytes = super.readBytes(getInputBufferBytesCount());
				String readBytesStr = ToHex.bytesToHex(readBytes);
				logger.warn("\n\t?? Clear: {}", readBytesStr);
			} while (wait(1, 200));
		}
		return readBytes;
	}

	@Override
	public byte[] readBytes( int waitTime) throws SerialPortException{
		logger.entry(waitTime);

		byte[] readBytes = null;
		boolean isEnd = false;
		byte[] tmp = null;

		
		if(waitTime<=0)
			waitTime = 1;

		do{
			try {
				synchronized (this) {
					wait(waitTime);
				}
			} catch (Exception e) {
				logger.catching(e);
			}

			if (getInputBufferBytesCount() > 0) {

				tmp = super.readBytes(getInputBufferBytesCount());
				logger.trace("\n\ttmp: {}", Arrays.toString(tmp));

				if (tmp != null) {
					if (readBytes == null)
						readBytes = tmp;
					else {
						int l = readBytes.length;
						int tmpLength = tmp.length;
						readBytes = Arrays.copyOf(readBytes, l + tmpLength);
						System.arraycopy(tmp, 0, readBytes, l, tmpLength);
					}

					List<byte[]> asList = Arrays.asList(readBytes);
					int lastIndex = asList.lastIndexOf(Packet.FLAG_SEQUENCE);
					isEnd =  lastIndex > 22;//ConfirmBytes = 11, link header = 4, packet header = 7 
				}

			} else
				tmp = null;

		}while(!isEnd && tmp!=null);
		logger.trace("\n\tEXIT: {}", readBytes);
		return readBytes;
	}

	public synchronized void setRun(boolean run, String why) {
		logger.warn("setRun({}, {})", run, why);
		this.run = run;
		notify();
	}

	public synchronized boolean isRun() {
		return run;
	}

	public boolean wait(int eventValue, int waitTime) throws SerialPortException {
		logger.entry(eventValue, waitTime);
		boolean isReady = false;
		long start = System.currentTimeMillis();
		long waitTimeL = waitTime*eventValue;

		while(isOpened() && !(isReady = getInputBufferBytesCount()>=eventValue) && (System.currentTimeMillis()-start)<waitTimeL && isRun()){
			synchronized (this) {

				try {
					wait(waitTimeL);
				} catch (Exception e) {
					logger.catching(e);
				}

				logger.trace("isSerialPortEven={}", isSerialPortEven);
				isSerialPortEven = false;
			}
		};

		isSerialPortEven = false;

		return logger.exit(isReady);
	}

	@Override
	public boolean openPort() throws SerialPortException {
		
		boolean isOpened;

		synchronized (logger) {
			isOpened = isOpened();

			if(!isOpened)
				if (run) {
					logger.debug("openPort() Port Name={}", this);
					isOpened = super.openPort();
					if (isOpened){
						addEventListener(serialPortEvent);
						setBaudrate();
					}
				}else
					throw new SerialPortException(getPortName(), "openPort()", "Property LinkedPacketSender.run set to " + run);
		}
		return isOpened;
	}

	@Override
	public boolean closePort() throws SerialPortException{

		boolean isClosed = !isOpened();
		logger.debug("1) Port Name={} closePort()is Closed={}", this, isClosed);

		synchronized (logger) {
			if (!isClosed) {

					removeEventListener();
					boolean isPurged = purgePort(PURGE_RXCLEAR | PURGE_TXCLEAR | PURGE_RXABORT | PURGE_TXABORT);
					isClosed = super.closePort();
					logger.debug("2) closePort()is Closed={}, is purged={}",isClosed, isPurged);

			}
		}

		return isClosed;
	}

	@Override
	public String toString() {
		return getPortName();
	}

	//*** Class SerialPortEvent *****************************************************
	private class SerialPortEvent implements SerialPortEventListener{

		@Override
		public void serialEvent(jssc.SerialPortEvent serialPortEvent) {

			synchronized (LinkedPacketSender.this) {
				isSerialPortEven = true;
				LinkedPacketSender.this.notify();
//				Console.appendLn("", "notify");
			}
		}
		
	}

	public static int getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(int baudrate){
		LinkedPacketSender.baudrate = baudrate;
		try {
			setBaudrate();
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void setBaudrate() throws SerialPortException {
		setParams(baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);
	}
}
