
package irt.packet.observable;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import irt.packet.LinkHeader;
import irt.packet.PacketHeader;
import irt.packet.PacketIdDetails;
import irt.packet.Payload;
import irt.packet.enums.PacketErrors;
import irt.packet.enums.PacketId;
import irt.packet.enums.PacketType;
import irt.packet.interfaces.LinkedPacket;
import irt.packet.interfaces.PacketToSend;
import irt.services.MyObservable;
import irt.services.ToHex;

public class TestPacket  extends MyObservable implements LinkedPacket {

	private byte[] packetWithoutChecksum;
	private byte[] answer;
	private final LinkHeader linkHeader;

	public TestPacket(byte address) {
		linkHeader = new LinkHeader(address, (byte)0, (short) 0);
		final PacketIdDetails packetIdDetails = new PacketIdDetails(PacketId.ALARMS, "Scan addresses");
		PacketHeader packetHeader = new PacketHeader(PacketType.REQUEST,  packetIdDetails, PacketErrors.NO_ERROR);

		packetWithoutChecksum = linkHeader.toBytes();
		byte[] phb = packetHeader.getPacketIdDetails().toBytes();

		packetWithoutChecksum = Arrays.copyOf(packetWithoutChecksum, 7);
		packetWithoutChecksum[4] = PacketType.ACKNOWLEGEMENT.getValue();
		packetWithoutChecksum[5] = phb[0];
		packetWithoutChecksum[6] = phb[1];
	}

	public TestPacket(byte[] packetWithoutChecksum) {
		this.packetWithoutChecksum = packetWithoutChecksum;
		 linkHeader = new LinkHeader((byte)0, (byte)0, (byte)0);
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 1;
	}

	@Override
	public LinkHeader getLinkHeader() {
		return linkHeader;
	}

	@Override
	public PacketHeader getPacketHeader() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public List<Payload> getPayloads() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public byte[] toBytes() {
		return PacketAbstract.preparePacket(packetWithoutChecksum);
	}

	@Override
	public byte[] getAnswer() {
		return answer;
	}

	@Override
	public void setAnswer(byte[] data) {
		answer = data;

		setChanged();
		notifyObservers();
	}

	@Override
	public byte[] getAcknowledgement() {
		return PacketAbstract.preparePacket(Arrays.copyOf(packetWithoutChecksum, 7));
	}

	@Override
	public String toString() {
		return "\n\tTestPacket [\n\tpacketWithoutChecksum=" + ToHex.bytesToHex(packetWithoutChecksum) + ", \n\tanswer=" + ToHex.bytesToHex(answer) + "]";
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override @JsonIgnore
	public PacketId getPacketId() {
		return null;
	}

	@Override
	public void clearAnswer() {
	}

	@Override
	public int hashCode() {
		return 31  + ((linkHeader == null) ? 0 : linkHeader.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestPacket other = (TestPacket) obj;
		if (linkHeader == null) {
			if (other.linkHeader != null)
				return false;
		} else if (!linkHeader.equals(other.linkHeader))
			return false;
		return true;
	}

	@Override
	public byte[] getEndSequence() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}
}
