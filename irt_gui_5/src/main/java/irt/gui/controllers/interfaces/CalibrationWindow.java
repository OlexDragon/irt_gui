package irt.gui.controllers.interfaces;

import java.util.Observer;

public interface CalibrationWindow extends Observer {

	void setCloseMethod(Runnable closeMethod);

}
