package irt.data.listener;

import irt.data.packet.Packet;

import java.util.EventListener;

public interface PacketListener extends EventListener{

	void packetRecived(Packet packet);

}
