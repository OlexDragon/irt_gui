package irt.gui.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.stream.IntStream;

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
			byte[] readBytes = readBytes(5);

			//Send back acknowledgement
			byte[] acknowledgement = packet.getAcknowledgement();
			if(acknowledgement!=null)
				writeBytes(acknowledgement);

			packet.setAnswer(readBytes);
			timer.stop();

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
		if(waitTime<=0) waitTime = 1;

		byte[] readBytes = null;
		boolean isEnd = false;
		byte[] tmp = null;
		int count = 20;

		do{
			if (wait(1, waitTime)) {

				tmp = super.readBytes(getInputBufferBytesCount());

				if (tmp != null) {
					if (readBytes == null)
						readBytes = tmp;
					else {
						int l = readBytes.length;
						int tmpLength = tmp.length;
						readBytes = Arrays.copyOf(readBytes, l + tmpLength);
						System.arraycopy(tmp, 0, readBytes, l, tmpLength);
					}

					isEnd = getFlagSequences(readBytes);//It should be 4 Packet.FLAG_SEQUENCE
//					logger.error("\n\t {}\n\t count: {}", length, count);
				}

//				logger.debug("\n\t tmp: {}\n\t readBytes: {}\n\t isEnd: {}\n\t {}", tmp, readBytes, isEnd, count);
			} else
				--count;

		}while(!isEnd && count>0);

		logger.trace("\n\tEXIT: {}", readBytes);
		return readBytes;
	}

	private boolean getFlagSequences(byte[] readBytes) {
		return IntStream.range(0, readBytes.length).map(i -> readBytes[i]).filter(x->x==(Packet.FLAG_SEQUENCE)).count() >= 4;
	}

	public synchronized void setRun(boolean run, String why) {
		logger.warn("setRun({}, {})", run, why);
		this.run = run;
		notify();
	}

	public synchronized boolean isRun() {
		return run;
	}

	public synchronized boolean wait(int eventValue, int waitTime) throws SerialPortException {
		logger.entry(eventValue, waitTime);
		boolean isReady = false;
		long start = System.currentTimeMillis();
		long waitTimeL = waitTime*eventValue;

		while(isOpened() && !(isReady = getInputBufferBytesCount()>=eventValue) && (System.currentTimeMillis()-start)<waitTimeL && isRun()){

				try {
					wait(waitTimeL);
				} catch (Exception e) {
					logger.catching(e);
				}
		};

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
