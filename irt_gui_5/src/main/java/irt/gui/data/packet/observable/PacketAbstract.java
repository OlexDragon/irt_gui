
package irt.gui.data.packet.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import irt.gui.data.ChecksumLinkedPacket;
import irt.gui.data.MyObservable;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.AlarmPacket;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.errors.PacketParsingException;

@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.CLASS)
public abstract class PacketAbstract extends MyObservable implements LinkedPacket {

	@JsonIgnore
	protected final Logger logger = LogManager.getLogger(getClass().getName());

	@JsonIgnore
	protected LinkHeader linkHeader = new LinkHeader((byte)254, (byte)0, (short)0);
	@JsonIgnore
	protected PacketHeader packetHeader;
	@JsonIgnore
	protected List<Payload> payloads = new ArrayList<>();
	protected byte[] answer;

	public PacketAbstract(PacketHeader packetHeader, Payload payload) {

		this.packetHeader = packetHeader;
		payloads.add(payload);
	}

	public PacketAbstract(PacketProperties packetProperties, byte[] answer) throws PacketParsingException {

		answer = Optional
				.ofNullable(answer)
				.filter(a->a.length>0)
				.orElseThrow(()->new PacketParsingException("\n\t Constructor parameter can not be null or empty."));

		byte[] bs  = removeAcknowledgmentAndChecksum(answer, packetProperties);

		linkHeader = packetProperties.isConverter() ? new LinkHeader((byte)-1, (byte)0, (byte)0) : new LinkHeader(bs);
		packetHeader = new PacketHeader(bs, packetProperties);
		payloads.addAll(Payload.parsePayloads(packetProperties, bs));
	}

