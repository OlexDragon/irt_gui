package irt.packet.interfaces;

import java.util.Observer;

import irt.packet.LinkHeader;

public interface PacketToSend extends Comparable<PacketToSend> {

	byte[] toBytes();
	byte[] getAcknowledgement();
	void clearAnswer();
	void setAnswer(byte[] data);
	byte[] getAnswer();
	Observer[] getObservers() throws Exception;
	void deleteObserver(Observer observerFrequency);
	LinkHeader getLinkHeader();
	void addObserver(Observer o);
	void setLinkHeaderAddr(byte addr);
	byte[] getEndSequence();
}
