
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
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

public class MyComPort implements SerialPortInterface,  AutoCloseable {

	private final static Logger logger = LogManager.getLogger();

    public static final int BAUDRATE_115200 = 115200;
	private static final int WAIT_TIME = 50;

	private final CommPortIdentifier portIdentifier;
	private boolean opened ;
	private SerialPort serialPort;

	private InputStream inputStream;
	private OutputStream outputStream;
	private int available;//The use to reduce the time ( inputStream.available() - takes a lot of time )
	private byte[] buffer;//The use to read all available bytes ( inputStream.read(byte[]) - takes a lot of time )

	public MyComPort(String portName) throws NoSuchPortException {
		portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	}

	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public Packet send(PacketWork packetWork) {

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
			linkHeader = Optional.ofNullable(((LinkedPacket)p).getLinkHeader()).filter(lh->lh.getAddr()!=0).orElse(null);//
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

			if(data!=null && isOpened()){
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
										logger.debug("{}\n {}", parameterHeader, ToHex.bytesToHex(buffer));

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

										if(wait(payloadSize, Optional.of(payloadSize*10).filter(wt->wt<1000).orElse(1000))>=payloadSize){

											final byte[] rd = readData = readBytes(payloadSize);
											final byte[] payloadByteStuffing = byteStuffing(readData);

											logger.debug("payload size: {}; parameter data: {}", ()->payloadSize, ()->ToHex.bytesToHex(rd));

											if (payloadByteStuffing.length>=payloadSize) {

												checksum.add(payloadByteStuffing);
												Payload payload = new Payload(parameterHeader, payloadByteStuffing);
												payloadsList.add(payload);

											}else
												logger.warn("readData.length>={}", payloadSize);
										}else{
											logger.warn("No data or wrong data size");
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

				sendAcknowlwdge(p);

				if(packet.getHeader()==null || packet.getPayloads()==null)
					packet = p;

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "Error");
		}

		Console.appendLn(packet, "Get");
		final long workTime = System.currentTimeMillis()-start;
		Console.appendLn(""+workTime, "Time");
		logger.debug("Time taken to send the packat: {}", workTime);

		logger.debug(packet);
		return packet;
	}

	private void sendAcknowlwdge(Packet p) throws IOException {

		byte[] acknowledge = getAcknowledge(p);
		outputStream.write(acknowledge);
	}

	public void clear() throws Exception {
		logger.traceEntry();

		buffer = null;

		wait(1, WAIT_TIME);
		while (available!=0) {

			logger.warn("{} bytes to clear", available);
			inputStream.read(new byte[available]);
			wait(1, WAIT_TIME*3);
		}
	}

	public Integer wait(int watedBytes, int waitTime){
		logger.trace("ENTRY: watedBytes: {}; available: {}; waitTime: {}; buffer.length: {}", ()->watedBytes, ()->available, ()->waitTime, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null));
		long start = System.currentTimeMillis();

		return Optional
				.ofNullable(buffer)
				.filter(b->b.length>=watedBytes)
				.map(b->b.length)
				.orElseGet(

						()->{

							try {

								while(opened){

									long wt = Optional.of((waitTime - System.currentTimeMillis()-start)/10).filter(time->time>1).orElse(1L) ;
									synchronized (this) { try { wait(wt); } catch (Exception e) { logger.catching(e); } }

									available = inputStream.available();

									if(available>=watedBytes || System.currentTimeMillis()-start>=waitTime)
										break;
								}

							} catch (IOException e) {
								logger.catching(e);
							}
							logger.trace("EXIT:available: {}; buffer.length: {}", ()->available, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null));
							return Optional.ofNullable(buffer).map(b->b.length+available).orElse(available);
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

		boolean isComfirm = false;

		final Optional<LinkHeader> oLinkHeader = Optional
				.ofNullable(packet)
				.filter(LinkedPacket.class::isInstance)
				.map(LinkedPacket.class::cast)
				.map(LinkedPacket::getLinkHeader)
				.filter(lh->lh.getAddr()!=0);	// 0 -> converter

		int size = oLinkHeader
				.map(LinkHeader::getAddr)
				.map(a->11)
				.orElse(7);


		final int available = wait(size, WAIT_TIME*size);
		if(available<size){
			logger.warn("requires {} bytes, but is {} bytes", size, available);
			return false;
		}

		byte[] readBytes = readBytes(size);

		logger.trace("\nneed {} bytes\n readBytes= {}", ()->size, ()->ToHex.bytesToHex(readBytes));

		isComfirm = readBytes!=null && readBytes[0]==PacketImp.FLAG_SEQUENCE && readBytes[readBytes.length-1]==PacketImp.FLAG_SEQUENCE;

		if(isComfirm){

			int index = size - 3;
			byte[] data = Arrays.copyOfRange(byteStuffing(readBytes), 1, index);

			LinkHeader lh = new LinkHeader(data);

			// (isLinked == null) -> converter
			final Boolean isLinked = oLinkHeader
					.map(lh::equals)
					.orElse(null);

			if(isLinked==null || isLinked){

				final short idReceived = getPacketId(isLinked!=null, data);
				final short idSent = packet.getHeader().getPacketId();

				if(idSent==idReceived){

					Checksum cs = new Checksum(data);
					byte[] b = cs.getChecksumAsBytes();

					if(b[0]==readBytes[index] && b[1]==readBytes[++index])
						isComfirm = true;
					else
						logger.warn("Checksum ERROR ({})", ToHex.bytesToHex(readBytes));
				}else
					logger.warn("Packet IDs are not equal (sent={}, received={}\nSent packet:{}", idSent, idReceived, packet);
			}else
				oLinkHeader
				.ifPresent(linkHeader->logger.warn("LinckHeaders are not equal (sent={}, received={}", linkHeader, lh));
		}

		return isComfirm;
	}

