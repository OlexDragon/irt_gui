package irt.data.packets.interfaces;

import irt.data.packets.PacketParsingException;

public interface ScheduledNode {

	String getPropertyName();

	void stop(boolean b);
	void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException;
}
