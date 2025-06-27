package irt.gui.web.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Packet {

	public static final int PACKET_TYPE_POSITION = 4;
	public static final int PACKET_TYPE_FCM_POSITION = 0;	// without FLAG_SEQUENCE
	public static final int ACKNOWLEDGEMENT_LENGTH = 7;	
	public static final int ACKNOWLEDGEMENT_FCM_LENGTH = 3;
	public static final byte FLAG_SEQUENCE	= 0x7E;
	public static final byte CONTROL_ESCAPE = 0x7D;;

	private final int typeIndex;
	private final int length;
	private final byte[] bytes;

	@Getter private int lastIndex;

	public Packet(byte[] bytes, boolean fcm) {

		typeIndex = fcm ? PACKET_TYPE_FCM_POSITION : PACKET_TYPE_POSITION; 	// without FLAG_SEQUENCE
		length = fcm ? ACKNOWLEDGEMENT_FCM_LENGTH : ACKNOWLEDGEMENT_LENGTH; 	// without FLAG_SEQUENCE

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

	public byte[] getAcknowledgement() {

		if(bytes==null || bytes.length<length)
			return null;


		final PacketType packetType = PacketType.valueOf(bytes[typeIndex]);
		if(packetType == PacketType.ACKNOWLEDGEMENT){
			final byte[] controlEscape = controlEscape(bytes);
			final byte[] toSend = new byte[length + 4];
			System.arraycopy(controlEscape, 0, toSend, 1, controlEscape.length);
			toSend[0] = toSend[toSend.length-1] = FLAG_SEQUENCE;
			return toSend;
		}

		if(packetType == PacketType.RESPONSE || packetType == PacketType.REQUEST || packetType == PacketType.COMMAND) {

			final byte[] copyOf = Arrays.copyOf(bytes, length);
			copyOf[typeIndex] = PacketType.ACKNOWLEDGEMENT.getCode();
			final byte[] checksum = new Checksum(copyOf).toBytes();

			final byte[] withCHeckSum = Arrays.copyOf(copyOf, length+2);
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
}
