package irt.data.listener;

import java.util.EventListener;

import irt.data.packet.Packet;

public interface PacketListener extends EventListener{

	void onPacketRecived(Packet packet);

}
