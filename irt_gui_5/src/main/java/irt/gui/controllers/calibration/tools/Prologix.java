package irt.gui.controllers.calibration.tools;

import irt.gui.data.packet.interfaces.PacketToSend;

public interface Prologix {

	void send(String addr, PacketToSend packet);
	void listen();
}
