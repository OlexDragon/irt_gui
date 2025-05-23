package irt.serial.port.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.packet.Packet;
import irt.packet.interfaces.PacketToSend;
import irt.packet.interfaces.WaitTime;
import irt.serial.port.enums.Baudrate;
import irt.services.ToHex;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class PacketSender extends SerialPort {

	public static final int STANDARD_WAIT_TIME 		= 5;
	public static final int FLASH_MEMORY_WAIT_TIME 	= 1;
	public static final int FCM_BY_BAIS_WAIT_TIME 	= 1;
	public static final int TOOLS_WAIT_TIME = 1000;

	private final Logger logger = LogManager.getLogger();

	private static Baudrate baudrate = Baudrate.BAUDRATE_115200;

	volatile private boolean run = true;
	private int timeout = 1000;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private int parity = PARITY_NONE;  		public int getParity() { return parity; }  public void setParity(int parity) { this.parity = parity; }
	private int waitTime = STANDARD_WAIT_TIME;


	public PacketSender(String portName) {
		super(portName);
	}

	public void send(PacketToSend packet){
		logger.traceEntry(()->packet.toString());

		if(!run) return;

		Timer timer = setTimer();

		try {

			if(!isOpened())
				openPort();

			packet.clear();
			if(writePacket(packet)){

				if(packet.getObservers().length>0){

					if(packet instanceof WaitTime)
						waitTime = ((WaitTime)packet).getWaitTime();

					logger.trace("waitTime = {}", waitTime);

					byte[] readBytes = readBytes(waitTime, packet.getEndSequence());

					//Send back acknowledgement
					byte[] acknowledgement = packet.getAcknowledgement();
					if(acknowledgement!=null)
						writeBytes(acknowledgement);

					packet.setAnswer(readBytes);
				}
			}else{
				packet.setAnswer(null);
				logger.warn("packet.toBytes() return null. {}", ()->packet.toString());
			}

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

	private boolean writePacket(PacketToSend packet) throws SerialPortException {

		byte[] buffer = packet.toBytes();

		final Optional<byte[]> d = Optional
		.ofNullable(buffer)
		.filter(b->b.length>0);

		if(d.isPresent()){
			clear();
			logger.trace("{}", ()->ToHex.bytesToHex(buffer));
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
		logger.traceEntry();

		byte[] readBytes = null;
		if (wait(1, 20)){
			do {
				readBytes = readBytes(getInputBufferBytesCount());
				final byte[] rb = readBytes;
				logger.warn("\n\t?? Clear: {}", ()->ToHex.bytesToHex(rb));
			} while (wait(1, 200));
		}
		return readBytes;
	}

	public byte[] readBytes( int waitTime, byte[] endBytes) throws SerialPortException{
		logger.entry(waitTime);
		if(waitTime<=0) waitTime = 1;

		byte[] readBytes = null;
		boolean hasEnd = false;
		byte[] tmp = null;
		int count = 20;

		do{
			if (wait(1, waitTime)) {

				tmp = readBytes(getInputBufferBytesCount());

				if (tmp != null) {
					if (readBytes == null)
						readBytes = tmp;
					else {
						int l = readBytes.length;
						int tmpLength = tmp.length;
						readBytes = Arrays.copyOf(readBytes, l + tmpLength);
						System.arraycopy(tmp, 0, readBytes, l, tmpLength);
					}

					hasEnd = hasFlagSequences(readBytes, endBytes);
//					logger.error("\n\t {}\n\t count: {}", length, count);
				}

//				logger.debug("\n\t tmp: {}\n\t readBytes: {}\n\t isEnd: {}\n\t {}", tmp, readBytes, isEnd, count);
			} else
				--count;

		}while(!hasEnd && count>0);

		final byte[] rb = readBytes;
		logger.trace("\n\tEXIT: {}", ()->ToHex.bytesToHex(rb));

		return readBytes;
	}

	public boolean hasFlagSequences(byte[] readBytes, byte[] endBytes) {
		logger.trace("{} -:- {}", readBytes, endBytes);

		if(endBytes.length==1 && endBytes[0]==Packet.FLAG_SEQUENCE)	//	It should be 4 Packet.FLAG_SEQUENCE
			return IntStream
					.range(0, readBytes.length)
					.map(i -> readBytes[i])
					.filter(x->x==Packet.FLAG_SEQUENCE)
					.count() >= 4;

		else{

			int from = 0;
			int to = readBytes.length - endBytes.length + 1;

			return logger.traceExit(

					IntStream
					.range( from, to)
					.map(i -> to - i + from - 1)
					.mapToObj(i->Arrays.copyOfRange(readBytes, i, i+endBytes.length))
					.filter(bs->Arrays.equals(bs, endBytes))
					.findAny()
					.isPresent());
		}
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

		return logger.traceExit(isReady);
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
						"Property PacketSender.run set to " + run);
		return isOpened;
	}

	@Override public synchronized boolean closePort() throws SerialPortException{

		return isOpened() ? super.closePort() : true;
	}

	public static Baudrate getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(Baudrate baudrate){
		PacketSender.baudrate = baudrate;
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

			synchronized (PacketSender.this) {
				PacketSender.this.notify();
			}
		}
	}
}
