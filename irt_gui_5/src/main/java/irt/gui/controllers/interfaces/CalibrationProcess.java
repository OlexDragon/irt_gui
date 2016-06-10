package irt.gui.controllers.interfaces;

import java.util.List;

import irt.gui.controllers.calibration.enums.Calibration;
import irt.gui.controllers.calibration.tools.Tool;

public interface CalibrationProcess {

	boolean isCompleted();
	boolean inProgress();
	boolean setMode(List<Calibration> calibrations);
	void start(List<Tool> tools, double stepDb, int steps);
	void stop();
	void setStopAction(Runnable stopAction);
}
