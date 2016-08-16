package irt.gui.controllers.calibration.tools;

import java.util.Observer;
import java.util.concurrent.Future;

public interface Tool {

	void get(Commands command, Observer observer);
	void set(Commands command, Object valueToSend);
	void set(Commands command, Object valueToSend, Observer observer);
	<T>Future<T> get(Commands command);

	public enum Commands{
		GET,
		INPUT,
		OUTPUT,
		POWER,
		FREQUENCY
	}
}