	private byte[] readBytes(int size) throws Exception {
		logger.trace( "ENTRY size: {}; available: {}, ()->buffer.length", ()->size, ()->available, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null));

		 final Optional<byte[]> oBuffer = Optional
				 							.ofNullable(buffer);

		 return oBuffer
				 .filter(bf->bf.length>=size)
				 .map(bf->{
					try {
						return getAPartFromTheBuffer(size);
					} catch (IOException e1) {
						logger.catching(e1);
					}
					return null;
				})
				 .orElseGet(()->{
					 try {

						 wait(size, Optional.of(WAIT_TIME*size).filter(t->t<1000).orElse(1000));

						 read();

						 if(buffer.length>=size)
							 return getAPartFromTheBuffer(size);

					 } catch (Exception e) {
						 logger.catching(e);
					 }

					 logger.warn("Have to read: {}; available: {};", size, available);
					 return null;
				 });
	}

	private void read() throws IOException {
		available = inputStream.available();
		if(available<=0)
			return;

		final byte[] bytes = new byte[available];
		inputStream.read(bytes);
		available = 0;

		buffer = Optional
				.ofNullable(buffer)
				.map(bf->{

					final int oldLength = bf.length;
					bf = Arrays.copyOf(bf, oldLength + bytes.length);
					System.arraycopy(bytes, 0, bf, oldLength, bytes.length);

					return bf;
				})
				.orElse(bytes);
	}

	private byte[] getAPartFromTheBuffer(int size) throws IOException {

		byte[] result;

		if(buffer==null || buffer.length<size)
			read();

		if(buffer==null || buffer.length<size)
			return null;

		if(buffer.length==size){
			 result = buffer;
			 buffer = null;

		}else{
			result = Arrays.copyOf(buffer, size);
			buffer = Arrays.copyOfRange(buffer, size, buffer.length);
		}

		byte[] r = result;
		result = Optional
				.of(result)
				.map(b->(int) IntStream.range(0, size).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count())
				.filter(s->s!=0)
				.map(s->{

					byte[] copyOf;

					try {

						byte[] tmp = getAPartFromTheBuffer(s);
						if(tmp==null || tmp.length<s)
								return null;

						copyOf = Arrays.copyOf(r, r.length + s);
						System.arraycopy(tmp, 0, copyOf, r.length, tmp.length);

					} catch (IOException e) {
						logger.catching(e);
						copyOf = r;
					}

					return copyOf;
				})
				.orElse(result);

		return result;
	}

	public boolean isFlagSequence() throws Exception {

		return readBytes(1)[0] == PacketImp.FLAG_SEQUENCE;
	}

	private short getPacketId(boolean isLinked, byte[] data) {
		return (short) PacketImp.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
	}

	private byte[] readLinkHeader() throws Exception {
		logger.traceEntry();

		if(wait(LinkHeader.SIZE, WAIT_TIME*LinkHeader.SIZE)<LinkHeader.SIZE)
			return null;

		final byte[] readBytes = readBytes(LinkHeader.SIZE);

		logger.debug("readBytes={}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readHeader() throws Exception {
		logger.traceEntry();

		if(wait(PacketHeader.SIZE, WAIT_TIME*PacketHeader.SIZE)<PacketHeader.SIZE)
			return null;

		final byte[] readBytes = readBytes(PacketHeader.SIZE);

		logger.debug("readBytes: {}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readParameterHeader() throws Exception {
		logger.traceEntry();

		final byte[] readBytes;

		if(wait(ParameterHeader.SIZE, WAIT_TIME*ParameterHeader.SIZE)<ParameterHeader.SIZE)
			readBytes = null;
		else
			readBytes = readBytes(ParameterHeader.SIZE);

		logger.debug("size: {}; readBytes: {};", ()->ParameterHeader.SIZE, ()->ToHex.bytesToHex(readBytes));

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
	public boolean openPort() throws Exception {
		logger.traceEntry();
		serialPort = (SerialPort) portIdentifier.open(MyComPort.class.getName(), 1000);
		setBaudrate(BAUDRATE_115200);
		serialPort.enableReceiveThreshold(1); 
		serialPort.enableReceiveThreshold(1000);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();
		return opened = true;
	}

	@Override
	public boolean closePort() {
		logger.traceEntry();

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

		serialPort.removeEventListener();
		if(serialPort!=null)
			serialPort.close();

		return opened = false;
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

	@Override
	public void close() throws Exception {
		closePort();
	}
}
