
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.Checksum;
import irt.data.MyThreadFactory;
import irt.data.ToHex;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacketImp;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.panel.head.Console;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.PureJavaSerialPort;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class PureJavaComPort implements SerialPortInterface {

	private static final int CLEAR_TIME_BUC = 100;
	private static final int WAIT_TIME_BUC = 45;

	private static final int CLEAR_TIME_CONVERTER = 110;
	private static final int WAIT_TIME_CONVERTER = 20;

	public static final int MAX_WAIT_TIME = 1500;

	private final static Logger logger = LogManager.getLogger();

    public static final int BAUDRATE_115200 = 115200;

	private final CommPortIdentifier PORT_IDENTIFIER;
	private Level LOGGER_LEVEL;

	private boolean opened ;
	private PureJavaSerialPort serialPort;

	private InputStream inputStream;
	private OutputStream outputStream;

	private int position;
	private byte[] buffer;//The use to read all available bytes ( inputStream.read(byte[]) - takes a lot of time )

	private boolean readToTheBuffer;

	private long clearTimeout;
	private int waitTime;
	private LinkHeader linkHeader;
	private Optional<Short> oLogger;

	public PureJavaComPort(String portName) throws NoSuchPortException {
		PORT_IDENTIFIER = CommPortIdentifier.getPortIdentifier(portName);
		LOGGER_LEVEL = logger.getLevel();
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public synchronized Packet send(PacketWork packetWork){
		Packet packet = packetWork.getPacketThread().getPacket();

		long start = System.currentTimeMillis();

		if(packet==null)
			return null;

		final PacketIDs packetIDToTest = PacketIDs.CONFIGURATION_MUTE;

		Packet readPacket = Optional

				.ofNullable(packet)
				.filter(LinkedPacket.class::isInstance)
				.map(LinkedPacket.class::cast)
				.map(LinkedPacket::getLinkHeader)
				.filter(lh->lh.getAddr()!=0)
				.map(LinkedPacketImp::new)
				.map(
						p->{
							waitTime = PureJavaComPort.WAIT_TIME_BUC;
							clearTimeout = PureJavaComPort.CLEAR_TIME_BUC;
							linkHeader = p.getLinkHeader();
							return (Packet)p;
						})
				.orElseGet(()->{
					waitTime = PureJavaComPort.WAIT_TIME_CONVERTER;
					clearTimeout = PureJavaComPort.CLEAR_TIME_CONVERTER;
					linkHeader = null;
					return new PacketImp();
				});

		byte[] readData;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {

	Checksum checksum = null;

	clear();

	PacketHeader ph = packet.getHeader();
	byte groupId = ph.getGroupId();
	oLogger = Optional.of(LOGGER_LEVEL).filter(ll->ll.compareTo(Level.DEBUG)>=0).map(ll->ph.getPacketId()).filter(packetIDToTest::match);
	oLogger.ifPresent(pId->logger.debug("send(PacketWork) ENTRY {}", packet));
//	logger.error("{}; {}", oLogger, ph); 

	byte[] data = packet.toBytes();

	new MyThreadFactory(()->{
		
		final String hexStr = ToHex.bytesToHex(data);

		String prefix = "send";
		Console.appendLn(packet, prefix);
		Console.appendLn(hexStr, prefix);
		oLogger.ifPresent(pId->logger.debug("writeBytes({})", hexStr));
	}, "Send to Console");

	if(data!=null && opened){

		outputStream.write(data);


		if (isConfirmBytes()){

			if (isFlagSequence()) {

				if (readPacket instanceof LinkedPacket){
					if ((readData = readLinkHeader()) != null)
						checksum = new Checksum(readData);
					else {
						logger.info("\n\tlinkHeader==null\n\tSent packet {}", packet);
						Console.appendLn("LinkHeader", "Break");
					}
				}

				if ((readData = readHeader()) != null) {


					if (checksum != null)
						checksum.add(readData);
					else
						checksum = new Checksum(readData);

					packetHeader = new PacketHeader(readData);
					oLogger.ifPresent(pId->logger.debug(packetHeader));

					final byte newGroupId = packetHeader.getGroupId();
					if (newGroupId == groupId) {
						readPacket.setHeader(packetHeader);
						payloadsList = new ArrayList<>();

						while (opened && (readData = readParameterHeader()) != null) {

							if (containsFlagSequence(readData)) {
								cs = checksum.getChecksumAsBytes();

								final byte[] rd = readData;

								if (cs[0] == rd[0] && cs[1] == rd[1]) {
									readPacket.setPayloads(payloadsList);
								}else
									logger.warn("checksum error: {} : {}", ()->ToHex.bytesToHex(cs), ()->ToHex.bytesToHex(rd));

								final Packet rp = readPacket;
								oLogger.ifPresent(pId->logger.debug("END {}", rp));
								break;
							}

							checksum.add(readData);
							parameterHeader = new ParameterHeader(readData);

							int payloadSize = parameterHeader.getSize();

							if (parameterHeader.getCode()>300 || payloadSize>3000 || payloadSize<=0 ) {
								Console.appendLn("ParameterHeader Sizes", "Break ");
								logger.warn("parameterHeader.getCode()>300({}) || payloadSize>3000({}) || payloadSize<=0({}) \n{} \n{}\nSent Packet{}", parameterHeader.getCode(), payloadSize, payloadSize, packetHeader, parameterHeader, packet);
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

											final byte[] rd = readData;
											oLogger.ifPresent(pId->logger.debug("payload size: {}; parameter data: {}", ()->payloadSize, ()->ToHex.bytesToHex(rd)));

											if (Optional.ofNullable(readData).map(bs->bs.length).orElse(0)>=payloadSize) {

												checksum.add(readData);
												Payload payload = new Payload(parameterHeader, readData);
												payloadsList.add(payload);
												oLogger.ifPresent(pId->logger.debug("{}\n\t buffer: {}", payload, buffer));

											}else{
												logger.warn("readData.length>={}; payloadByteStuffing: {}", payloadSize, readData);
												break;
											}
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
							logger.warn("isFlagSequence returns false\nSent packet{}", packet);
				}else
					logger.warn("isConfirmBytes returns false\nSent Packet:{}", packet);
			}else 
				logger.warn("data!=null && isOpened() does not hold");

		} catch (InterruptedException | PureJavaIllegalStateException e) {
			logger.catching(Level.DEBUG, e);
			new MyThreadFactory(()->closePort(), "PureJavaComPort.send-1");
		}catch (Exception e) {
			logger.error("Error to send Packet: {}", packet);
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "Error");
			new MyThreadFactory(()->closePort(), "PureJavaComPort.send-2");
		}

		if(readPacket.getHeader()==null || readPacket.getPayloads()==null)
			readPacket = packet;

		Console.appendLn(readPacket, "Get");
		final long workTime = System.currentTimeMillis()-start;
		Console.appendLn(""+workTime, "Time");
		final Packet rp = readPacket;
		oLogger.ifPresent(pId->logger.debug("EXIT send(PacketWork packetWork) - Time taken to send the packet: {}; read packet: {}", workTime, rp));

		return readPacket;
	}

	public void clear() throws Exception {

		position = 0;

		do{
			if(oLogger!=null)
				oLogger.ifPresent(pId->logger.debug("cleared {} bytes; {}", ()->Optional .ofNullable(buffer).map(b->b.length).orElse(0), ()->buffer));

			buffer = null;

			synchronized (this) { wait(clearTimeout); }

		 }while (readToTheBuffer || isBuffer());

	}

	public boolean waitComPort(int watedBytes){
		oLogger.ifPresent(pId->logger.debug("ENTRY: waitComPort(int watedBytes: {}); WAIT_TIME: {}; buffer.length: {}", ()->watedBytes,  ()->waitTime, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null)));
		long start = System.currentTimeMillis();

		// control maximum timeout
		final int wait = Optional.of(watedBytes*waitTime).filter(wt->wt<MAX_WAIT_TIME).orElse(MAX_WAIT_TIME);
		int size = position + watedBytes;

		final Boolean result = Optional
				.ofNullable(buffer)
				.filter(b->b.length>=size)
				.filter(b->b.length>=size + (int) IntStream.range(position, size).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(b->true)
				.orElseGet(

						()->{

							int bufferLength = 0;
							int newSize;

							long waitingTime;
							do{

								synchronized (PureJavaComPort.this) { try { PureJavaComPort.this.wait(wait); } catch (InterruptedException e) {} }

								final Optional<byte[]> oBuffer = Optional.ofNullable(buffer);

								newSize = size + oBuffer.filter(b->b.length>=size).map(b->(int) IntStream.range(position, size).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count()).orElse(0);
								bufferLength = oBuffer.map(b->b.length).orElse(0);

								waitingTime = System.currentTimeMillis()-start;
							}while(bufferLength<newSize && waitingTime < wait);

							final int ns = newSize;
							final long wt = waitingTime;
							oLogger.ifPresent(pId->logger.debug("waitComPort({}) should be {} bytes:  wait time: {}", watedBytes, ns, wt));

							return Optional.ofNullable(buffer).filter(b->b.length>=ns).isPresent();
						});

		oLogger.ifPresent(pId->logger.debug("waitComPort({}) RETURN:  {}\n\t buffer: {}", watedBytes, result, buffer));
		return result;
	}

	private boolean isConfirmBytes() throws Exception {

		byte[] confirmBytes;
		final Optional<LinkHeader> oLinkHeader = Optional.ofNullable(linkHeader);
		final Integer size = oLinkHeader.map(lh->11).orElse(7);

		if(waitComPort(size))
			confirmBytes = readBytes(size);
		else
			return false;

		oLogger.ifPresent(pId->logger.debug("isConfirmBytes(): {}", confirmBytes));


		// No LinkHeader(converter) or Link addresses equal
		boolean linkHeadersEqual = !oLinkHeader

					.filter(lh->!lh.equals(new LinkHeader(Arrays.copyOfRange(confirmBytes, 1, confirmBytes.length-1))))
					.isPresent();

		if(linkHeadersEqual){

				oLogger.ifPresent(pId->logger.debug("isConfirmBytes() - byteStuffing : {}", confirmBytes));
				int checksumIndex = confirmBytes.length - 3;	// 2 bytes checksum and 1 byte PacketImp.FLAG_SEQUENCE
				byte[] data = Arrays.copyOfRange(confirmBytes, 1, checksumIndex);

				byte[] b = new Checksum(data).getChecksumAsBytes();

				if(b[0]==confirmBytes[checksumIndex] && b[1]==confirmBytes[++checksumIndex])
					return true;
				else
					logger.warn("Checksum ERROR ({})", ()->ToHex.bytesToHex(confirmBytes));
			}else
				oLinkHeader
				.ifPresent(linkHeader->logger.warn("LinckHeaders are not equal (sent linkHeader={}, received={}", linkHeader.toBytes(), confirmBytes));

		return false;
	}

	@Override
	public synchronized byte[] readBytes(final int size) throws Exception {
		oLogger.ifPresent(pId->logger.debug("ENTRY readBytes(final int size: {}); position: {}", size, position));

		return Optional.ofNullable(buffer)
				.map(bf->bf.length)
				.filter(length->length >= size + (int) IntStream.range(0, size).filter(index->buffer[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(bLength->{
					try {
						return getAPartOfTheBuffer(size);
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

	private synchronized void writeToBuffer() {

		readToTheBuffer = true;

		byte[] bytes;
		try {
			bytes = new byte[inputStream.available()];

			if(bytes.length==0)
				return;

			inputStream.read(bytes);

			oLogger.ifPresent(pId->logger.debug("writeToBuffer():\n\t read bytes: {};\n\t bytes: {}\n\t buffer: {}", bytes.length, bytes, buffer));

			buffer =Optional
				.ofNullable(buffer)
				.map(bf->{

					final int oldLength = bf.length;
					bf = Arrays.copyOf(bf, oldLength + bytes.length);
					System.arraycopy(bytes, 0, bf, oldLength, bytes.length);

					return bf;
				})
				.orElse(bytes);

		} catch (IOException e1) {
			logger.catching(e1);
		}

		Optional
		.ofNullable(buffer)
		.filter(bs->bs.length>0)
		.map(bs->IntStream.range(0, bs.length).map(index->bs[index]).filter(b->b==PacketImp.FLAG_SEQUENCE).count())
		.filter(count->count>=4)
		.ifPresent(
				count-> {
					try {
						int acknowledgeSize = 0;
						Integer size = Optional.ofNullable(linkHeader).map(l->11).orElse(7);
						for(int i=0; i<buffer.length; i++){
							int index = size + i;
							if(buffer[index]==PacketImp.FLAG_SEQUENCE){
								acknowledgeSize = index + 1;
								break;
							}
						}

						if(acknowledgeSize>0){
							byte[] acknowledge = Arrays.copyOf(buffer, acknowledgeSize);
							outputStream.write(acknowledge);
							oLogger.ifPresent(pId->logger.debug("sendAcknowledge(): {}", acknowledge));
						}
					} catch (IOException e) {
						logger.catching(e);
					}
				});

		readToTheBuffer = false;
		PureJavaComPort.this.notifyAll();
	}

	private synchronized byte[] getAPartOfTheBuffer(final int size) throws IOException {
		oLogger.ifPresent(pId->logger.debug("ENTRY positiob: {}; getAPartOfTheBuffer(final int size: {});\n buffer: {}", position, size, buffer));

		final int end = position + size;
		return getBuffer()
				.filter(b->b.length>=end)
				.map(
						b->{

							int newSize = size + (int) IntStream.range(position, end).filter(index->buffer[index]==PacketImp.CONTROL_ESCAPE).count();

							int available = b.length - position;

							if(available<newSize)
								return null;

							byte[] result;
							if(available==newSize && position==0){

								result = b;
								position = b.length;
							}else {

								final int newPosition = position + newSize;
								result = Arrays.copyOfRange(b, position, newPosition);
								position = newPosition;
							}

							if(newSize>size)
								result = byteStuffing(result);

							final byte[] r = result;
							oLogger.ifPresent(pId->logger.debug("getAPartOfTheBuffer({}); newSize: {}, position: {}; result: {}", size, newSize, position, r));
							return result;
						})
				.orElse(null);
	}

	public boolean isFlagSequence() throws Exception {
		oLogger.ifPresent(pId->logger.debug("isFlagSequence() - buffer: {}; position: {}", buffer, position));

		if(!waitComPort(10))
			return false;

		final byte[] readBytes = readBytes(1);
		if(readBytes==null)
			return false;

		return readBytes[0] == PacketImp.FLAG_SEQUENCE;
	}

	private byte[] readLinkHeader() throws Exception {
	
		if(!waitComPort(LinkHeader.SIZE))
			return null;

		final byte[] readBytes = readBytes(LinkHeader.SIZE);

		oLogger.ifPresent(pId->logger.debug("Exit readBytes={}", ()->ToHex.bytesToHex(readBytes)));

		return readBytes;
	}

	private byte[] readHeader() throws Exception {

		if(!waitComPort(PacketHeader.SIZE))
			return null;

		final byte[] readBytes = readBytes(PacketHeader.SIZE);

		oLogger.ifPresent(pId->logger.debug("EXIT readBytes: {}", ()->ToHex.bytesToHex(readBytes)));

		return readBytes;
	}

	private byte[] readParameterHeader() throws Exception {

		if(!waitComPort(ParameterHeader.SIZE))
			return null;

		byte[] readBytes = readBytes(ParameterHeader.SIZE);

		oLogger.ifPresent(pId->logger.debug("EXIT size: {}; readBytes: {};", ()->ParameterHeader.SIZE, ()->ToHex.bytesToHex(readBytes)));

		return readBytes;
	}

	private boolean containsFlagSequence(byte[] readBytes) {
		oLogger.ifPresent(pId->logger.debug(()->ToHex.bytesToHex(readBytes)));

		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==PacketImp.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		final boolean is = isFlagSequence;
		oLogger.ifPresent(pId->logger.debug(is));
		return isFlagSequence;
	}

	@Override
	public String getPortName() {
		return PORT_IDENTIFIER.getName();
	}

	@Override
	public synchronized boolean openPort() throws Exception {

		if(opened)
			return true;

		serialPort = (PureJavaSerialPort) PORT_IDENTIFIER.open(PureJavaComPort.class.getName(), PureJavaComPort.MAX_WAIT_TIME);
		setBaudrate(BAUDRATE_115200);
		serialPort.enableReceiveThreshold(PureJavaComPort.MAX_WAIT_TIME);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();

		serialPort.notifyOnDataAvailable(true);
		serialPort.addEventListener(e->writeToBuffer());

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

		buffer = null;

		return true;
	}

	@Override
	public void setBaudrate(int baudrate) {

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

	public synchronized Optional<byte[]> getBuffer() {
		return Optional.ofNullable(buffer);
	}

	private boolean isBuffer() {
		return buffer!=null && buffer.length!=0;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " - " + Optional.ofNullable(PORT_IDENTIFIER).map(CommPortIdentifier::getName).orElse(null);
	}
}
