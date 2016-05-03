package irt.gui.data.packet.observable.flash;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import irt.gui.controllers.flash.enums.Command;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket;

public class AbstractFlashPacket extends Observable implements LinkedPacket, FlashPacket{

	private Command command;
	private byte[] answer;

	public AbstractFlashPacket(Command command){
		this.command = Objects.requireNonNull(command);
	}

	@Override
	public int compareTo(LinkedPacket o) {
		return 1;
	}

	@Override
	public PacketId getPacketId() {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override
	public LinkHeader getLinkHeader() {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override
	public void setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override
	public PacketHeader getPacketHeader() {
		notifyObservers();
		return null;
	}

	@Override
	public List<Payload> getPayloads() {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override
	public byte[] toBytes() {
		return command.toBytes();
	}

	@Override
	public byte[] getAnswer() {
		return answer;
	}

	@Override
	public void setAnswer(byte[] answer) {
		this.answer = answer;
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
	public Observer[] getObservers() throws Exception {

		final Field obs = Observable.class.getDeclaredField("obs");
		obs.setAccessible(true);
		@SuppressWarnings("unchecked")
		final Vector<Observer> vector = (Vector<Observer>) obs.get(this);
		return vector.toArray(new Observer[vector.size()]);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [answer=" + ToHex.bytesToHex(answer) + ", toBytes()=" + ToHex.bytesToHex(toBytes()) + "]";
	}

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	@Override
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
}
