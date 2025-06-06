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

	public static final int PACKET_TYPE_POSITION = 4;	// without FLAG_SEQUENCE
	public static final int PACKET_TYPE_FCM_POSITION = 0;	// without FLAG_SEQUENCE
	public static final int ACKNOWLEDGEMENT_LENGTH = 9;	// with checksum
	public static final int ACKNOWLEDGEMENT_FCM_LENGTH = 5;	// with checksum
	public static final byte FLAG_SEQUENCE	= 0x7E;
	public static final byte CONTROL_ESCAPE = 0x7D;;

	private final byte[] bytes;
	@Getter
	private int lastIndex;

	public Packet(byte[] bytes) {

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

		if(bytes==null || bytes.length<Packet.PACKET_TYPE_POSITION)
			return null;

		final PacketType packetType = PacketType.valueOf(bytes[Packet.PACKET_TYPE_POSITION]);
		if(packetType == PacketType.ACKNOWLEDGEMENT) {

			final byte[] controlEscape = controlEscape(bytes);
			final byte[] ackn = new byte[controlEscape.length+2];
			System.arraycopy(controlEscape, 0, ackn, 1, controlEscape.length);
			ackn[0] = ackn[ackn.length-1] = FLAG_SEQUENCE;
			return ackn;

		}else if(packetType == PacketType.RESPONSE || packetType == PacketType.REQUEST || packetType == PacketType.COMMAND) {
			final byte[] copyOf = Arrays.copyOf(bytes, Packet.ACKNOWLEDGEMENT_LENGTH);
			copyOf[PACKET_TYPE_POSITION] = PacketType.ACKNOWLEDGEMENT.getCode();
			final byte[] bs = Arrays.copyOf(copyOf, copyOf.length-2);
			final byte[] checksum = new Checksum(bs).toBytes();
			copyOf[copyOf.length-2] = checksum[1];
			copyOf[copyOf.length-1] = checksum[0];
			final byte[] controlEscape = controlEscape(copyOf);
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
