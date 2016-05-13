package irt.gui.controllers.calibration.tools;

import java.util.Observer;

import irt.gui.data.packet.interfaces.PacketToSend;

public interface Prologix {

	void send(String addr, PacketToSend packet, Observer observer);
	void listen();
}
