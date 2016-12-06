package irt.fx.control.prologix.interfaces;

import irt.packet.interfaces.PacketToSend;

public interface Prologix {

	void send(String addr, PacketToSend packet);
	void setPrologixToListen();
}
