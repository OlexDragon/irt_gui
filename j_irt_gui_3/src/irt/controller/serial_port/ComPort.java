package irt.controller.serial_port;

import irt.data.Checksum;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.tools.panel.head.Console;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Timer;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class ComPort extends SerialPort {

	private boolean run = true;
	private int timeout = 3000;
	private int timesTimeout;
	private Timer timer;
	private SerialPortEvent serialPortEvent = new SerialPortEvent();
	private LinkHeader linkHeader;
	private short packetId;
	private boolean isSerialPortEven;
	private boolean isComfirm;
	private long start;

	public ComPort(String portName) {
		super(portName);

		timer = new Timer(timeout, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					Console.appendLn("Timeout", "Timer");
					synchronized (ComPort.this) {
						closePort();
					}
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
			}
		});
		timer.setRepeats(false);
	}

	public Packet send(PacketWork packetWork){

		start = System.currentTimeMillis();
		PacketThread pt = packetWork.getPacketThread();
		Packet p = pt.getPacket();
		PacketHeader ph = p.getHeader();
		byte groupId = ph.getGroupId();
		packetId = ph.getPacketId();
		Packet packet;

		if(p instanceof LinkedPacket){
			linkHeader = ((LinkedPacket)p).getLinkHeader();
			packet = new LinkedPacket(linkHeader);
		}else{
			linkHeader = null;
			packet = new Packet();
		}
//		System.out.println("Send bytes: "+Arrays.toString(packetThread.getData()));


		int runTimes = 0;
		byte[] readData;
		int ev;
		byte[] cs;
		PacketHeader packetHeader;
		List<Payload> payloadsList;
		ParameterHeader parameterHeader;
		try {

if(!isRun())
	setRun(true);

do{

	Checksum checksum = null;

	synchronized (this) {
		if(!isOpened()){
			openPort();
			setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		}
	}
//	Console.appendLn(""+(System.currentTimeMillis()-start), "openPort");

			timer.restart();
			clear();

			byte[] data = pt.getData();

			String prefix = (runTimes+1)+") send";

//			Console.appendLn(""+(System.currentTimeMillis()-start), "Clear Time");
			Console.appendLn(p, prefix);
			Console.appendLn(Arrays.toString(data), prefix);

			if(data!=null){
				writeBytes(data);
//				Console.appendLn(""+(System.currentTimeMillis()-start), "writeBytes Time");

				if ((isConfirmBytes()) && isFlagSequence()){
//					Console.appendLn(""+(System.currentTimeMillis()-start), "get answer Time");

					if(linkHeader!=null)
						if((readData=readLinkHeader())!=null)
							checksum = new Checksum(readData);
						else{
							Console.append("LinkHeader", "Break");
							break;
						}

					if((readData=readHeader())!=null) {
//					System.out.println("??? 1 readHeader 1 ???");
						if(checksum!=null)
							checksum.add(readData);
						else
							checksum = new Checksum(readData);

						packetHeader = new PacketHeader(readData);
//					System.out.println("packetHeader - "+packetHeader);

						if (packetHeader.asBytes() != null && packetHeader.getGroupId()==groupId) {
							packet.setHeader(packetHeader);
							payloadsList = new ArrayList<>();

							while ((readData = readParameterHeader())!=null && isRun()) {

//							System.out.println("??? 2 readHeader 2 ??? "+Arrays.toString(readData));
								if (containsFlagSequence(readData)) {
									cs = checksum.getChecksumAsBytes();

//									System.out.println("Checksum - "+Arrays.toString(cs));
									if (cs[0] == readData[0] && cs[1] == readData[1]){
										timesTimeout = 0;
										packet.setPayloads(payloadsList);
									}

									break;
								}
//								System.out.println("??? - add(readData) - ???");
								checksum.add(readData);
								parameterHeader = new ParameterHeader(readData);

//								System.out.println("??? - parameterHeader - ??? "+parameterHeader);
								if(parameterHeader.getCode()>30 || (ev = parameterHeader.getSize())>2000){
									Console.appendLn("ParameterHeader Sizes", "Break ");
									break;
								}
								if (ev >= 0 && (readData = readBytes(ev))!=null) {

									checksum.add(readData);
									Payload payload = new Payload(parameterHeader,	readData);
//									System.out.println("Payload - "+payload);
									payloadsList.add(payload);
								}else{
									Console.appendLn("Payload", "Break ");
									break;
								}
							}					
						}
					}
				}
				byte[] acknowledge = getAcknowledge();
//				Console.appendLn(Arrays.toString(acknowledge), "sent Acknowledge");
				writeBytes(acknowledge);
			}else
				setRun(false);
}while(isComfirm && packet.getPayloads()==null && ++runTimes<3 && isRun());//if error repeat up to 3 times

			if(packet.getHeader()==null || packet.getPayloads()==null && isRun())
					packet = p;

		} catch (SerialPortException e) {
			if(timesTimeout<3){
				timesTimeout++;
				setRun(false);
			}
			Console.appendLn(e.getLocalizedMessage(), "Error");
//			System.out.println(e.getLocalizedMessage());
//			JOptionPane.showMessageDialog(null, e.getLocalizedMessage()+";(E003)");
		}

		timer.stop();

		Console.appendLn(packet, "Get");
//		System.out.println("Reseve:"+p);
		Console.appendLn(""+(System.currentTimeMillis()-start), "Time");

		return packet;
	}

	private byte[] getAcknowledge() {
		byte[] b;

		if(linkHeader!=null)
			b = Arrays.copyOf(linkHeader.asBytes(), 7);
		else
			b = new byte[3];

		int idPosition = b.length-3;
		b[idPosition] = (byte) 0xFF;

		byte[] packetId = Packet.toBytes(this.packetId);
		System.arraycopy(packetId, 0, b, ++idPosition, 2);

		return PacketThread.preparePacket(b);
	}

	private byte[] readLinkHeader() throws SerialPortException {
//		System.out.println("??? - readLinkHeader - ???");

		return readBytes(LinkHeader.SIZE);
	}

	private byte[] readHeader() throws SerialPortException {
//		System.out.println("??? - readHeader - ???");
		return readBytes(PacketHeader.SIZE);
	}

	private byte[] readParameterHeader() throws SerialPortException {
//		System.out.println("??? - readParameterHeader - ???");
		return readBytes(ParameterHeader.SIZE);
	}

	public boolean isFlagSequence() throws SerialPortException {
//		System.out.println("??? - readFlagSequence - ???");

		byte[] readBytes = readByte(2500);
		boolean isFlagSequence = readBytes!=null && readBytes[0] == Packet.FLAG_SEQUENCE;

		return isFlagSequence;
	}

	private boolean containsFlagSequence(byte[] readBytes) {
		boolean isFlagSequence = false;
		for(byte b:readBytes)
			if(b==Packet.FLAG_SEQUENCE){
				isFlagSequence = true;
				break;
			}

		return isFlagSequence;
	}

	private boolean isConfirmBytes() throws SerialPortException {

		boolean isComfirm = false;
		int ev = linkHeader!=null ? 11 : 7;
		int index = ev - 3;

		byte[] readBytes = readBytes(ev,100);
		this.isComfirm = readBytes!=null && readBytes[0]==Packet.FLAG_SEQUENCE && readBytes[readBytes.length-1]==Packet.FLAG_SEQUENCE;

		if(!this.isComfirm && readBytes!=null && linkHeader!=null && readBytes[6]==Packet.FLAG_SEQUENCE)
					linkHeader = null;

//		System.out.println(" - isConfirmBytes - "+Arrays.toString(readBytes)+" : "+linkHeader);

		if(this.isComfirm){
			byte[] data = Arrays.copyOfRange(readBytes, 1, index);
			if((linkHeader==null || new LinkHeader(data).equals(linkHeader)) && packetId==getPacketId(linkHeader!=null, data)){
				Checksum cs = new Checksum(data);
				byte[] b = cs.getChecksumAsBytes();
				if(b[0]==readBytes[index] && b[1]==readBytes[++index])
					isComfirm = true;
			}
		}

//		Console.appendLn(""+(System.currentTimeMillis()-start), "Acknowledge Time");
		return isComfirm;
	}

	private short getPacketId(boolean isLinked, byte[] data) {
		return (short) Packet.shiftAndAdd(isLinked ? Arrays.copyOfRange(data, LinkHeader.SIZE+1, LinkHeader.SIZE+3) : Arrays.copyOfRange(data, 1, 3));
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
	protected void finalize() throws Throwable {
		closePort();
	}

	public byte[] clear() throws SerialPortException {
		int waitTime = 20;
		byte[] readBytes = null;
		while(wait(1, waitTime)){
			readBytes = super.readBytes(getInputBufferBytesCount());
			Console.appendLn(Arrays.toString(readBytes), "Clear");
			if(waitTime!=100)
				waitTime = 100;
		}
		return readBytes;
	}

	public byte[] readByte(int timeout) throws SerialPortException {
//		System.out.println("??? - readByte - ???");
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

			Console.appendLn(Arrays.toString(tmpBytes), "Read");

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

//		System.out.println("Read - "+Arrays.toString(readBytes));

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
			} catch (InterruptedException e) {}

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
				if(b==Packet.CONTROL_ESCAPE)
					escCount++;

		return escCount;
	}

	private byte[] byteStuffing(byte[] readBytes) {

		int index = 0;
		if(readBytes!=null){

			for(int i=0; i<readBytes.length; i++)

				if(readBytes[i]==Packet.CONTROL_ESCAPE){
					if(++i<readBytes.length)
						readBytes[index++] = (byte) (readBytes[i]^0x20);
				}else
					readBytes[index++] = readBytes[i];
		}

		return readBytes==null ? null : index==readBytes.length ? readBytes : Arrays.copyOf(readBytes, index);
	}

	public void setRun(boolean run) {
		synchronized (this) {
			this.run = run;
			notify();
		}
	}

	public synchronized boolean isRun() {
		return run;
	}

	public boolean wait(int eventValue, int waitTime) throws SerialPortException {
		boolean isReady = false;
		long start = System.currentTimeMillis();

		while(isOpened() && !(isReady = getInputBufferBytesCount()>=eventValue) && (System.currentTimeMillis()-start)<waitTime && isRun()){
			synchronized (this) {

				try { wait(waitTime); } catch (InterruptedException e) {}

				if(isSerialPortEven)
					isSerialPortEven = false;
			}
			Console.appendLn("waitTime:"+waitTime+"; isSerialPortEven="+isSerialPortEven, "wait:"+(System.currentTimeMillis()-start));
		};
//		Console.append("isOpend:"+isOpened()+"; isReady:"+isReady+"; t:"+t+"; eventValue:"+eventValue+"; isRun():"+isRun(), "Wait");
		if(isSerialPortEven)
			isSerialPortEven = false;
		return isReady;
	}

	@Override
	public boolean openPort() throws SerialPortException {
		boolean isOpen = super.openPort();
		if(isOpen)
			addEventListener(serialPortEvent);
		return isOpen;
	}

	@Override
	public boolean closePort() throws SerialPortException {

		boolean isClose = false;
		if(isOpened()){
			removeEventListener();
			isClose = super.closePort();
		}else
			isClose = true;

		return isClose;
	}

	//*** Class SerialPortEvent *****************************************************
	private class SerialPortEvent implements SerialPortEventListener{

		@Override
		public void serialEvent(jssc.SerialPortEvent serialPortEvent) {

			synchronized (ComPort.this) {
				isSerialPortEven = true;
				ComPort.this.notify();
				Console.appendLn("", "notify");
			}
		}
		
	}
}
