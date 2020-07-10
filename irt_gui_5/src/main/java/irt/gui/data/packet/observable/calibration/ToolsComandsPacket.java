package irt.gui.data.packet.observable.calibration;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import irt.gui.data.MyObservable;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.interfaces.PacketToSend;

/**
 * 
 */
public class ToolsComandsPacket extends MyObservable implements PacketToSend{

	private byte[] answer;
	private List<PacketToSend> packets;

	public ToolsComandsPacket(List<PacketToSend> packets) {
		this.packets = packets;
	}

	public byte[] toBytes() {

		final List<byte[]> collect = packets.stream().map(p -> p.toBytes()).collect(Collectors.toList());
		final int lingth = collect.parallelStream().mapToInt(bs -> bs.length).sum();

		byte[] commands = new byte[lingth];

		for (int i = 0, x = 0; i < collect.size(); i++) {

			final byte[] bs = collect.get(i);
			final int length = bs.length;

			System.arraycopy(bs, 0, commands, x, length);
			x += length;
		}

		return commands;
	}

	@Override
	public byte[] getAcknowledgement() {
		return null;
	}

	@Override
	public void clearAnswer() {
		answer = null;
	}

	@Override
	public void setAnswer(byte[] data) {
		answer = data;
		setChanged();
		notifyObservers();
	}

	public byte[] getAnswer() {
		return answer;
	}

	@Override @JsonIgnore
	public LinkHeader getLinkHeader() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override
	public String toString() {
		return "ToolsComandsPacket [commands=" + ToHex.bytesToHex(toBytes()) + ", answer=" + ToHex.bytesToHex(answer) + "]";
	}

	@Override
	public boolean setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}
}
