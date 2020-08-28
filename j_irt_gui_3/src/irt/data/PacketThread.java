package irt.data;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper.Priority;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.value.Value;


public class PacketThread extends Thread implements PacketThreadWorker {

	protected final Logger logger = LogManager.getLogger();

	public static final byte FLAG_SEQUENCE	= 0x7E;

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
		logger.trace("\n\tStart with data={}", Arrays.toString(data));
		if (packet == null && data != null) {
			try {
				packet = newPacket();
				synchronized (logger) {
					packet.set(data);
					if (value != null) {
						Payload pl = packet.getPayload(0);
						pl.setBuffer(value);
					}

					data = preparePacket(packet);

					logger.trace("\n\t{}", data);
				}
			} catch (Exception ex) {
				logger.catching(ex);
			}
		}else
			logger.warn("\n\t!!!\tNo Change\t!!!");
	}

	protected Packet newPacket() {
		logger.trace("newPacket() = new Packet()");
		return new PacketImp();
	}

	public byte[] getData() {
		return data!=null ? Arrays.copyOf(data, data.length) : null;
	}

	private byte[] preparePacket(Packet packet) {

		byte[]data = packet.toBytes();
		logger.debug("\n\t{}", data);
		return preparePacket(data);
	}

	public static byte[] preparePacket(final byte[] data) {

		if(data==null)
			return null;


		byte[] csTmp = PacketImp.toBytes((short)new Checksum(data).getChecksum());

		byte[] result = Optional
				.ofNullable(data)
				.map(d->countFlags(data) + countFlags(csTmp))
				.map(count->new byte[data.length + count + 4])
				.orElse(new byte[data.length + 4]);


			result[0] = result[result.length-1] = FLAG_SEQUENCE;

			int index = 1;
			for(int i=0; i< data.length; i++, index ++){
				index = checkControlEscape(data, i, result, index);
			}
			for(int i=1; i>=0; i--, index ++)
				index = checkControlEscape(csTmp, i, result, index);

		return result;
	}

	private static int countFlags(final byte[] data) {
		return (int) IntStream.range(0, data.length).filter(index->data[index]==PacketImp.CONTROL_ESCAPE || data[index]==PacketImp.FLAG_SEQUENCE).count();
	}

	public void preparePacket(byte value) {

		setPacketHeaderType(PacketImp.PACKET_TYPE_COMMAND);
		Payload payload = packet.getPayload(0);
		payload.setBuffer(value);
		data = preparePacket(packet.toBytes());
	}

	public static int checkControlEscape(byte[] surce, int surceIndex, byte[] destination, int destinationIndex) {
		if(surce[surceIndex]==FLAG_SEQUENCE || surce[surceIndex]==PacketImp.CONTROL_ESCAPE){
			destination[destinationIndex++] = PacketImp.CONTROL_ESCAPE;
			destination[destinationIndex]	= (byte) (surce[surceIndex] ^ 0x20);
		}else
			destination[destinationIndex] = (byte) surce[surceIndex];
		return destinationIndex;
	}

	public Packet getPacket() {
		try {
			join(10);
		} catch (InterruptedException e) {
			logger.catching(e);
		}
		return packet;
	}

	@Override
	public void start() {
		if(getState()==State.NEW && packet==null)
			super.start();
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
				setPacketHeaderType(PacketImp.PACKET_TYPE_COMMAND);
			else
				setPacketHeaderType(PacketImp.PACKET_TYPE_REQUEST);

			preparePacket();
		}
	}

	public void preparePacket(byte irtSlcpParameter, RegisterValue registerValue) {

		Payload pl = packet.getPayload(irtSlcpParameter);
		Value rv = registerValue.getValue();

		if(rv!=null){
			pl.setBuffer(registerValue.getIndex(), registerValue.getAddr(), (int)rv.getValue());
			setPacketHeaderType(PacketImp.PACKET_TYPE_COMMAND);
		}else{
			pl.setBuffer(registerValue.getIndex(), registerValue.getAddr());
			setPacketHeaderType(PacketImp.PACKET_TYPE_REQUEST);
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
		return packet!=null && packet.getHeader().toBytes()!=null && data!=null;
	}

	public void setDataPacketTypeCommand() {
		if(data!=null)
			data[0] = PacketImp.PACKET_TYPE_COMMAND;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "PacketThread [data=" + ToHex.bytesToHex(data) + ", packet=" + packet + "]";
	}

	@Override
	public void setPriority(Priority priority) {
		// TODO Auto-generated method stub
		
	}
}
