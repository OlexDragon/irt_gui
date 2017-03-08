
package irt.gui.data.packet.observable;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import irt.gui.data.PacketIdDetails;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;

public class TestPacket  extends Observable implements LinkedPacket {

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
		throw new UnsupportedOperationException("Auto-generated method stub");
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
		return PacketAbstract5.preparePacket(packetWithoutChecksum);
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
		return PacketAbstract5.preparePacket(Arrays.copyOf(packetWithoutChecksum, 7));
	}

	@Override
	public String toString() {
		return "\n\tTestPacket [\n\tpacketWithoutChecksum=" + ToHex.bytesToHex(packetWithoutChecksum) + ", \n\tanswer=" + ToHex.bytesToHex(answer) + "]";
	}

	@Override
	public Observer[] getObservers() throws Exception {
		throw new UnsupportedOperationException("Auto-generated method stub");
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
}
