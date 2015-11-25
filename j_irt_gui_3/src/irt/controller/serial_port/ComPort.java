package irt.controller.serial_port;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;

import irt.data.Checksum;
import irt.data.LoggerWorker;
import irt.data.PacketThread;
import irt.data.PacketThreadWorker;
import irt.data.PacketWork;
import irt.data.ToHex;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.IrtPanel;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class ComPort extends SerialPort {

	private static final String LOGGER_NAME = "comPort";

	private final Logger logger = (Logger) LogManager.getLogger(LOGGER_NAME);
	private final Marker marker = MarkerManager.getMarker("FileWork");

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

	private boolean run = true;
	private int timeout = 3000;
	private int timesTimeout;
	private Timer timer;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private LinkHeader linkHeader;
	private short packetId;
	private boolean isSerialPortEven;
	private boolean isComfirm;

	private boolean logged = false;

	public ComPort(String portName) {
		super(portName);

		LoggerWorker.setLoggerLevel(LOGGER_NAME, Level.toLevel(IrtPanel.PROPERTIES.getProperty("dump_serialport"), Level.ERROR));

		timer = new Timer(timeout, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				Console.appendLn("Timeout", "Timer");
				synchronized (ComPort.this) {
					closePort();
				}
			}
		});
		timer.setRepeats(false);
	}

	public Packet send(PacketWork packetWork){
		logger.trace(packetWork);

		long start = System.currentTimeMillis();
		PacketThreadWorker pt = packetWork.getPacketThread();
		try {
			pt.join(500);
		} catch (Exception e1) {
			logger.catching(e1);
		}
		Packet p = pt.getPacket();
		PacketHeader ph = p.getHeader();
		byte groupId = ph.getGroupId();
		packetId = ph.getPacketId();
		Packet packet;

		if(p instanceof LinkedPacket){
			linkHeader = ((LinkedPacket)p).getLinkHeader();
			packet = new LinkedPacketImp(linkHeader);
		}else{
			linkHeader = null;
			packet = new PacketImp();
		}

		int runTimes = 0;
		byte[] readData;
		int ev;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {

if(!isRun())
	setRun(true, "Restart");

do{

	Checksum checksum = null;
	logger.trace("\n\t {}\n\t", ph);

			timer.restart();
			clear();

			byte[] data = pt.getData();
			String hexStr = ToHex.bytesToHex(data);

			String prefix = (runTimes+1)+") send";

			logger.info(marker, ">> {}: {}", prefix, p);
			logger.info(marker, ">> {}: {}", prefix, hexStr);
			Console.appendLn(p, prefix);
			Console.appendLn(hexStr, prefix);

			if(isRun() && data!=null){
				writeBytes(data);

				if ((isConfirmBytes()) && isFlagSequence()){

					if(linkHeader!=null)
						if((readData=readLinkHeader())!=null)
							checksum = new Checksum(readData);
						else{
							Console.appendLn("LinkHeader", "Break");
							break;
						}

					if((readData=readHeader())!=null) {
						if(checksum!=null)
							checksum.add(readData);
						else
							checksum = new Checksum(readData);

						packetHeader = new PacketHeader(readData);

						if (packetHeader.toBytes() != null && packetHeader.getGroupId()==groupId) {
							packet.setHeader(packetHeader);
							payloadsList = new ArrayList<>();

							while ((readData = readParameterHeader())!=null && isRun()) {

								if (containsFlagSequence(readData)) {
									cs = checksum.getChecksumAsBytes();

									if (cs[0] == readData[0] && cs[1] == readData[1]){
										timesTimeout = 0;
										packet.setPayloads(payloadsList);
									}
									logger.trace("END");
									break;
								}
								checksum.add(readData);
								parameterHeader = new ParameterHeader(readData);

								ev = parameterHeader.getSize();
								logger.trace("parameterHeader.getSize()={}", ev);
								if(parameterHeader.getCode()>300 || ev>2000){
									Console.appendLn("ParameterHeader Sizes", "Break ");
									logger.error("parameterHeader.getCode()>300({}) || ev>2000({})", parameterHeader.getCode(), ev);
									break;
								}
								Console.appendLn("", "Payload ");
								if (ev >= 0 && (readData = readBytes(ev))!=null) {

									checksum.add(readData);
									Payload payload = new Payload(parameterHeader,	readData);
									payloadsList.add(payload);
								}else{
									Console.appendLn("Payload", "Break ");
									logger.warn("ev < 0 || (readData = readBytes(ev))==null");
									break;
								}
							}
						}else
							logger.warn("packetHeader.asBytes() == null || packetHeader.getGroupId()!=groupId");
					}else
						logger.warn("(readData=readHeader())==null");
				}else if(!logged){
					logger.warn("isFlagSequence() = false");
				}
			}else
				setRun(false, "run="+run+", data="+data);
}while(isComfirm && packet.getPayloads()==null && ++runTimes<3 && isRun());//if error repeat up to 3 times

				if(isRun()) {
					byte[] acknowledge = getAcknowledge();
					writeBytes(acknowledge);
					logger.info(marker, "acknowledge={}", ToHex.bytesToHex(acknowledge));
				}

				if(packet.getHeader()==null || packet.getPayloads()==null && isRun())
					packet = p;

		} catch (Exception e) {
			logger.catching(e);
			if(timesTimeout<3){
				timesTimeout++;
				setRun(false, "Times Timeout");
			}
			Console.appendLn(e.getLocalizedMessage(), "Error");
		}

		timer.stop();

		logger.trace(marker, "<< Get: {}", packet);
		Console.appendLn(packet, "Get");
		Console.appendLn(""+(System.currentTimeMillis()-start), "Time");

		return logger.exit(packet);
	}

	private byte[] getAcknowledge() {
		logger.entry();
		byte[] b;

		if(linkHeader!=null)
			b = Arrays.copyOf(linkHeader.toBytes(), 7);
		else
			b = new byte[3];

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;

		byte[] packetId = PacketImp.toBytes(this.packetId);
		System.arraycopy(packetId, 0, b, ++idPosition, 2);

		return logger.exit(PacketThread.preparePacket(b));
	}

	private byte[] readLinkHeader() throws SerialPortException {
		return readBytes(LinkHeader.SIZE);
	}

	private byte[] readHeader() throws SerialPortException {
		return readBytes(PacketHeader.SIZE);
	}

	private byte[] readParameterHeader() throws SerialPortException {
		return readBytes(ParameterHeader.SIZE);
	}

	public boolean isFlagSequence() throws SerialPortException {

		byte[] readBytes = readByte(2500);
		boolean isFlagSequence = readBytes!=null && readBytes[0] == PacketImp.FLAG_SEQUENCE;

		return logger.exit(isFlagSequence);
	}

	private boolean containsFlagSequence(byte[] readBytes) {
		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==PacketImp.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		return isFlagSequence;
	}

	private boolean isConfirmBytes() throws SerialPortException {

		boolean isComfirm = false;
		int ev = linkHeader!=null ? 11 : 7;
		int index = ev - 3;
		logger.debug("linkHeader = {}", linkHeader);

		byte[] readBytes = readBytes(ev,100);
		this.isComfirm = readBytes!=null && readBytes[0]==PacketImp.FLAG_SEQUENCE && readBytes[readBytes.length-1]==PacketImp.FLAG_SEQUENCE;

		//for converters
		if(!this.isComfirm && readBytes!=null && linkHeader!=null && readBytes[6]==PacketImp.FLAG_SEQUENCE)
					linkHeader = null;

		if(this.isComfirm){
			byte[] data = Arrays.copyOfRange(readBytes, 1, index);
			LinkHeader lh = new LinkHeader(data);
			if((linkHeader==null || lh.equals(linkHeader)) && packetId==getPacketId(linkHeader!=null, data)){
				Checksum cs = new Checksum(data);
				byte[] b = cs.getChecksumAsBytes();
				if(b[0]==readBytes[index] && b[1]==readBytes[++index])
					isComfirm = true;
				else
					logger.warn("Checksum ERROR ({})", ToHex.bytesToHex(readBytes));
			}else
				logger.warn("LinckHeaders are not equal (sent={}, received={}", linkHeader, lh);
		}else if(!logged){
			logger.warn("Acknowledge is wrong({})", ToHex.bytesToHex(readBytes));
		}

		logged = readBytes==null || !isComfirm;

		return logger.exit(isComfirm);
	}

	private short getPacketId(boolean isLinked, byte[] data) {
		return (short) PacketImp.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
	}

	@Override
	public String toString() {
		return "ComPort Thread Name - "+Thread.currentThread().getName();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected void finalize(){
			closePort();
	}

	public byte[] clear() throws SerialPortException {
		byte[] readBytes = null;
		if (wait(1, 20)){
			if(isRun()) {
				byte[] acknowledge = getAcknowledge();
				writeBytes(acknowledge);
				logger.info(marker, "acknowledge={}", ToHex.bytesToHex(acknowledge));
			}
			do {
				readBytes = super.readBytes(getInputBufferBytesCount());
				String readBytesStr = ToHex.bytesToHex(readBytes);
				Console.appendLn(readBytesStr, "Clear");
				logger.warn(marker, "?? Clear: {}", readBytesStr);
			} while (wait(1, 200));
		}
		return readBytes;
	}

	public byte[] readByte(int timeout) throws SerialPortException {
		return readBytes(1, timeout);
	}

	@Override
	public byte[] readBytes(int byteCount) throws SerialPortException {

		return readBytes(byteCount, 50);
	}

	public byte[] readBytes(int byteCount, int waitTime) throws SerialPortException {

		byte[] readBytes = null;

		int escCount = 0;
		boolean hasEsc = false;

		do{
			byte[] tmpBytes = null;

			synchronized (this) {
				if(wait(hasEsc ? escCount : byteCount, waitTime) && isOpened())
					escCount = hasEsc(tmpBytes = super.readBytes(readBytes==null ? byteCount : escCount));
			}

			Console.appendLn(ToHex.bytesToHex(tmpBytes), "Read");

			if(hasEsc){
				if(tmpBytes!=null){
					int index = readBytes.length;
					readBytes = Arrays.copyOf(readBytes, index+tmpBytes.length);
					System.arraycopy(tmpBytes, 0, readBytes, index, tmpBytes.length);
				}
			}else
				readBytes = tmpBytes;

			if(escCount>0){
				byteCount += escCount;
				hasEsc = true;
			}

		}while(escCount>0 && readBytes!=null && readBytes.length<byteCount);

		if(hasEsc)
			readBytes = byteStuffing(readBytes);

		logger.info(marker, "<< get:{}, hasEsc={}", ToHex.bytesToHex(readBytes), hasEsc);

		return readBytes;
	}

	public byte[] readBytes(byte[] readEnd, int waitTime) throws SerialPortException {

		int count = 0;
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

			if(getInputBufferBytesCount()>0){

				tmp = readBytes(getInputBufferBytesCount(), waitTime);

				if(tmp!=null){
					if(readBytes==null)
						readBytes = tmp;
					else{
						int l = readBytes.length;
						int tmpLength = tmp.length;
						readBytes = Arrays.copyOf(readBytes, l+tmpLength);
						System.arraycopy(tmp, 0, readBytes, l, tmpLength);
					}

					int end = readBytes.length-readEnd.length;

					if(end>0){
						for(byte b:readEnd)
							if(readBytes[end++]==b && ++count>=readEnd.length)
								isEnd = true;
					}
				}
			}else
				tmp = null;

		}while(!isEnd && tmp!=null);

		return readBytes;
	}

	private int hasEsc(byte[] readBytes) {

		int escCount = 0;

		if(readBytes!=null)
			for(byte b:readBytes)
				if(b==PacketImp.CONTROL_ESCAPE)
					escCount++;

		return escCount;
	}

	private byte[] byteStuffing(byte[] readBytes) {

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

	public void setRun(boolean run, String why) {
		logger.warn("setRun({}, {})", run, why);
		synchronized (this) {
			this.run = run;
			notify();
		}
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

			if (run && !isOpened) {
				logger.debug("openPort() Port Name={}", getPortName());
				isOpened = super.openPort();
				if (isOpened){
					addEventListener(serialPortEvent);
					setBaudrate();
				}
			}
		}
		return isOpened;
	}

	@Override
	public boolean closePort(){

		boolean isClosed = !isOpened();
		logger.debug("1) Port Name={} closePort()is Closed={}", getPortName(), isClosed);

		run = false;
		synchronized (logger) {
			if (!isClosed) {
				try {

					removeEventListener();
					boolean isPurged = purgePort(PURGE_RXCLEAR | PURGE_TXCLEAR | PURGE_RXABORT | PURGE_TXABORT);
					isClosed = super.closePort();
					logger.debug("2) closePort()is Closed={}, is purged={}",isClosed, isPurged);

				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}

		return isClosed;
	}

	//*** Class SerialPortEvent *****************************************************
	private class SerialPortEvent implements SerialPortEventListener{

		@Override
		public void serialEvent(jssc.SerialPortEvent serialPortEvent) {

			synchronized (ComPort.this) {
				isSerialPortEven = true;
				ComPort.this.notify();
//				Console.appendLn("", "notify");
			}
		}
		
	}

	public static int getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(int baudrate){
		ComPort.baudrate = baudrate;
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
