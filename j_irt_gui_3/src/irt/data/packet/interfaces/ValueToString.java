package irt.data.packet.interfaces;

public interface ValueToString extends LinkedPacket, PacketWork, PacketThreadWorker{

	String valueToString();
	String valueToString(Number value);
}
