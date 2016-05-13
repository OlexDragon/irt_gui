package irt.gui.data.packet.observable.calibration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import irt.gui.data.MyObservable;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.interfaces.PacketToSend;

public class ToolsComandsPacket extends MyObservable implements PacketToSend{

	private final byte[] commands;
	private byte[] answer;

	public ToolsComandsPacket(PacketToSend... packets) {
		final List<byte[]> collect = Arrays
										.stream(packets)
										.map(p->p.toBytes())
										.collect(Collectors.toList());
		final int lingth = collect
								.parallelStream()
								.mapToInt(bs->bs.length)
								.sum();

		commands = new byte[lingth];

		for(int i=0, x=0; i<collect.size(); i++){

			final byte[] bs = collect.get(i);
			final int length = bs.length;

			System.arraycopy(bs, 0, commands, x, length);
			x += length;
		}
	}

	public byte[] toBytes(){
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

	@Override
	public LinkHeader getLinkHeader() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override
	public String toString() {
		return "ToolsComandsPacket [commands=" + ToHex.bytesToHex(commands) + ", answer=" + ToHex.bytesToHex(answer) + "]";
	}
}
