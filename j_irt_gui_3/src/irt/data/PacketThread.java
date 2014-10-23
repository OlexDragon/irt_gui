package irt.data;

import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.data.value.Value;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


public class PacketThread extends Thread {

	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final byte FLAG_SEQUENCE	= 0x7E;
	public static final byte CONTROL_ESCAPE= 0x7D;

	protected byte[] data;
	private Packet packet;
	private Object value;

	public PacketThread(byte[] packetSetting, String threadName) {
		super(threadName);
		data = packetSetting;
		setDaemon(true);
	}

	public PacketThread(byte[] packetSetting) {
		data = packetSetting;
	}

	@Override
	public void run() {
		logger.trace("Start with data={}", Arrays.toString(data));
		if (packet == null && data != null) {
			try {
				packet = newPacket();
				synchronized (data) {
					packet.set(data);
					if (value != null) {
						Payload pl = packet.getPayload(0);
						pl.setBuffer(value);
					}
					logger.trace("packet={}", packet);

					data = preparePacket(packet);
					logger.trace("result={}", Arrays.toString(data));
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}
	}

	protected Packet newPacket() {
		logger.trace("newPacket() = new Packet()");
		return new Packet();
	}

	public byte[] getData() {
		return data!=null ? Arrays.copyOf(data, data.length) : null;
	}

	private byte[] preparePacket(Packet packet) {
		byte[]data = packet.asBytes();
		return preparePacket(data);
	}

	public static byte[] preparePacket(byte[] data) {
		if(data!=null){
			byte[] p = new byte[data.length*3];
			int index = 0;
			p[index++] = FLAG_SEQUENCE;
			for(int i=0; i< data.length; i++, index ++){
				index = checkControlEscape(data, i, p, index);
			}

			byte[] csTmp = Packet.toBytes((short)new Checksum(data).getChecksum());
			for(int i=1; i>=0; i--, index ++)
				index = checkControlEscape(csTmp, i, p, index);

			p[index++] = FLAG_SEQUENCE;

			data = new byte[index];
			System.arraycopy(p, 0, data, 0, index);
		}
		return data;
	}

	public void preparePacket(byte value) {
		logger.entry(value);
		setPacketHeaderType(Packet.IRT_SLCP_PACKET_TYPE_COMMAND);
		Payload payload = packet.getPayload(0);
		payload.setBuffer(value);
		data = preparePacket(packet.asBytes());
		logger.exit();
	}

	public static int checkControlEscape(byte[] surce, int surceIndex, byte[] destination, int destinationIndex) {
		if(surce[surceIndex]==FLAG_SEQUENCE || surce[surceIndex]==CONTROL_ESCAPE){
			destination[destinationIndex++] = CONTROL_ESCAPE;
			destination[destinationIndex]	= (byte) (surce[surceIndex] ^ 0x20);
		}else
			destination[destinationIndex] = (byte) surce[surceIndex];
		return destinationIndex;
	}

	public Packet getPacket() {
		return packet;
	}

	@Override
	public synchronized void start() {
		if(packet==null)
			super.start();
	}

	@Override
	public String toString() {
		return "PacketThread [data=" + ToHex.bytesToHex(data) + ", packet="
				+ packet + "]";
	}


	public void preparePacket() {
		data = preparePacket(packet);
	}

	public void setPacketHeaderType(byte packetType) {
		PacketHeader header = packet.getHeader();
		if(header.getPacketType()!=packetType){
			header.setType(packetType);
		}
	}

	public void preparePacket(byte irtSlcpParameter, Object value) {
		Payload pl = packet.getPayload(irtSlcpParameter);

		if(pl!=null){
			pl.setBuffer(value);

			if(value!=null)
				setPacketHeaderType(Packet.IRT_SLCP_PACKET_TYPE_COMMAND);
			else
				setPacketHeaderType(Packet.IRT_SLCP_PACKET_TYPE_REQUEST);

			preparePacket();
		}
	}

	public void preparePacket(byte irtSlcpParameter, RegisterValue registerValue) {

		Payload pl = packet.getPayload(irtSlcpParameter);
		Value rv = registerValue.getValue();

		if(rv!=null){
			pl.setBuffer(registerValue.getIndex(), registerValue.getAddr(), (int)rv.getValue());
			setPacketHeaderType(Packet.IRT_SLCP_PACKET_TYPE_COMMAND);
		}else{
			pl.setBuffer(registerValue.getIndex(), registerValue.getAddr());
			setPacketHeaderType(Packet.IRT_SLCP_PACKET_TYPE_REQUEST);
		}

		preparePacket();
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setType(byte packetType) {
		packet.getHeader().setType(packetType);
	}

	@Override
	public boolean equals(Object obj) {
		return obj!= null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	public LinkHeader getLinkHeader() {
		return null;
	}

	public void clear(){
		packet = null;
		data = null;
		value = null;
	}

	public boolean isReadyToSend() {
		return packet!=null && packet.getHeader().asBytes()!=null && data!=null;
	}

	public void setDataPacketTypeCommand() {
		if(data!=null)
			data[0] = Packet.IRT_SLCP_PACKET_TYPE_COMMAND;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
