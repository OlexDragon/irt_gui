package irt.data.packet.interfaces;

import irt.data.packet.PacketWork;

public interface ValueToString extends LinkedPacket, PacketWork, PacketThreadWorker{

	String valueToString();
	String valueToString(Number value);
}