	private byte[] removeAcknowledgmentAndChecksum(byte[] answer, PacketProperties packetProperties) throws PacketParsingException {

		//Acknowledgement
		int beginning = indexOf(answer, Packet.FLAG_SEQUENCE);
		if(beginning<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t Das not has Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		int end = indexOf(answer, ++beginning, Packet.FLAG_SEQUENCE);
		if(end<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t Das not has second Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		if(!packetProperties.hasAcknowledgment())
			return getPacketAsArray(answer, 0, packetProperties);

		byte[] acknowledgement = byteStuffing(Arrays.copyOfRange(answer, beginning, end));
		if(!checkAcknowledgement(acknowledgement, packetProperties))
			throw new PacketParsingException("\n\t The Acknowledgement length is not correct\n\t" + ToHex.bytesToHex(acknowledgement) + "\n\tanswer: " + ToHex.bytesToHex(answer));

		//acknowledgement checksum
		acknowledgement = byteStuffing(acknowledgement);
		ChecksumLinkedPacket ch = new ChecksumLinkedPacket(Arrays.copyOf(acknowledgement, acknowledgement.length-2));
		byte[] checksum = Arrays.copyOfRange(acknowledgement, acknowledgement.length-2, acknowledgement.length);

		//check acknowledgement checksum
		final byte[] checksumToBytes = ch.toBytes();
		logger.trace("\n\t answer:{}\n\t checksum1:{}\n\t checksum2:{}",ch, checksumToBytes, checksum);
		if(Arrays.equals(checksum, checksumToBytes))
			return getPacketAsArray(answer, ++end, packetProperties);

		else
			throw new PacketParsingException("\n\t Acknowledgement checksum is not correct:\n\t" + ToHex.bytesToHex(answer) + "\n\t"
												+ "acknowledgement: " + ToHex.bytesToHex(acknowledgement) + "\n\t"
												+ "checksum: " + ToHex.bytesToHex(checksum) +
												ch
					);
	}

	protected boolean checkAcknowledgement(byte[] acknowledgement, PacketProperties packetProperties) {
		final int length = acknowledgement.length;
		final boolean isConverter = length==5;
		packetProperties.setConverter(isConverter);
		return length==9 || isConverter;// 9 LinkedPacket; 5 - Converter
	}

	private byte[] getPacketAsArray(byte[] answer, int beginning, PacketProperties packetProperties) throws PacketParsingException {
		//Packet
		beginning = indexOf(answer, beginning, Packet.FLAG_SEQUENCE);
		if(beginning<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t NO Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		int end = indexOf(answer, ++beginning, Packet.FLAG_SEQUENCE);
		if(end<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t NO second Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		//remove garbage
		if((end-beginning) > packetProperties.gerHeadersSize()){

			byte[] packet = byteStuffing( Arrays.copyOfRange(answer, beginning, end));

			ChecksumLinkedPacket ch = new ChecksumLinkedPacket(Arrays.copyOf(packet, packet.length-2));

			byte[] checksum = Arrays.copyOfRange(packet, packet.length-2, packet.length);

			if(Arrays.equals(checksum, ch.toBytes()))
				return ch.getChecksumOf();

			else
				throw new PacketParsingException("\n\t " + getClass().getSimpleName() + " checksum is not correct:\n\t"
												+ "ansver checksum: " + ToHex.bytesToHex(checksum)
												+ ch + "\n\t"
												+ "ansver: " + ToHex.bytesToHex(answer) + "\n\t"
												+ "packet with checksum: " + ToHex.bytesToHex(packet));
		}else
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t" + ToHex.bytesToHex(answer));
	}

	private int indexOf(byte[] answer, byte flag) {
		return indexOf(answer, 0, flag);
	}

	private int indexOf(byte[] answer, int startFrom, byte flag) {

		int index = -1;
		for(int i=startFrom; i<answer.length; i++)
			if(answer[i]==flag){
				index = i;
				break;
			}

		return logger.exit(index);
	}

	@Override public LinkHeader getLinkHeader() {
		return linkHeader.immutable();
	}

	protected synchronized void setPacketHeader(PacketHeader packetHeader) {
		 this.packetHeader = packetHeader;
	}

	@Override public PacketHeader getPacketHeader() {
		return packetHeader;
	}

	@Override public List<Payload> getPayloads() {
		return payloads;
	}

	@Override @JsonProperty(value="asBytes")
	public synchronized byte[] toBytes() {

		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.toBytes();

			if(packetHeader!=null){
				b = Packet.concat(b, packetHeader.toBytes());

				if(payloads!=null)
					for(Payload d:payloads)
						b = Packet.concat(b, d.toBytes());
			}
		}

		return preparePacket(b);
	}

	/** set answer and notify observers */
	@Override public void setAnswer(byte[] answer) {
		this.answer = answer;

		setChanged();
		notifyObservers();
	}

	@Override public void clearAnswer() {
		answer = null;
	}

	@Override public byte[] getAnswer() {
		return answer;
	}

	@Override @JsonIgnore
	public byte[] getAcknowledgement() {
		
		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.toBytes();

			if(packetHeader!=null){

				byte[] phb = packetHeader.getPacketIdDetails().toBytes();

				if(b.length==0)
					b = new byte[3];
				else
					b = Arrays.copyOf(b, 7);

				int length = b.length;
				b[length-3] = PacketType.ACKNOWLEGEMENT.getValue();
				b[length-2] = phb[0];
				b[length-1] = phb[1];

			}
		}

		return preparePacket(b);
	}

	private static final Logger l = LogManager.getLogger();
	public static byte[] preparePacket(byte[] data) {

		if(data!=null){
			byte[] p = new byte[(int) (data.length*2.4)];
			int index = 0;
			p[index++] = Packet.FLAG_SEQUENCE;
			for(int i=0; i< data.length; i++, index ++){
				index = checkControlEscape(data, i, p, index);
			}

			final ChecksumLinkedPacket checksum = new ChecksumLinkedPacket(data);
			l.trace(checksum);
			byte[] csTmp = Packet.toBytes((short)checksum.getChecksum());
			for(int i=1; i>=0; i--, index ++)
				index = checkControlEscape(csTmp, i, p, index);

			p[index++] = Packet.FLAG_SEQUENCE;

			data = Arrays.copyOf(p, index);
		}
		return data;
	}

	public static int checkControlEscape(byte[] surce, int surceIndex, byte[] destination, int destinationIndex) {
		if(surce[surceIndex]==Packet.FLAG_SEQUENCE || surce[surceIndex]==Packet.CONTROL_ESCAPE){
			destination[destinationIndex++] = Packet.CONTROL_ESCAPE;
			destination[destinationIndex]	= (byte) (surce[surceIndex] ^ 0x20);
		}else
			destination[destinationIndex] = (byte) surce[surceIndex];
		return destinationIndex;
	}

	public static byte[] byteStuffing(byte[] readBytes) {

		byte[] result = null;
		if(readBytes!=null){
			final int length = readBytes.length;
			result = new byte[length];

			int index = 0;
			int i=0;
			for(; i<length; i++, index++)

				if(readBytes[i]==Packet.CONTROL_ESCAPE){
					if(i++<length)
						result[index] = (byte)(readBytes[i]^0x20);
				}else
					result[index] = readBytes[i];

			if(index<i)
				result = Arrays.copyOf(result, index);
		}

		return result;
	}

	@Override public synchronized void setLinkHeaderAddr(byte addr) {
		linkHeader.setAddr(addr);
	}

	@Override public int hashCode() {
		return 31 + ((packetHeader == null) ? 0 : packetHeader.hashCode());
	}

	@Override public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PacketAbstract other = (PacketAbstract) obj;
		if (packetHeader == null) {
			if (other.packetHeader != null)
				return false;
		} else if (!packetHeader.equals(other.packetHeader))
			return false;
		
		if (linkHeader == null) {
			if (other.linkHeader != null)
				return false;
		} else if (linkHeader.getAddr()!=other.linkHeader.getAddr())
			return false;
		
		return this==obj || (obj instanceof PacketAbstract ? hashCode()==obj.hashCode() : false);
	}

	@Override public int compareTo(PacketToSend packet) {
		
		PacketType packetType = packetHeader.getPacketType();
		byte v1 = packetType.getValue();
		if(packetType!=PacketType.COMMAND && !(this instanceof AlarmPacket))
			v1--;
		
		packetType = ((LinkedPacket)packet).getPacketHeader().getPacketType();
		byte v2 = packetType.getValue();
		if(packetType!=PacketType.COMMAND && !(packet instanceof AlarmPacket))
			v2--;

		return Byte.compare(v2, v1);
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()+ " [linkHeader=" + linkHeader + ", packetHeader=" + packetHeader + ", payloads=" + payloads + ", \n\t answer=" + ToHex.bytesToHex(answer) +
				"\n\t toBytes()=" + ToHex.bytesToHex(toBytes()) + "]";
	}
}
