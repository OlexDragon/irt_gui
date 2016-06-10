package irt.gui.controllers.interfaces;

import irt.gui.errors.PacketParsingException;

public interface ScheduledNode {

	String getPropertyName();

	void stop(boolean b);
	void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException;
}
