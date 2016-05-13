package irt.gui.data.packet.observable.calibration;

import java.util.Arrays;
import java.util.List;

import irt.gui.controllers.LinkedPacketSender;
import irt.gui.controllers.calibration.tools.enums.ToolCommands;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.data.MyObservable;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;

public class ToolsPacket extends MyObservable implements LinkedPacket, WaitTime {

	private ToolCommands command; 	public ToolCommands getCommand() { return command; }

	private byte[] answer;

	public ToolsPacket(ToolCommands command) {
		this.command = command;
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override
	public PacketId getPacketId() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public LinkHeader getLinkHeader() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public PacketHeader getPacketHeader() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public List<Payload> getPayloads() {
		// TODO Auto-generated method stub
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
	public String toString() {
		return getClass().getSimpleName() + " [command=" + command + ", answer=" + Arrays.toString(answer) + "]";
	}

	@Override
	public int getWaitTime() {
		return LinkedPacketSender.TOOLS_WAIT_TIME;
	}
}
