package irt.gui.data.packet.observable.calibration;

import java.util.Arrays;

import irt.gui.controllers.calibration.tools.enums.ToolCommands;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.controllers.serial_port.PacketSenderJssc;
import irt.gui.data.MyObservable;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.interfaces.PacketToSend;

public class ToolsPacket extends MyObservable implements PacketToSend, WaitTime {

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
	public boolean setLinkHeaderAddr(byte addr) {
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
		return PacketSenderJssc.TOOLS_WAIT_TIME;
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
	public String toString() {
		return getClass().getSimpleName() + " [command=" + command + "(" + command.getValue() + "), answer=" + Arrays.toString(answer) + "]";
	}
}
