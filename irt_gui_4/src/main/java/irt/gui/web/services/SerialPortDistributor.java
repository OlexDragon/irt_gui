package irt.gui.web.services;

import java.util.concurrent.FutureTask;

import irt.gui.web.beans.RequestPacket;

public interface SerialPortDistributor {

	FutureTask<RequestPacket> send(RequestPacket requestPacket);
	void shutdown();
	void lockPort(String spName);
	void unlockPort(String spName);
	boolean closePort(String spName);

}
