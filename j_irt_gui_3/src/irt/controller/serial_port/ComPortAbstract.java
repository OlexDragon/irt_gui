
package irt.controller.serial_port;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.Checksum;
import irt.data.PacketThread;
import irt.data.ThreadWorker;
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
import jssc.SerialPortException;

public abstract class ComPortAbstract implements SerialPortInterface {

	protected final Logger logger = LogManager.getLogger(getClass());

	private final int WAIT_TIME;
	private final int MAX_WAIT_TIME;

//    public static final int BAUDRATE_115200 = 115200;
//	private volatile int available;//The use to reduce the time ( inputStream.available() - takes a lot of time )
	private byte[] buffer;//The use to read all available bytes ( inputStream.read(byte[]) - takes a lot of time )

	/**
	 * This variable use to reduce the time to send acknowledgment 
	 */
	private byte[] confirmBytes;

	private boolean reading;

	public ComPortAbstract(int waitTime, int maxWaitTime) {
		WAIT_TIME = waitTime;
		MAX_WAIT_TIME = maxWaitTime;
	}

	@Override
	public Packet send(PacketWork packetWork){
//		logger.info(packetWork);
		Packet packet = packetWork.getPacketThread().getPacket();
		if(packet==null)
			return null;

		long start = System.currentTimeMillis();

		PacketHeader ph = packet.getHeader();
		byte groupId = ph.getGroupId();
		Packet readPacket;

		LinkHeader linkHeader;
		if(packet instanceof LinkedPacket){
			linkHeader = Optional.ofNullable(packet).filter(LinkedPacketImp.class::isInstance).map(LinkedPacketImp.class::cast).map(LinkedPacketImp::getLinkHeader).filter(lh->lh.getAddr()!=0).orElse(null);//
			readPacket = new LinkedPacketImp(linkHeader);
		}else{
			linkHeader = null;
			readPacket = new PacketImp();
		}

		byte[] readData;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {

	Checksum checksum = null;

	clear();

	byte[] data = packet.toBytes();

	if(data!=null && isOpened()){

		writeBytes(data);

		if (isConfirmBytes(packet)){

			if (isFlagSequence()) {

				if (linkHeader != null)
					if ((readData = readLinkHeader()) != null)
						checksum = new Checksum(byteStuffing(readData));

					else {
						logger.warn("\n\t linkHeader==null\n\t Sent packet {}", packet);
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
						readPacket.setHeader(packetHeader);
						payloadsList = new ArrayList<>();

						while (isOpened() && (readData = readParameterHeader()) != null) {

							if (containsFlagSequence(readData)) {
								cs = checksum.getChecksumAsBytes();

								final byte[] rd = byteStuffing(readData);

								if (cs[0] == rd[0] && cs[1] == rd[1]) {
									readPacket.setPayloads(payloadsList);
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
								logger.warn("parameterHeader.getCode()>300({}) || payloadSize>3000({}) || payloadSize<=0({}) \n{} \n{}\nSent Packet{}", parameterHeader.getCode(), payloadSize, payloadSize, packetHeader, parameterHeader, packet);
								break;
							}

							if(waitComPort(payloadSize)){

								readData = readFromBuffer(payloadSize);
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
				logger.warn("isFlagSequence returns false\nSent packet{}", packet);
		}else
			logger.warn("isConfirmBytes returns false\nSent Packet:{}", packet);
	}else 
		logger.warn("data!=null && isOpened() does not hold");

	sendAcknowledge(packet);

		} catch (InterruptedException e) {
			new ThreadWorker(()->closePort(), "ComPortAbstract-send-1");
		}catch (Exception e) {
			logger.catching(e);
			new ThreadWorker(()->closePort(), "ComPortAbstract-send-2");
		}

		synchronized (this) { buffer = null; }

		if(readPacket.getHeader()==null || readPacket.getPayloads()==null)
			readPacket = packet;

		final long workTime = System.currentTimeMillis()-start;
		logger.debug("Time taken to send the packet: {}", workTime);

		return readPacket;
	}

	private void sendAcknowledge(Packet p) throws IOException, SerialPortException {

		byte[] acknowledge = getAcknowledge(p);
//		writeBytes(acknowledge);
		logger.trace(()->ToHex.bytesToHex(acknowledge));
	}

	public void clear() {

		confirmBytes = null;

		 do{

			 synchronized (this) { buffer = null; }

			if(!reading)
				synchronized (this) { try { wait(100); } catch (InterruptedException e) { logger.catching(Level.DEBUG, e); } }

		 }while (reading || isBuffer());
	}

	public boolean waitComPort(int watedBytes){
		logger.debug("ENTRY: watedBytes: {}; WAIT_TIME: {}; buffer.length: {}", ()->watedBytes,  ()->WAIT_TIME, ()->Optional.ofNullable(buffer).map(b->b.length).orElse(null));
		long start = System.currentTimeMillis();

		// control maximum timeout
		final int wait = Optional.of(watedBytes*WAIT_TIME).filter(wt->wt<MAX_WAIT_TIME).orElse(MAX_WAIT_TIME);

		return getBuffer()
				.filter(b->b.length>=watedBytes)
				.filter(b->b.length>=watedBytes + (int) IntStream.range(0, watedBytes).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(b->true)
				.orElseGet(

						()->{

							int newSize;

							do{

								synchronized (ComPortAbstract.this) { try { ComPortAbstract.this.wait(wait); } catch (InterruptedException e) { logger.catching(Level.DEBUG, e); } }

								final Optional<byte[]> oBuffer = getBuffer();

								newSize = watedBytes + oBuffer.filter(b->b.length>=watedBytes).map(b->(int) IntStream.range(0, watedBytes-1).filter(index->b[index]==PacketImp.CONTROL_ESCAPE).count()).orElse(0);

							}while(getBuffer().map(b->b.length).orElse(0)<newSize && System.currentTimeMillis()-start < wait);

							final int ns = newSize;
							return getBuffer().map(b->b.length).filter(l->l>=ns).isPresent();

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

		final boolean waitComPort = waitComPort(size);
		if(waitComPort)
			confirmBytes = readFromBuffer(size);

		boolean isFlafDequence = confirmBytes!=null && confirmBytes[0]==PacketImp.FLAG_SEQUENCE && confirmBytes[confirmBytes.length-1]==PacketImp.FLAG_SEQUENCE;
//		logger.info("size: {}; isFlafDequence: {}; confirmBytes: {}; \n buffer: {}", size, isFlafDequence, confirmBytes, buffer);

		final boolean fs = isFlafDequence;
		logger.debug("\n need {} bytes\n readBytes= {}\n ConfirmBytes contain flag sequence: {}", ()->size, ()->ToHex.bytesToHex(confirmBytes), ()->fs);

		if(isFlafDequence){

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
		return isFlafDequence;
	}

	private byte[] readFromBuffer(final int size) {

		return Optional.ofNullable(buffer)
				.map(bf->bf.length)
				.filter(bLength->bLength >= size)
				.filter(bLength->bLength >= size + (int) IntStream.range(0, size).filter(index->buffer[index]==PacketImp.CONTROL_ESCAPE).count())
				.map(bLength->{
					try {
						return getAPartFromTheBuffer(size);
					} catch (IOException e1) {
						logger.catching(e1);
					}
					return null;
				})
				.orElseGet(()->{

					if(waitComPort(size))
						return readFromBuffer(size);

					return null;
				});
	}

	protected synchronized void writeToTheBuffer() throws IOException, SerialPortException {
		logger.traceEntry();

		reading = true;


//TODO			final int inputBufferBytesCount = getInputBufferBytesCount();

//			if(inputBufferBytesCount==0){
//				reading = false;
//				return;
//			}

//			byte[] bytes = readBytes(inputBufferBytesCount);
//			logger.debug("read bytes: {}; bytes: {}\n buffer: {}", ()->bytes.length, ()->ToHex.bytesToHex(bytes), ()->ToHex.bytesToHex(buffer));

//			buffer =Optional
//					.ofNullable(buffer)
//					.filter(b->b.length>0)
//					.map(bf->{
//
//						final int oldLength = bf.length;
//						bf = Arrays.copyOf(bf, oldLength + bytes.length);
//						System.arraycopy(bytes, 0, bf, oldLength, bytes.length);
//
//						return bf;
//					})
//					.orElse(bytes);

			reading = false;
			notifyAll();
//		}
	}

	private synchronized byte[] getAPartFromTheBuffer(final int size) throws IOException {
		logger.trace("ENTRY size: {}; buffer: {}", ()->size, ()->ToHex.bytesToHex(buffer));

		byte[] result;

		final Optional<byte[]> oBuffer = Optional.ofNullable(buffer);
		final int newSize = size + oBuffer.map(b->(int) IntStream.range(0, size).filter(index->buffer[index]==PacketImp.CONTROL_ESCAPE).count()).orElse(0);

		logger.debug("Size to get: {}", newSize);

		if(buffer==null || buffer.length<newSize)
			return null;
			
			if(buffer.length==newSize){
				 result = buffer;
				 buffer = null;

			}else{
				result = Arrays.copyOf(buffer, newSize);
				buffer = Arrays.copyOfRange(buffer, newSize, buffer.length);
			}

		return result;
	}

	public boolean isFlagSequence() throws Exception {
		logger.traceEntry();

		byte[] readBytes = readFromBuffer(1);
		if(readBytes==null){
			waitComPort(10);//TODO - test with smaller value
			readBytes = readFromBuffer(1);
		}

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

		final byte[] readBytes = readFromBuffer(LinkHeader.SIZE);

		logger.debug("Exit readBytes={}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readHeader() throws Exception {

		if(!waitComPort(PacketHeader.SIZE))
			return null;

		final byte[] readBytes = readFromBuffer(PacketHeader.SIZE);

		logger.debug("EXIT readBytes: {}", ()->ToHex.bytesToHex(readBytes));

		return readBytes;
	}

	private byte[] readParameterHeader() throws Exception {

		if(!waitComPort(ParameterHeader.SIZE))
			return null;

		byte[] readBytes = readFromBuffer(ParameterHeader.SIZE);

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

	public synchronized Optional<byte[]> getBuffer() {
		return Optional.ofNullable(buffer);
	}

	private synchronized boolean isBuffer() {
		return buffer!=null && buffer.length!=0;
	}

	@Override
	public String toString() {
		return getPortName();
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
}
