package irt.gui.data.packet.observable.flash;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import irt.gui.controllers.flash.enums.Command;
import irt.gui.controllers.interfaces.WaitTime;
import irt.gui.controllers.serial_port.PacketSenderJssc;
import irt.gui.data.ToHex;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;

public abstract class AbstractFlashPacket extends Observable implements LinkedPacket, FlashPacket, WaitTime{

	private Command command;
	private byte[] answer;

	public AbstractFlashPacket(Command command){
		this.command = Objects.requireNonNull(command);
	}

	@Override
	public int compareTo(PacketToSend o) {
		return 1;
	}

	@Override
	public PacketHeader getPacketHeader() {
		notifyObservers();
		return null;
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
		return getObservers(this);
	}

	public static Observer[] getObservers(LinkedPacket packet) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		final Field obs = Observable.class.getDeclaredField("obs");
		obs.setAccessible(true);
		@SuppressWarnings("unchecked")
		final Vector<Observer> vector = (Vector<Observer>) obs.get(packet);
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

	@Override
	public List<Payload> getPayloads() {
		throw new UnsupportedOperationException("This function should not be used");
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
	public boolean setLinkHeaderAddr(byte addr) {
		throw new UnsupportedOperationException("This function should not be used");
	}

	@Override
	public int getWaitTime() {
		return PacketSenderJssc.FLASH_MEMORY_WAIT_TIME;
	}
}
