
package irt.fx.control.prologix.packets;

import java.util.Objects;
import java.util.Observer;
import java.util.Optional;

import irt.fx.control.prologix.enums.PrologixCommands;
import irt.packet.LinkHeader;
import irt.packet.interfaces.PacketToSend;
import irt.packet.interfaces.WaitTime;
import irt.services.MyObservable;
import irt.services.ToHex;

public class PrologixPacket extends MyObservable implements PacketToSend, WaitTime {

	private static final int PROLOGIX_WAIT_TIME = 10;

	private PrologixCommands command; public PrologixCommands getCommand() { return command; }

	private byte[] answer;

	public PrologixPacket(PrologixCommands command) {
		this.command = Objects.requireNonNull(command);
	}

	@Override public int compareTo(PacketToSend o) {

		if(o instanceof PrologixPacket){

			int val1 = command.getValue() == null ? 0 : 1;
			int val2 = ((PrologixPacket)o).command.getValue() == null ? 0 : 1;

			//This function used in priority blocking queue 
			return Integer.compare(val2, val1);
		}
		return 0;
	}

	@Override public byte[] toBytes() {
		return command.getCommand();
	}

	public byte[] getAnswer() {
		return answer;
	}

	@Override public void setAnswer(byte[] data) {
		answer = data;
		if(command.getOldValue()==null){
			setChanged();
			notifyObservers();
		}
	}

	@Override
	public int hashCode() {
		int hc = Optional
					.ofNullable(command)
					.map(c->c.hashCode())
					.map(h->h + (command.getValue()==null ? 0 : command.getValue().hashCode()))
					.orElse(0);

		return 31 + hc;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		PrologixPacket other = (PrologixPacket) obj;

		if (command != other.command)
			return false;
		return true;
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
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override public String toString() {
		return getClass().getSimpleName() + " [" + command + ", answer=" + ToHex.bytesToHex(answer) + "]";
	}

	@Override
	public int getWaitTime() {
		return command.getValue()==null && command.getOldValue()==null ? PROLOGIX_WAIT_TIME : 1;
	}

	@Override
	public byte[] getEndSequence() {
		return "\r\n".getBytes();
	}
}
