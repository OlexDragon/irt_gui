
package irt.gui.data.packet.observable;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;

public class TestPacket  extends Observable implements LinkedPacket {

	private byte[] packetWithoutChecksum;
	private byte[] answer;

	public TestPacket(byte[] packetWithoutChecksum) {
		this.packetWithoutChecksum = packetWithoutChecksum;
	}

	@Override
	public int compareTo(LinkedPacket o) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public LinkHeader getLinkHeader() {
		throw new UnsupportedOperationException("Auto-generated method stub");
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
}
