
package irt.gui.data.packet.observable.calibration.prologix;

import java.util.Objects;
import java.util.Observer;

import irt.gui.controllers.calibration.tools.prologix.enums.PrologixCommands;
import irt.gui.data.MyObservable;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.interfaces.PacketToSend;

public class PrologixPacket extends MyObservable implements PacketToSend {

	private PrologixCommands command; public PrologixCommands getCommand() { return command; }
	private Object sentValue;

	private byte[] answer;

	public PrologixPacket(PrologixCommands command) {
		this.command = Objects.requireNonNull(command);
	}

	@Override public int compareTo(PacketToSend o) {
		return 0;
	}

	@Override public byte[] toBytes() {
		sentValue = command.getValue();
		return command.getCommand();
	}

	public byte[] getAnswer() {
		return answer;
	}

	@Override public void setAnswer(byte[] data) {
		answer = data;
		if(sentValue==null){
			setChanged();
			notifyObservers();
		}
	}

	@Override public void clearAnswer() {
		answer = null;
	}

	@Override public byte[] getAcknowledgement() {
		return null;
	}

	@Override public Observer[] getObservers() throws Exception {
		if(command.getValue()==null)
			return super.getObservers();
		else
			return new Observer[0];
		
	}

	@Override public LinkHeader getLinkHeader() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override public void deleteObservers() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override public String toString() {
		return getClass().getSimpleName() + " [" + command + ", answer=" + ToHex.bytesToHex(answer) + "]";
	}
}
