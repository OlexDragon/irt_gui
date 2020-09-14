package irt.controller.serial_port;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

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
import irt.tools.panel.head.Console;

public class SerialPortWorker {
	private final static Logger logger = LogManager.getLogger();

	private static short packetId;
	private static LinkHeader linkHeader;
	private static Marker marker = MarkerManager.getMarker("FileWork");
	private static SerialPortInterface serialPort;
	private static boolean isComfirm;

	private static int maxSize = 3;

	public synchronized static Packet send(SerialPortInterface serialPort, Packet packet) {

		SerialPortWorker.serialPort = serialPort;
		maxSize = packet.getMaxSize();

		long start = System.currentTimeMillis();

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

		int runTimes = 0;
		byte[] readData;
		int ev;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {



	Checksum checksum = null;

	serialPort.clear();

			byte[] data = packet.toBytes();
			String hexStr = ToHex.bytesToHex(data);

			String prefix = (runTimes+1)+") send";

			logger.info(marker, ">> {}: {}", prefix, packet);
			logger.info(marker, ">> {}: {}", prefix, hexStr);
			Console.appendLn(packet, prefix);
			Console.appendLn(hexStr, prefix);

			if(data!=null && serialPort.isOpened()){
				logger.debug("writeBytes: {}", ()->Arrays.toString(data));
				serialPort.writeBytes(data);

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
								if(parameterHeader.getCode()>300 || ev>maxSize){
									Console.appendLn("ParameterHeader Sizes", "Break ");
									logger.error(
											"parameterHeader.getCode()>300({}) || ev>{}({}) \n{} \n{} \n readData: {}",
											parameterHeader.getCode(),
											maxSize,
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
				logger.warn("the condition data!=null && isOpened({}) does not hold", serialPort.isOpened());
			}

			Optional.ofNullable(getAcknowledge()).filter(a->serialPort.isOpened())
			.ifPresent(
					acknowledge->{ try {
						serialPort.writeBytes(acknowledge);
					} catch (Exception e) {
						logger.catching(e);
					}});

			if(readPacket.getHeader()==null || readPacket.getPayloads()==null)
				readPacket = packet;

		} catch (InterruptedException e) {
			logger.catching(Level.DEBUG, e);
		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "Error");
		}

		Console.appendLn(readPacket, "Get");
		Console.appendLn(""+(System.currentTimeMillis()-start), "Time");

		return readPacket;
	}

	private static byte[] readBytes(int byteCount) throws Exception {

		byte[] readBytes = serialPort.getFromBuffer(byteCount);
		final int escapes = escapeCount(readBytes);

		if(escapes==0)
			return readBytes;

		final byte[] tmp = serialPort.getFromBuffer(escapes);

		if(tmp==null)
			return SerialPortWorker.byteStuffing(readBytes);

		final int newLength = byteCount + escapes;
		readBytes = Arrays.copyOf(readBytes, newLength);
		System.arraycopy(tmp, 0, readBytes, byteCount, escapes);

		Console.appendLn(ToHex.bytesToHex(readBytes), "Read");

		return byteStuffing(readBytes);
	}

	private static byte[] getAcknowledge() {
		byte[] b;

		if(linkHeader!=null)
			b = Arrays.copyOf(linkHeader.toBytes(), 7);
		else
			b = new byte[3];

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;

		byte[] packetId = PacketImp.toBytes(SerialPortWorker.packetId);
		System.arraycopy(packetId, 0, b, ++idPosition, 2);

		final byte[] preparePacket = PacketThread.preparePacket(b);
		logger.debug("{}", preparePacket);

		return preparePacket;
	}

	private static boolean containsFlagSequence(byte[] readBytes) {
		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==PacketImp.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		return isFlagSequence;
	}

	private static byte[] readParameterHeader() throws Exception {
		return readBytes(ParameterHeader.SIZE);
	}

	private static byte[] readHeader() throws Exception {
		return readBytes(PacketHeader.SIZE);
	}

	private static byte[] readLinkHeader() throws Exception {
		return readBytes(LinkHeader.SIZE);
	}

	private static boolean isFlagSequence() throws Exception {

		byte[] readBytes = readBytes(1);
		return readBytes!=null && readBytes[0] == PacketImp.FLAG_SEQUENCE;
	}

	private static boolean isConfirmBytes() throws Exception {

		boolean isComfirm = false;
		int ev = Optional.ofNullable(linkHeader).map(LinkHeader::getIntAddr).filter(addr->addr>0).map(a->11).orElse(7);
		int index = ev - 3;

		byte[] readBytes = readBytes(ev);

		logger.debug("readBytes= {}", readBytes);

		SerialPortWorker.isComfirm = readBytes!=null && readBytes[0]==PacketImp.FLAG_SEQUENCE && readBytes[readBytes.length-1]==PacketImp.FLAG_SEQUENCE;

		//for converters
		if(!SerialPortWorker.isComfirm && readBytes!=null && linkHeader!=null && readBytes[6]==PacketImp.FLAG_SEQUENCE)
					linkHeader = null;

		if(SerialPortWorker.isComfirm){

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

	private static short getPacketId(boolean isLinked, byte[] data) {
		return (short) PacketImp.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
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

	private static int escapeCount(final byte[] readBytes) {

		if(readBytes==null)
			return 0;

		return (int) IntStream.range(0, readBytes.length).filter(i->readBytes[i]==PacketImp.CONTROL_ESCAPE).count();
	}
}
