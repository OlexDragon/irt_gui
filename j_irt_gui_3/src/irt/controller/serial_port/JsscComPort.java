package irt.controller.serial_port;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.swing.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import irt.data.Checksum;
import irt.data.LoggerWorker;
import irt.data.PacketThread;
import irt.data.ToHex;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.head.Console;
import irt.tools.panel.head.IrtPanel;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class JsscComPort extends SerialPort implements SerialPortInterface {

	private static final String LOGGER_NAME = "comPort";

	private final Logger logger = LogManager.getLogger();
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
			return Integer.toString(baudrate);
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

	private int timeout = 3000;
	private Timer timer;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private LinkHeader linkHeader;
	private short packetId;
//	private boolean isSerialPortEven;
	private boolean isComfirm;

	public JsscComPort(String portName) {
		super(portName);

		LoggerWorker.setLoggerLevel(LOGGER_NAME, Level.toLevel(IrtPanel.PROPERTIES.getProperty("dump_serialport"), Level.ERROR));

		timer = new Timer(timeout, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try{
					Console.appendLn("Timeout", "Timer");
					synchronized (JsscComPort.this) {
						closePort();
					}
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
		timer.setRepeats(false);
	}

	public Packet send(PacketWork packetWork){
		logger.debug("ENTRY: {}", packetWork);

		Packet packet = packetWork.getPacketThread().getPacket();

		long start = System.currentTimeMillis();

		if(packet == null)
			return null;

		PacketHeader ph = packet.getHeader();
		byte groupId = ph.getGroupId();
		packetId = ph.getPacketId();
		Packet readPacket;


		if(packet instanceof LinkedPacket){
			linkHeader = Optional.ofNullable(packet).filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).filter(lh->lh.getAddr()!=0).orElse(null);//
			readPacket = new LinkedPacketImp(linkHeader);
		}else{
			linkHeader = null;
			readPacket = new PacketImp();
		}
		logger.debug("linkHeader = {}", linkHeader);

		int runTimes = 0;
		byte[] readData;
		int ev;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {



	Checksum checksum = null;
	logger.trace("\n\t {}\n\t", ph);

			timer.restart();
			clear();

			byte[] data = packet.toBytes();
			String hexStr = ToHex.bytesToHex(data);

			String prefix = (runTimes+1)+") send";

			logger.info(marker, ">> {}: {}", prefix, packet);
			logger.info(marker, ">> {}: {}", prefix, hexStr);
			Console.appendLn(packet, prefix);
			Console.appendLn(hexStr, prefix);

			if(data!=null && isOpened()){
				writeBytes(data);
				logger.debug("writeBytes: {}", ()->Arrays.toString(data));

				if ((isConfirmBytes()) && isFlagSequence()){

					if(linkHeader!=null){
						if((readData=readLinkHeader())!=null)
							checksum = new Checksum(readData);
						else{
							logger.warn("linkHeader==null");
							Console.appendLn("LinkHeader", "Break");
							return packet;
						}
					}

					if((readData=readHeader())!=null) {
						if(checksum!=null)
							checksum.add(readData);
						else
							checksum = new Checksum(readData);

						packetHeader = new PacketHeader(readData);
						logger.debug(packetHeader);

						if (packetHeader.toBytes() != null && packetHeader.getGroupId()==groupId) {
							readPacket.setHeader(packetHeader);
							payloadsList = new ArrayList<>();

							while ((readData = readParameterHeader())!=null) {

								if (containsFlagSequence(readData)) {
									cs = checksum.getChecksumAsBytes();

									if (cs[0] == readData[0] && cs[1] == readData[1]){
										readPacket.setPayloads(payloadsList);
									}
									logger.trace("END");
									break;
								}
								checksum.add(readData);
								parameterHeader = new ParameterHeader(readData);

								ev = parameterHeader.getSize();
								logger.trace("parameterHeader.getSize()={}", ev);
								if(parameterHeader.getCode()>300 || ev>3000){
									Console.appendLn("ParameterHeader Sizes", "Break ");
									logger.error(
											"parameterHeader.getCode()>300({}) || ev>3000({}) \n{} \n{} \n readData: {}",
											parameterHeader.getCode(),
											ev,
											packetHeader,
											parameterHeader,
											readData);
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
				}else 
					logger.warn("isFlagSequence() = false {}", packet);
			}else {
				logger.warn("the condition data!=null && isOpened({}) does not hold", isOpened());
			}

			Optional.ofNullable(getAcknowledge()).filter(a->isOpened()).ifPresent(acknowledge->{ try { writeBytes(acknowledge); } catch (SerialPortException e) { logger.catching(e); }});

				if(readPacket.getHeader()==null || readPacket.getPayloads()==null)
					readPacket = packet;

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "Error");
		}

		timer.stop();

		Console.appendLn(readPacket, "Get");
		Console.appendLn(""+(System.currentTimeMillis()-start), "Time");

		return readPacket;
	}

	private byte[] getAcknowledge() {
		byte[] b;

		if(linkHeader!=null)
			b = Arrays.copyOf(linkHeader.toBytes(), 7);
		else
			b = new byte[3];

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;

		byte[] packetId = PacketImp.toBytes(this.packetId);
		System.arraycopy(packetId, 0, b, ++idPosition, 2);

		final byte[] preparePacket = PacketThread.preparePacket(b);
		logger.debug("{}", preparePacket);

		return preparePacket;
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

		return isFlagSequence;
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
		int ev = Optional.ofNullable(linkHeader).map(LinkHeader::getIntAddr).filter(addr->addr>0).map(a->11).orElse(7);
		int index = ev - 3;

		byte[] readBytes = readBytes(ev, 100);

		logger.debug("readBytes= {}", readBytes);

		this.isComfirm = readBytes!=null && readBytes[0]==PacketImp.FLAG_SEQUENCE && readBytes[readBytes.length-1]==PacketImp.FLAG_SEQUENCE;

		//for converters
		if(!this.isComfirm && readBytes!=null && linkHeader!=null && readBytes[6]==PacketImp.FLAG_SEQUENCE)
					linkHeader = null;

		if(this.isComfirm){

			byte[] data = Arrays.copyOfRange(readBytes, 1, index);

			logger.trace("\n readBytes= {}\n LinkHeader data= {}", ()->Arrays.toString(readBytes), ()->Arrays.toString(data));

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
		}else
			logger.warn("Acknowledge is wrong({})", readBytes);

		logger.debug("RETURN: {}; size:{}; read bytes:{}", isComfirm, ev, readBytes);
		return isComfirm;
	}

	private short getPacketId(boolean isLinked, byte[] data) {
		return (short) PacketImp.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
	}

	@Override
	public String toString() {
		return "JsscComPort  - "+ getPortName();
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
			byte[] acknowledge = getAcknowledge();
			writeBytes(acknowledge);

			do {
				logger.info(marker, "?? Cleared {} bytes", ()->{ try { return getInputBufferBytesCount();} catch (SerialPortException e) {logger.catching(e); return e.getLocalizedMessage(); }});

				purgePort(SerialPort.PURGE_TXABORT | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXABORT | SerialPort.PURGE_RXCLEAR);

//				readBytes = super.readBytes(getInputBufferBytesCount());
				String readBytesStr = ToHex.bytesToHex(readBytes);
				Console.appendLn(readBytesStr, "Clear");
			} while (wait(1, 200));
		}
		return readBytes;
	}

	public byte[] readByte(int timeout) throws SerialPortException {
		logger.debug("timeout: {}", timeout);
		return readBytes(1, timeout);
	}

	@Override
	public byte[] readBytes(int byteCount) {
		logger.debug("ENTRY byteCount: {}", byteCount);

		return readBytes(byteCount, 50);
	}

	public byte[] readBytes(int byteCount, int waitTime) {
		logger.trace("ENTRY byteCount: {}; waitTime: {}", byteCount, waitTime);

		byte[] readBytes;
		try {

			if(!wait(byteCount, waitTime))
				return null;

			readBytes = super.readBytes(byteCount);

			final int escapes = escapeCount(readBytes);
			logger.debug("readBytes: {}", readBytes);
			if(escapes==0)
				return readBytes;

			final byte[] tmp = readBytes(escapes);

			if(tmp==null)
				return byteStuffing(readBytes);

			final int newLength = byteCount + escapes;
			readBytes = Arrays.copyOf(readBytes, newLength);
			System.arraycopy(tmp, 0, readBytes, byteCount, escapes);

			Console.appendLn(ToHex.bytesToHex(readBytes), "Read");

			return byteStuffing(readBytes);

		} catch (SerialPortException e) {
			logger.catching(e);
			return null;
		}
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

	private int escapeCount(final byte[] readBytes) {
		return (int) IntStream.range(0, readBytes.length).filter(i->readBytes[i]==PacketImp.CONTROL_ESCAPE).count();
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

	public boolean wait(int waitFor, int waitTime) throws SerialPortException {
		boolean isReady = false;
		long start = System.currentTimeMillis();
		long waitTimeL = waitTime*waitFor;
		logger.entry(waitFor, waitTime);

		while(isOpened() && !(isReady = getInputBufferBytesCount()>=waitFor) && (System.currentTimeMillis()-start) < waitTimeL){
			synchronized (this) {

				try {
					wait(waitTimeL);
				} catch (InterruptedException e) {
					logger.catching(Level.DEBUG, e);
				} catch (Exception e) {
					logger.catching(e);
				 }

//				isSerialPortEven = false;
			}
		};

		logger.info("waitFor: {} bytes; waitTimeL: {}; System.currentTimeMillis()-start: {};", ()->waitFor , ()->waitTimeL, ()->System.currentTimeMillis()-start);
//		isSerialPortEven = false;

		return isReady;
	}

	@Override
	public synchronized boolean openPort() throws SerialPortException {

		if (isOpened()) return true;
		
		if(!super.openPort()) return false;

		addEventListener(serialPortEvent);

		setBaudrate();

		logger.debug("Serial port {} is OPEND", this);
		return true;
	}

	@Override
	public synchronized boolean closePort(){
		logger.debug(this);

		if (!isOpened()) return true;

		try { removeEventListener(); } catch (SerialPortException e) { logger.catching(Level.DEBUG, e); }
		try { return super.closePort(); } catch (Exception e) { logger.catching(e); }

		return !isOpened();
	}

	//*** Class SerialPortEvent *****************************************************
	private class SerialPortEvent implements SerialPortEventListener{

		@Override
		public void serialEvent(jssc.SerialPortEvent serialPortEvent) {

			synchronized (JsscComPort.this) {
//				isSerialPortEven = true;
				JsscComPort.this.notify();
//				Console.appendLn("", "notify");
			}
		}
		
	}

	@Override
	public int getBaudrate() {
		return baudrate;
	}

	public void setBaudrate(int baudrate){
		JsscComPort.baudrate = baudrate;
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
