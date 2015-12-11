
package irt.gui.data.packet.observable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.ChecksumLinkedPacket;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.errors.PacketParsingException;

public abstract class PacketAbstract extends Observable implements LinkedPacket {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	protected LinkHeader linkHeader = new LinkHeader((byte)254, (byte)0, (short)0);
	protected PacketHeader packetHeader;
	protected List<Payload> payloads = new ArrayList<>();
	protected byte[] answer;

	public PacketAbstract(PacketHeader packetHeader, Payload payload) {

		this.packetHeader = packetHeader;
		payloads.add(payload);
	}

	public PacketAbstract(PacketId packetId, byte[] answer) throws PacketParsingException {
		logger.trace("\n\t ENTRY ({})", answer);

		answer = Optional
				.ofNullable(answer)
				.filter(a->a.length>0)
				.orElseThrow(()->new PacketParsingException("\n\t Constructor parameter can not be null or empty."));

		byte[] bs  = removeAcknowledgmentAndChecksum(answer);

		linkHeader = new LinkHeader(bs);
		packetHeader = new PacketHeader(bs);
		payloads.addAll(Payload.parsePayloads(packetId, bs));
	}

	private byte[] removeAcknowledgmentAndChecksum(byte[] answer) throws PacketParsingException {

		//Acknowledgement
		int beginning = indexOf(answer, Packet.FLAG_SEQUENCE);
		if(beginning<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t Das not has Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		int end = indexOf(answer, ++beginning, Packet.FLAG_SEQUENCE);
		if(end<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t Das not has second Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		byte[] acknowledgement = byteStuffing(Arrays.copyOfRange(answer, beginning, end));
		if(acknowledgement.length!=9)
			throw new PacketParsingException("\n\t The Acknowledgement length is not correct\n\t" + ToHex.bytesToHex(acknowledgement) + "\n\tanswer: " + ToHex.bytesToHex(answer));

		//acknowledgement checksum
		acknowledgement = byteStuffing(acknowledgement);
		ChecksumLinkedPacket ch = new ChecksumLinkedPacket(Arrays.copyOf(acknowledgement, acknowledgement.length-2));
		byte[] checksum = Arrays.copyOfRange(acknowledgement, acknowledgement.length-2, acknowledgement.length);

		//check acknowledgement checksum
		final byte[] checksumToBytes = ch.toBytes();
		logger.trace("\n\t answer:{}\n\t checksum1:{}\n\t checksum2:{}",ch, checksumToBytes, checksum);
		if(Arrays.equals(checksum, checksumToBytes))
			return getPacket(answer, ++end);

		else
			throw new PacketParsingException("\n\t Acknowledgement checksum is not correct:\n\t" + ToHex.bytesToHex(answer) + "\n\t"
												+ "acknowledgement: " + ToHex.bytesToHex(acknowledgement) + "\n\t"
												+ "checksum: " + ToHex.bytesToHex(checksum) +
												ch
					);
	}

	private byte[] getPacket(byte[] answer, int beginning) throws PacketParsingException {
		//Packet
		beginning = indexOf(answer, beginning, Packet.FLAG_SEQUENCE);
		if(beginning<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t NO Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		int end = indexOf(answer, ++beginning, Packet.FLAG_SEQUENCE);
		if(end<0)
			throw new PacketParsingException("\n\t The Packet structure is not correct:\n\t NO second Packet.FLAG_SEQUENCE : 126(0x7E)\n\t" + ToHex.bytesToHex(answer));

		//remove garbage
		if((end-beginning)>LinkHeader.SIZE+PacketHeader.SIZE){

			byte[] packet = byteStuffing( Arrays.copyOfRange(answer, beginning, end));

			ChecksumLinkedPacket ch = new ChecksumLinkedPacket(Arrays.copyOf(packet, packet.length-2));

			byte[] checksum = Arrays.copyOfRange(packet, packet.length-2, packet.length);

			logger.trace("\n\t answer:{}\n\t checksum:{}",ch, checksum);
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

	@Override
	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	public PacketHeader getPacketHeader() {
		return packetHeader;
	}

	@Override
	public List<Payload> getPayloads() {
		return payloads;
	}

	@Override
	public byte[] toBytes() {

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

	@Override
	public void setAnswer(byte[] answer) {
		this.answer = answer;

		if(answer!=null){
			setChanged();
			notifyObservers();
		}
	}

	@Override
	public byte[] getAnswer() {
		return answer;
	}

	@Override
	public byte[] getAcknowledgement() {
		
		byte[] b = null;
		if(linkHeader!=null){
			b = linkHeader.toBytes();

			if(packetHeader!=null){

				byte[] phb = packetHeader.getPacketIdDetails().toBytes();

				b = Arrays.copyOf(b, 7);
				b[4] = PacketType.ACKNOWLEGEMENT.getValue();
				b[5] = phb[0];
				b[6] = phb[1];

			}else
				b = null;
		}

		return preparePacket(b);
	}

	private static final Logger l = LogManager.getLogger();
	public static byte[] preparePacket(byte[] data) {
		if(data!=null){
			byte[] p = new byte[data.length*2];
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

			data = Arrays.copyOf(p, index);//new byte[index];
//			System.arraycopy(p, 0, data, 0, index);
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

	@SuppressWarnings("unchecked")
	public Observer[] getObservers() throws Exception{

		final Field obs = Observable.class.getDeclaredField("obs");
		obs.setAccessible(true);
		final Vector<Observer> vector = (Vector<Observer>) obs.get(this);
		return vector.toArray(new Observer[vector.size()]);
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		getLinkHeader().setAddr(addr);
	}

	@Override
	public int hashCode() {
		return 31 + ((packetHeader == null) ? 0 : packetHeader.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
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
		return this==obj || (obj instanceof PacketAbstract ? hashCode()==obj.hashCode() : false);
	}

	@Override
	public int compareTo(LinkedPacket packet) {

		byte v1 = packetHeader.getPacketType().getValue();
		byte v2 = packet.getPacketHeader().getPacketType().getValue();

		return Byte.compare(v2, v1);
	}

	@Override
	public String toString() {
		return "\n\t" + getClass().getSimpleName()+ " [linkHeader=" + linkHeader + ", packetHeader=" + packetHeader + ", payloads=" + payloads + ", \n\t answer=" + ToHex.bytesToHex(answer) + "]";
	}
}
