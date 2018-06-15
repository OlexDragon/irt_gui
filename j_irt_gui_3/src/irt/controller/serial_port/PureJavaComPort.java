
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
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.Checksum;
import irt.data.MyThreadFactory;
import irt.data.PacketThread;
import irt.data.ToHex;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.panel.head.Console;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.PureJavaSerialPort;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class PureJavaComPort implements SerialPortInterface {

	public static final int MAX_WAIT_TIME = 1500;

	private final static Logger logger = LogManager.getLogger();

    public static final int BAUDRATE_115200 = 115200;
	private static final int WAIT_TIME = 100;

	private final CommPortIdentifier portIdentifier;
	private boolean opened ;
	private PureJavaSerialPort serialPort;

	private InputStream inputStream;
	private OutputStream outputStream;
//	private volatile int available;//The use to reduce the time ( inputStream.available() - takes a lot of time )
	private byte[] buffer;//The use to read all available bytes ( inputStream.read(byte[]) - takes a lot of time )

	/**
	 * This variable use to reduce the time to send acknowledgment 
	 */
	private long flagSequenceCount;
	private byte[] confirmBytes;

	private boolean readToTheBuffer;

	public PureJavaComPort(String portName) throws NoSuchPortException {
		portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public synchronized Packet send(PacketWork packetWork) {

		logger.entry(packetWork);

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
		Packet packet;

		LinkHeader linkHeader;
		if(p instanceof LinkedPacket){
			linkHeader = Optional.ofNullable(((LinkedPacket)p).getLinkHeader()).filter(lh->lh.getAddr()!=0).orElse(null);
			packet = new LinkedPacketImp(linkHeader);
		}else{
			linkHeader = null;
			packet = new PacketImp();
		}

		int runTimes = 0;
		byte[] readData;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {

	Checksum checksum = null;

	clear();

	byte[] data = pt.getData();
	String hexStr = ToHex.bytesToHex(data);

	String prefix = (runTimes+1)+") send";
	Console.appendLn(p, prefix);
	Console.appendLn(hexStr, prefix);

	if(data!=null && opened){
		outputStream.write(data);

		logger.debug("writeBytes({})", hexStr);

		if (isConfirmBytes(p)){

			if (isFlagSequence()) {

				if (linkHeader != null)
					if ((readData = readLinkHeader()) != null)
						checksum = new Checksum(byteStuffing(readData));
					else {
						logger.warn("\n\tlinkHeader==null\n\tSent packet {}", p);
						Console.appendLn("LinkHeader", "Break");
					}

				if ((readData = readHeader()) != null) {

					final byte[] headerByteStuffing = byteStuffing(readData);

					if (checksum != null)
						checksum.add(headerByteStuffing);
					else
						checksum = new Checksum(headerByteStuffing);

					packetHeader = new PacketHeader(headerByteStuffing);
					logger.debug(packetHeader);

					final byte newGroupId = packetHeader.getGroupId();
					if (newGroupId == groupId) {
						packet.setHeader(packetHeader);
						payloadsList = new ArrayList<>();

						while (opened && (readData = readParameterHeader()) != null) {

							if (containsFlagSequence(readData)) {
								cs = checksum.getChecksumAsBytes();

								final byte[] rd = byteStuffing(readData);

								if (cs[0] == rd[0] && cs[1] == rd[1]) {
									packet.setPayloads(payloadsList);
								}else
									logger.warn("checksum error: {} : {}", ()->ToHex.bytesToHex(cs), ()->ToHex.bytesToHex(rd));

								logger.trace("END");
								break;
							}

							final byte[] byteStuffing = byteStuffing(readData);
							checksum.add(byteStuffing);
							parameterHeader = new ParameterHeader(byteStuffing);

							final ParameterHeader pHeader = parameterHeader;
							logger.debug("{} buffer: {}", ()->pHeader, ()->ToHex.bytesToHex(buffer));

							int payloadSize = parameterHeader.getSize();

							if (parameterHeader.getCode()>300 || payloadSize>3000 || payloadSize<=0 ) {
								Console.appendLn("ParameterHeader Sizes", "Break ");
								logger.warn("parameterHeader.getCode()>300({}) || payloadSize>3000({}) || payloadSize<=0({}) \n{} \n{}\nSent Packet{}", parameterHeader.getCode(), payloadSize, payloadSize, packetHeader, parameterHeader, p);
								break;
							}

										Console.appendLn("", "Payload ");
										if (payloadSize <= 0) {
											Console.appendLn("Payload", "Break ");
											logger.warn("ev < 0 || (readData = readBytes(ev))==null");
											break;
										}

										if(waitComPort(payloadSize)){

											readData = readBytes(payloadSize);
											final byte[] payloadByteStuffing = byteStuffing(readData);

											final byte[] rd = readData;
											logger.debug("payload size: {}; parameter data: {}", ()->payloadSize, ()->ToHex.bytesToHex(rd));

											if (Optional.ofNullable(payloadByteStuffing).map(bs->bs.length).orElse(0)>=payloadSize) {

												checksum.add(payloadByteStuffing);
												Payload payload = new Payload(parameterHeader, payloadByteStuffing);
												payloadsList.add(payload);

											}else
												logger.warn("readData.length>={}", payloadSize);
										}else{
											logger.warn("No data or wrong data size({}), buffer: {}", payloadSize, buffer);
											break;
										}
									}
								} else
									logger.warn("packetHeader.asBytes() == null || packetHeader.getGroupId({})!={}", newGroupId, groupId);
							} else
								logger.warn("(readData=readHeader())==null");
						}else
							logger.warn("isFlagSequence returns false\nSent packet{}", p);
				}else
					logger.warn("isConfirmBytes returns false\nSent Packet:{}", p);
			}else 
				logger.warn("data!=null && isOpened() does not hold");

	sendAcknowledge(p);

		} catch (InterruptedException | PureJavaIllegalStateException e) {
			new MyThreadFactory(()->closePort());
		}catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "Error");
			new MyThreadFactory(()->closePort());
		}

		if(packet.getHeader()==null || packet.getPayloads()==null)
			packet = p;

		Console.appendLn(packet, "Get");
		final long workTime = System.currentTimeMillis()-start;
		Console.appendLn(""+workTime, "Time");
		logger.debug("Time taken to send the packet: {}", workTime);

//		logger.traceExit(packet);
		return packet;
	}

	private void sendAcknowledge(Packet p) throws IOException {

		byte[] acknowledge = getAcknowledge(p);
		outputStream.write(acknowledge);
		logger.trace(()->ToHex.bytesToHex(acknowledge));
	}

	public void clear() throws Exception {
		logger.traceEntry();

		flagSequenceCount = 0;
		confirmBytes = null;


		 do{

			 Optional .ofNullable(getBuffer()).filter(b->b.length>0).ifPresent(b->logger.debug("cleared {} bytes; {}", b.length, b));

			setBuffer(null);

			synchronized (this) { wait(50); }

		 }while (readToTheBuffer || isBuffer());

		logger.traceExit();
	}

	public boolean waitComPort(int watedBytes){
		logger.debug("ENTRY: watedBytes: {}; WAIT_TIME: {}; buffer.length: {}", ()->watedBytes,  ()->WAIT_TIME, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null));
		long start = System.currentTimeMillis();

		// control maximum timeout
		final int wait = Optional.of(watedBytes*WAIT_TIME).filter(wt->wt<MAX_WAIT_TIME).orElse(MAX_WAIT_TIME);

		return Optional
				.ofNullable(getBuffer())
				.filter(b->b.length>=watedBytes)
				.filter(b->b.length>=watedBytes + (int) IntStream.range(0, watedBytes-1).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(b->true)
				.orElseGet(

						()->{

							int bufferLength = 0;
							int newSize;

							do{

								synchronized (PureJavaComPort.this) { try { PureJavaComPort.this.wait(wait); } catch (InterruptedException e) { logger.catching(e); } }

								final Optional<byte[]> oBuffer = Optional.ofNullable(getBuffer());

								newSize = watedBytes + oBuffer.filter(b->b.length>=watedBytes).map(b->(int) IntStream.range(0, watedBytes-1).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count()).orElse(0);
								bufferLength = oBuffer.map(b->b.length).orElse(0);

							}while(bufferLength<newSize && System.currentTimeMillis()-start < wait);

							final int ns = newSize;
							return Optional.ofNullable(getBuffer()).map(b->b.length>=ns).orElse(bufferLength>=ns);

						});
	}

	private byte[] getAcknowledge(Packet packet) {
		logger.traceEntry();
		byte[] b = Optional
				.of(packet)
				.filter(LinkedPacket.class::isInstance)
				.map(LinkedPacket.class::cast)
				.map(LinkedPacket::getLinkHeader)
				.map(LinkHeader::toBytes)
				.filter(bs->bs.length>0)
				.map(bs->Arrays.copyOf(bs, 7))
				.orElse(new byte[3]);

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;

		byte[] packetId = PacketImp.toBytes(packet.getHeader().getPacketId());
		System.arraycopy(packetId, 0, b, ++idPosition, 2);

		return PacketThread.preparePacket(b);
	}

	private boolean isConfirmBytes(Packet packet) throws Exception {
		logger.traceEntry();

		final Optional<LinkHeader> oLinkHeader = Optional
				.ofNullable(packet)
				.filter(LinkedPacket.class::isInstance)
				.map(LinkedPacket.class::cast)
				.map(LinkedPacket::getLinkHeader)
				.filter(lh->lh.getAddr()!=0);	// 0 -> converter

		int size = oLinkHeader
				.map(a->11)
				.orElse(7);
//
//		if(!waitComPort(size)){
//			logger.warn("requires {} bytes, but is {} bytes", ()->size, ()->Optional.ofNullable(getBuffer()).map(b->b.length).orElse(0));
//			return false;
//		}

		final boolean waitComPort = waitComPort(size);
		if(waitComPort)
			confirmBytes = readBytes(size);

		boolean isFlafDequence = confirmBytes!=null && confirmBytes[0]==PacketImp.FLAG_SEQUENCE && confirmBytes[confirmBytes.length-1]==PacketImp.FLAG_SEQUENCE;

		final boolean fs = isFlafDequence;
		logger.debug("\n need {} bytes\n readBytes= {}\n ConfirmBytes contain flag sequence: {}", ()->size, ()->ToHex.bytesToHex(confirmBytes), ()->fs);

		if(isFlafDequence){

			final byte[] bufferCopy = getBuffer();

			flagSequenceCount += Optional.ofNullable(bufferCopy).map(b->IntStream.range(0, b.length)).orElse(IntStream.empty()).filter(index->bufferCopy[index]==PacketImp.FLAG_SEQUENCE).count();
			if(flagSequenceCount>=2)
				sendAcknowledge(packet);

			int index = size - 3;
			byte[] data = Arrays.copyOfRange(byteStuffing(confirmBytes), 1, index);

			Boolean isLinked = null;
			if(oLinkHeader.isPresent()){

				LinkHeader lh = new LinkHeader(data);

				// (isLinked == null) -> converter
				isLinked = oLinkHeader
						.map(lh::equals)
						.orElse(null);
			}

			if(isLinked==null || isLinked){

				final short idReceived = getPacketId(isLinked!=null, data);
				final short idSent = packet.getHeader().getPacketId();

				if(idSent==idReceived){

					Checksum cs = new Checksum(data);
					byte[] b = cs.getChecksumAsBytes();

					if(b[0]==confirmBytes[index] && b[1]==confirmBytes[++index])
						isFlafDequence = true;
					else
						logger.warn("Checksum ERROR ({})", ToHex.bytesToHex(confirmBytes));
				}else
					logger.warn("Packet IDs are not equal (sent={}, received={}\nSent packet:{}", idSent, idReceived, packet);
			}else
				oLinkHeader
				.ifPresent(linkHeader->logger.warn("LinckHeaders are not equal (sent linkHeader={}, received={}", linkHeader.toBytes(), ToHex.bytesToHex(confirmBytes)));
		}
//
//		if(buffer!=null){
//			long flagsSequence = IntStream.range(0, buffer.length).filter(index->buffer[index]==PacketImp.FLAG_SEQUENCE).count();
//			if(isFlafDequence && flagsSequence>=2){
//				outputStream.write(readBytes);
//				logger.error("Sent Confirm Bytes Back: {}", ToHex.bytesToHex(readBytes));
//			}
//		}
		return isFlafDequence;
	}

	@Override
	public byte[] readBytes(final int size) throws Exception {

		return Optional.ofNullable(getBuffer())
				.map(bf->bf.length)
				.filter(bLength->bLength >= size)
				.filter(bLength->bLength >= size + (int) IntStream.range(0, size).filter(index->getBuffer()[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(bLength->{
					try {
						return getAPartFromTheBuffer(size);
					} catch (IOException e1) {
						logger.catching(e1);
					}
					return null;
				})
				.orElseGet(()->{
					try {

						if(waitComPort(size))
							return readBytes(size);

					} catch (Exception e) {
						logger.catching(e);
					}

					return null;
				});
	}

	private synchronized void readToTheBuffer() throws IOException {
		logger.traceEntry();

		readToTheBuffer = true;

//		synchronized(PureJavaComPort.this){

			final byte[] bytes = new byte[inputStream.available()];

			if(bytes.length==0)
				return;

			inputStream.read(bytes);
			logger.debug("read bytes: {}; bytes: {}\n buffer: {}", ()->bytes.length, ()->ToHex.bytesToHex(bytes), ()->ToHex.bytesToHex(buffer));

			setBuffer(Optional
					.ofNullable(buffer)
					.map(bf->{

						final int oldLength = bf.length;
						bf = Arrays.copyOf(bf, oldLength + bytes.length);
						System.arraycopy(bytes, 0, bf, oldLength, bytes.length);

						return bf;
					})
					.orElse(bytes));

			flagSequenceCount += Optional.ofNullable(buffer).map(b->IntStream.range(0, b.length)).orElse(IntStream.empty()).filter(index->buffer[index]==PacketImp.FLAG_SEQUENCE).count();
			if(flagSequenceCount>=2 && confirmBytes!=null)
				outputStream.write(confirmBytes);
//		}

		readToTheBuffer = false;
	}

	private synchronized byte[] getAPartFromTheBuffer(final int size) throws IOException {
		logger.trace("ENTRY size: {}; buffer: {}", ()->size, ()->ToHex.bytesToHex(buffer));

		byte[] result;

		final Optional<byte[]> oBuffer = Optional.ofNullable(getBuffer());
		final int newSize = size + oBuffer.map(b->(int) IntStream.range(0, size).filter(index->getBuffer()[index]==PacketImp.CONTROL_ESCAPE).count()).orElse(0);

		logger.debug("Size to get: {}", newSize);

		if(buffer==null || buffer.length<newSize)
			return null;

//		synchronized (PureJavaComPort.this) {
			
			if(buffer.length==newSize){
				 result = buffer;
				 buffer = null;

			}else{
				result = Arrays.copyOf(buffer, newSize);
				buffer = Arrays.copyOfRange(buffer, newSize, buffer.length);
			}
//		}

//		final byte[] r = result;
//		result = Optional
//				.of(result)
//				.map(b->(int) IntStream.range(0, newSize).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count())
//				.filter(s->s!=0)
//				.map(s->{
//
//					byte[] copyOf;
//
//					try {
//
//						byte[] tmp = getAPartFromTheBuffer(s);
//						if(tmp==null || tmp.length<s)
//								return null;
//
//						copyOf = Arrays.copyOf(r, r.length + s);
//						System.arraycopy(tmp, 0, copyOf, r.length, tmp.length);
//
//					} catch (IOException e) {
//						logger.catching(e);
//						copyOf = r;
//					}
//
//					return copyOf;
//				})
//				.orElse(result);

		return result;
	}

	public boolean isFlagSequence() throws Exception {
		logger.traceEntry();

		waitComPort(10);//TODO - test with smaller value
		final byte[] readBytes = readBytes(1);
		if(readBytes==null)
			return false;

		return readBytes[0] == PacketImp.FLAG_SEQUENCE;
	}

	private short getPacketId(boolean isLinked, byte[] data) {
		return (short) PacketImp.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
	}

	private byte[] readLinkHeader() throws Exception {
	
		if(!waitComPort(LinkHeader.SIZE))
			return null;

		final byte[] readBytes = readBytes(LinkHeader.SIZE);

		logger.debug("Exit readBytes={}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readHeader() throws Exception {

		if(!waitComPort(PacketHeader.SIZE))
			return null;

		final byte[] readBytes = readBytes(PacketHeader.SIZE);

		logger.debug("EXIT readBytes: {}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readParameterHeader() throws Exception {

		if(!waitComPort(ParameterHeader.SIZE))
			return null;

		byte[] readBytes = readBytes(ParameterHeader.SIZE);

		logger.debug("EXIT size: {}; readBytes: {};", ()->ParameterHeader.SIZE, ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private boolean containsFlagSequence(byte[] readBytes) {
		logger.traceEntry(()->ToHex.bytesToHex(readBytes));

		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==PacketImp.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		logger.traceExit(isFlagSequence);
		return isFlagSequence;
	}

	@Override
	public void setRun(boolean b, String string) {
	}

	@Override
	public String getPortName() {
		return portIdentifier.getName();
	}

	@Override
	public synchronized boolean openPort() throws Exception {

		if(opened)
			return true;

		serialPort = (PureJavaSerialPort) portIdentifier.open(PureJavaComPort.class.getName(), PureJavaComPort.MAX_WAIT_TIME);
		setBaudrate(BAUDRATE_115200);
		serialPort.enableReceiveThreshold(PureJavaComPort.MAX_WAIT_TIME);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();

		serialPort.notifyOnDataAvailable(true);
		serialPort.addEventListener(e->{

//			new MyThreadFactory().newThread(()->{
			
				if(opened)
					try {

						readToTheBuffer();
					
						synchronized (PureJavaComPort.this) { PureJavaComPort.this.notify(); }

					} catch (IOException e1) { logger.catching(e1); }
//			})
//			.start();
		});

		return opened = true;
	}

	@Override
	public synchronized boolean closePort() {

		if(!opened)
			return true;

		opened = false;
		buffer = null;

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
			serialPort.disableReceiveTimeout();
			serialPort.close();
		}

		return true;
	}

	@Override
	public void setBaudrate(int baudrate) {
		logger.entry(baudrate);
		try {
			serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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

	public enum Baudrate{
		BAUDRATE_9600	(9600),
		BAUDRATE_19200	(19200),
		BAUDRATE_38400	(38400),
		BAUDRATE_57600	(57600),
		BAUDRATE_115200	(115200);

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

	@Override
	public int getBaudrate() {
		return serialPort.getBaudRate();
	}

	public synchronized byte[] getBuffer() {
		return buffer;
	}

	public synchronized void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	private synchronized boolean isBuffer() {
		return buffer!=null && buffer.length!=0;
	}

	@Override
	public String toString() {
		return Optional.ofNullable(portIdentifier).map(CommPortIdentifier::getName).orElse(null);
	}
}
