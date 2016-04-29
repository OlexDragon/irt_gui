package irt.gui.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.enums.Baudrate;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.data.ToHex;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.interfaces.LinkedPacket;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class LinkedPacketSender extends SerialPort {

	public static final int STANDARD_WAIT_TIME 		= 5;
	public static final int FLASH_MEMORY_WAIT_TIME 	= 1;
	public static final int FCM_BY_BAIS_WAIT_TIME 	= 1;

	private final Logger logger = LogManager.getLogger();

	private static Baudrate baudrate = Baudrate.BAUDRATE_115200;

	volatile private boolean run = true;
	private int timeout = 1000;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private int parity = PARITY_NONE;  		public int getParity() { return parity; }  public void setParity(int parity) { this.parity = parity; }
	private int waitTime = STANDARD_WAIT_TIME;


	public LinkedPacketSender(String portName) {
		super(portName);
	}

	public void send(LinkedPacket packet){
//		logger.error(packet);

		if(!run) return;

		Timer timer = setTimer();

		try {

			if(!isOpened())
				openPort();

			packet.clearAnswer();
			if(writePacket(packet)){

				if(packet.getObservers().length>0){

					if(packet instanceof WaitTime)
						waitTime = ((WaitTime)packet).getWaitTime();

					byte[] readBytes = readBytes(waitTime);

					//Send back acknowledgement
					byte[] acknowledgement = packet.getAcknowledgement();
					if(acknowledgement!=null)
						writeBytes(acknowledgement);

					packet.setAnswer(readBytes);
				}
			}else
				logger.warn("packet.toBytes() return null. {}", packet);

		} catch (Exception e) {
			logger.catching(e);
		}

		timer.stop();
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

	private boolean writePacket(LinkedPacket packet) throws SerialPortException {

		byte[] buffer = packet.toBytes();

		final Optional<byte[]> d = Optional
		.ofNullable(buffer)
		.filter(b->b.length>0);

		if(d.isPresent()){
			clear();
			return writeBytes(buffer);
		}

		return true;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override protected void finalize(){
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

	@Override public synchronized boolean openPort() throws SerialPortException {

		boolean isOpened;

		isOpened = isOpened();

		if (!isOpened)
			if (run) {
				logger.debug("openPort() Port Name={}", this);
				isOpened = super.openPort();
				if (isOpened) {
					setParams();
					addEventListener(serialPortEvent);
				}
			} else
				throw new SerialPortException(getPortName(), "openPort()",
						"Property LinkedPacketSender.run set to " + run);
		return isOpened;
	}

	@Override public synchronized boolean closePort() throws SerialPortException{

		boolean isClosed = !isOpened();

		if (!isClosed) {

			removeEventListener();
			boolean isPurged = purgePort(PURGE_RXCLEAR | PURGE_TXCLEAR | PURGE_RXABORT | PURGE_TXABORT);
			isClosed = super.closePort();
			logger.debug("closePort()is Closed={}, is purged={}", isClosed, isPurged);

		}

		return isClosed;
	}

	public static Baudrate getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(Baudrate baudrate){
		LinkedPacketSender.baudrate = baudrate;
		try {
			setParams();
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void setParams() throws SerialPortException {
		setParams(baudrate.getBaudrate(), DATABITS_8, STOPBITS_1, parity);
	}

	@Override public String toString() {
		return getPortName();
	}

	//*** Class SerialPortEvent *****************************************************
	private class SerialPortEvent implements SerialPortEventListener{

		@Override
		public void serialEvent(jssc.SerialPortEvent serialPortEvent) {

			synchronized (LinkedPacketSender.this) {
				LinkedPacketSender.this.notify();
			}
		}
	}
}
