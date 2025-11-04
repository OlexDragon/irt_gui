package irt.gui.web.beans;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Packet {
	private final static Logger logger = LogManager.getLogger();

	public static final int PACKET_TYPE_POSITION = 4;
	public static final int PACKET_TYPE_FCM_POSITION = 0;	// without FLAG_SEQUENCE
	public static final int ACKNOWLEDGEMENT_LENGTH = 7;	
	public static final int ACKNOWLEDGEMENT_FCM_LENGTH = 3;
	public static final byte FLAG_SEQUENCE	= 0x7E;
	public static final byte CONTROL_ESCAPE = 0x7D;;

	public static final int LINK_HEADER_SIZE = 4;
	public static final int PACKET_HEADER_SIZE = 7;
	public static final int PAYLOAD_HEADER_SIZE = 3;

	private final int typeIndex;
	private final int acknowLength;
	private final byte[] bytes;

	@Getter private int lastIndex;

	public Packet(Byte unitAddress, PacketType packetType, short packetId, PacketGroupId groupId, byte parameterId, byte[] value){

		final Optional<Byte> oAddress = Optional.ofNullable(unitAddress).filter(a->a!=0);
		int linkHeaderSize = oAddress.map(v->LINK_HEADER_SIZE).orElse(0);
		final Integer valueSize = value!=null ? value.length : 0;

		if (valueSize > 0xFFFF)
			throw new IllegalArgumentException("Value size is too big: " + valueSize);

		final int length = linkHeaderSize + PACKET_HEADER_SIZE + PAYLOAD_HEADER_SIZE + valueSize + 2; // +2 for checksum
		acknowLength = oAddress.map(v->ACKNOWLEDGEMENT_LENGTH).orElse(ACKNOWLEDGEMENT_FCM_LENGTH); 	// without FLAG_SEQUENCE;
		typeIndex = oAddress.map(v->PACKET_TYPE_POSITION).orElse(PACKET_TYPE_FCM_POSITION); // without FLAG_SEQUENCE
		bytes = new byte[length];

		oAddress.ifPresent(a->bytes[0] = a);
		int typePos = oAddress.map(v->PACKET_TYPE_POSITION).orElse(PACKET_TYPE_FCM_POSITION);
		bytes[typePos] = packetType.getCode();
		byte[] bsPacketId = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(packetId).array();
		bytes[++typePos] = bsPacketId[0];
		bytes[++typePos] = bsPacketId[1];
		bytes[++typePos] = groupId.getId();
		typePos+=4;
		bytes[typePos] = parameterId;
		byte[] bsValueSize = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(valueSize.shortValue()).array();
		bytes[++typePos] = bsValueSize[1];
		bytes[++typePos] = bsValueSize[0];
		if (valueSize > 0)
			System.arraycopy(value, 0, bytes, ++typePos, valueSize);
		byte[] checksum = new Checksum(Arrays.copyOf(bytes, bytes.length - 2)).toBytes();
		bytes[bytes.length - 2] = checksum[1];
		bytes[bytes.length - 1] = checksum[0];
	}

	public Packet(byte[] bytes, boolean fcm){

		typeIndex = fcm ? PACKET_TYPE_FCM_POSITION : PACKET_TYPE_POSITION; 	// without FLAG_SEQUENCE
		acknowLength = fcm ? ACKNOWLEDGEMENT_FCM_LENGTH : ACKNOWLEDGEMENT_LENGTH; 	// without FLAG_SEQUENCE

		if(bytes==null) {
			this.bytes = new byte[0];
			return;
		}

		final List<Integer> indexes = IntStream.range(0, bytes.length).map(i->bytes[i]==FLAG_SEQUENCE ? i : -1).filter(i->i>=0).boxed().collect(Collectors.toList());
		int start = -1;
		lastIndex = -1;
		for(int i=0;i<indexes.size();++i) {

			if(start < 0)
				start = indexes.get(i);

			else {
				lastIndex = indexes.get(i);
				if(start+1==lastIndex)
					start = lastIndex;
				else
					break;
			}
		}
		if(start<0 || start>=lastIndex)
			this.bytes = new byte[0];

		else{
			this.bytes = byteStuffing(Arrays.copyOfRange(bytes, ++start, lastIndex));
		}
	}

	public boolean checksum() {
		final byte[] checksum = new Checksum(Arrays.copyOf(bytes, bytes.length-2)).toBytes();
		return checksum[0] == bytes[bytes.length-2] && checksum[1] == bytes[bytes.length-1];
	}

	public Integer getChecksumValue() {

		if (bytes.length < 2)
			return null;

		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(bytes[bytes.length - 1]);
		bb.put(bytes[bytes.length - 2]);
		return bb.getShort(0) & 0xffff;
	}

	@ToString.Include
	public PacketType getPacketType() {

		if(bytes.length==0)
			return PacketType.SPONTANEOUS;

		final PacketType packetType = PacketType.valueOf(bytes[4]);
		if(packetType==PacketType.SPONTANEOUS)
			return PacketType.valueOf(bytes[0]);
		else
			return packetType;
	}

	@ToString.Include
	public int getPacketId() {
		if(!Optional.ofNullable(bytes).filter(bs->bs.length>typeIndex+2).isPresent())
			return -1;
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(bytes[typeIndex+1]);
		bb.put(bytes[typeIndex+2]);
		short shortVal = bb.getShort(0);
		return shortVal&0xffff;
	}

	public byte[] getAcknowledgement() {

		if(bytes==null || bytes.length<acknowLength) {
			logger.warn("Packet is too short to create Acknowledgement: {}\n\t{}", bytes==null ? "null" : bytes.length, bytes);
			return null;
		}

		final PacketType packetType = PacketType.valueOf(bytes[typeIndex]);
		if(packetType == PacketType.ACKNOWLEDGEMENT){
			if (bytes.length > acknowLength+2) {
				logger.warn("Acknowledgement packet is too long: {}\n\t{}", bytes.length, bytes);
				return null;
			}
			final byte[] controlEscape = controlEscape(bytes);
			final byte[] toSend = new byte[acknowLength + 4];
			System.arraycopy(controlEscape, 0, toSend, 1, controlEscape.length);
			toSend[0] = toSend[toSend.length-1] = FLAG_SEQUENCE;
			return toSend;
		}

		if(packetType == PacketType.RESPONSE || packetType == PacketType.REQUEST || packetType == PacketType.COMMAND) {

			final byte[] copyOf = Arrays.copyOf(bytes, acknowLength);
			copyOf[typeIndex] = PacketType.ACKNOWLEDGEMENT.getCode();
			final byte[] checksum = new Checksum(copyOf).toBytes();

			final byte[] withCHeckSum = Arrays.copyOf(copyOf, acknowLength+2);
			withCHeckSum[copyOf.length] = checksum[1];
			withCHeckSum[copyOf.length+1] = checksum[0];

			final byte[] controlEscape = controlEscape(withCHeckSum);
			final byte[] ackn = new byte[controlEscape.length+2];
			System.arraycopy(controlEscape, 0, ackn, 1, controlEscape.length);
			ackn[0] = ackn[ackn.length-1] = FLAG_SEQUENCE;
			return ackn;
		}

		return bytes;
	}

	public static byte[] byteStuffing(byte[] bytes) {

		int index = 0;
		if(bytes!=null){

			for(int i=0; i<bytes.length; i++)

				if(bytes[i]==CONTROL_ESCAPE){
					if(++i<bytes.length)
						bytes[index++] = (byte) (bytes[i]^0x20);
				}else
					bytes[index++] = bytes[i];
		}

		return bytes==null ? null : index==bytes.length ? bytes : Arrays.copyOf(bytes, index);
	}

	public static byte[] controlEscape(byte[] bytes){
		final List<Integer> list = new ArrayList<>();

		for(int i=0; i<bytes.length; i++){
			if(bytes[i]==FLAG_SEQUENCE || bytes[i]==CONTROL_ESCAPE){
				list.add(CONTROL_ESCAPE&0xff);
				list.add(bytes[i]^0x20);
			}else
				list.add(bytes[i]&0xff);
		}
		final int size = list.size();
		if(bytes.length==size)
			return bytes;

		byte[] controlEscape = new byte[size];
		for(int i=0; i<size;++i) {
			controlEscape[i] = list.get(i).byteValue();
		}
		return controlEscape;
	}

	public byte[] toBytes() {
		return bytes;
	}

	public byte[] toSend() {
		byte[] bytes = controlEscape(this.bytes);
		byte[] toSend = new byte[bytes.length + 2];
        toSend[0] = toSend[toSend.length - 1] = FLAG_SEQUENCE;
        System.arraycopy(bytes, 0, toSend, 1, bytes.length);
		return toSend;
	}

	@ToString.Include
	public PacketError getError() {
		final int errorIndex = typeIndex + PACKET_HEADER_SIZE-1;
		return Optional.ofNullable(bytes).filter(bs -> bs.length>=errorIndex).map(bs -> bs[errorIndex]).map(PacketError::valueByCode).orElse(PacketError.CORRUPTED_PACKAGE);
	}
}
