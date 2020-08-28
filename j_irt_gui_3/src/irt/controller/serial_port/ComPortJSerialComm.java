package irt.controller.serial_port;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

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
import irt.tools.panel.head.Console;

public class ComPortJSerialComm implements SerialPortInterface {
	private final Logger logger = LogManager.getLogger();
	private final Marker marker = MarkerManager.getMarker("FileWork");

	public static final int MAX_WAIT_TIME_IN_MINUTES = 1;

	private final SerialPort serialPort;
	private byte[] buffer;

	private short packetId;

	private LinkHeader linkHeader;
	private boolean isComfirm;

	public ComPortJSerialComm(String portName) {
		serialPort = SerialPort.getCommPort(portName);
	}

	@Override
	public boolean isOpened() {
		return serialPort.isOpen();
	}

	@Override
	public Packet send(PacketWork packetWork) {
		logger.traceEntry("{}", packetWork);

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

			clear();

			byte[] data = packet.toBytes();
			String hexStr = ToHex.bytesToHex(data);

			String prefix = (runTimes+1)+") send";

			logger.info(marker, ">> {}: {}", prefix, packet);
			logger.info(marker, ">> {}: {}", prefix, hexStr);
			Console.appendLn(packet, prefix);
			Console.appendLn(hexStr, prefix);

			if(data!=null && isOpened()){
				serialPort.writeBytes(data, data.length);
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

			Optional.ofNullable(getAcknowledge()).filter(a->isOpened()).ifPresent(acknowledge->{ serialPort.writeBytes(acknowledge, acknowledge.length);});

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

	private boolean containsFlagSequence(byte[] readBytes) {
		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==PacketImp.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		return isFlagSequence;
	}

	private byte[] readParameterHeader() throws Exception {
		return readBytes(ParameterHeader.SIZE);
	}

	private byte[] readHeader() throws Exception {
		return readBytes(PacketHeader.SIZE);
	}

	private byte[] readLinkHeader() throws Exception {
		return readBytes(LinkHeader.SIZE);
	}

	private boolean isFlagSequence() throws Exception {

		byte[] readBytes = readBytes(1);
		return readBytes!=null && readBytes[0] == PacketImp.FLAG_SEQUENCE;
	}

	private boolean isConfirmBytes() throws Exception {

		boolean isComfirm = false;
		int ev = Optional.ofNullable(linkHeader).map(LinkHeader::getIntAddr).filter(addr->addr>0).map(a->11).orElse(7);
		int index = ev - 3;

		byte[] readBytes = readBytes(ev);

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

	private void clear() {
		buffer = null;
	}

	@Override
	public String getPortName() {
		return serialPort.getSystemPortName();
	}

	@Override
	public boolean openPort() throws Exception {

		final boolean openPort = serialPort.openPort();

		if(openPort)
			serialPort.addDataListener(
					new SerialPortDataListener() {

						@Override public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

						@Override
						public void serialEvent(SerialPortEvent event) {
							logger.traceEntry("{}", event);

							ThreadWorker.runThread(
									()->{
										if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
											return;

										byte[] b = new byte[serialPort.bytesAvailable()];
										serialPort.readBytes(b, b.length);

										synchronized (serialPort) {

											if(buffer==null)
												buffer = b;

											else {
												final byte[] copyOfBuffer = Arrays.copyOf(buffer, buffer.length + b.length);
												System.arraycopy(b, 0, copyOfBuffer, buffer.length, b.length);
												buffer = copyOfBuffer;
											}
										}

										synchronized(ComPortJSerialComm.this){
											ComPortJSerialComm.this.notify();
											logger.debug("notify(); buffer: {}", buffer);
										}
									}, "Serial Port Listener");

						}});
		return openPort;
	}

	@Override
	public boolean closePort() {
		return serialPort.closePort();
	}

	@Override
	public void setBaudrate(int baudrate) {
		serialPort.setBaudRate(baudrate);
	}

	@Override
	public int getBaudrate() {
		return serialPort.getBaudRate();
	}

	@Override
	public byte[] readBytes(int size) throws Exception {
		logger.traceEntry("size: {};  buffer: {}", size, buffer);

		if(size<=0)
			size = Integer.MAX_VALUE;

		final long start = System.currentTimeMillis();
		long elapsed = 0;
		final int waitTime = (int) TimeUnit.MINUTES.toMillis(MAX_WAIT_TIME_IN_MINUTES);

		synchronized (this) {
			while((buffer==null || buffer.length<size) && (elapsed=(System.currentTimeMillis()-start))<waitTime) 
				wait(waitTime - elapsed);
		}

//		logger.error("waitTime: {} milles; elapsed time = {} milles; buffer: {};", waitTime, elapsed, buffer);

		if(buffer==null || buffer.length<size) {
			logger.warn("No answer or it is to short. buffer: {}", buffer);
			return null;
		}

		byte[] result;

		synchronized (serialPort) {
			result = Arrays.copyOf(buffer, size);
			buffer = Arrays.copyOfRange(buffer, size, buffer.length);
		}


		return result;
	}

}
