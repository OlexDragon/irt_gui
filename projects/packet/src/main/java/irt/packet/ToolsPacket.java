package irt.packet;

import java.util.Arrays;

import irt.data.prologix.Eos;
import irt.data.tools.interfaces.ToolCommands;
import irt.packet.interfaces.PacketToSend;
import irt.packet.interfaces.WaitTime;
import irt.services.MyObservable;

public class ToolsPacket extends MyObservable implements PacketToSend, WaitTime {

	private static final int TOOLS_WAIT_TIME = 10;
	private ToolCommands command; 	public ToolCommands getCommand() { return command; }

	private byte[] answer;

	protected ToolsPacket(ToolCommands command) {
		this.command = command;
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override
	public LinkHeader getLinkHeader() {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public byte[] toBytes() {
		return command.getCommand();
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
	public void clearAnswer() {
		answer = null;
	}

	@Override
	public byte[] getAcknowledgement() {
		return null;
	}

	@Override
	public int getWaitTime() {
		return TOOLS_WAIT_TIME;
	}

	@Override
	public int hashCode() {
		return command.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ToolsPacket ? command.equals(((ToolsPacket)obj).command) : false;
	}

	@Override
	public byte[] getEndSequence() {
		return Eos.LF.toBytes();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [command=" + command + "), answer=" + Arrays.toString(answer) + "]";
	}
}
