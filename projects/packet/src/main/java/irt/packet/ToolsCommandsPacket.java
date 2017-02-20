package irt.packet;

import java.util.List;
import java.util.stream.Collectors;

import irt.data.prologix.Eos;
import irt.packet.interfaces.PacketToSend;
import irt.packet.interfaces.WaitTime;
import irt.services.MyObservable;
import irt.services.ToHex;

/**
 * 
 */
public class ToolsCommandsPacket extends MyObservable implements PacketToSend, WaitTime{

	private byte[] answer;
	private List<PacketToSend> packets;

	public ToolsCommandsPacket(List<PacketToSend> packets) {
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
	public void clear() {
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

	@Override
	public LinkHeader getLinkHeader() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public byte[] getEndSequence() {
		return Eos.LF.toBytes();
	}

	@Override
	public int getWaitTime() {
		return packets.size() * 15;
	}

	@Override
	public String toString() {
		String commands = packets.stream().map(p->p.toString()).collect (Collectors.joining ("; "));
		return "ToolsCommandsPacket [commands=" + commands + ", answer=" + ToHex.bytesToHex(answer) + "]";
	}
}
