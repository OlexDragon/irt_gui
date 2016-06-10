package irt.gui.controllers.calibration.tools;

import java.util.Observer;

public interface Tool {

	void get(Commands command, Observer observer);
	void set(Commands command, Object valueToSend, Observer observer);

	public enum Commands{
		GET,
		INPUT,
		OUTPUT,
		POWER,
		FREQUENCY
	}
}
