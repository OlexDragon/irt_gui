
package irt.gui.controllers.calibration.process;

import java.util.List;
import java.util.Optional;

import irt.gui.controllers.calibration.enums.Calibration;
import irt.gui.controllers.interfaces.CalibrationProcess;

public class CalibrationBuilder {

	private static CalibrationProcess calibrationProcess;

	public static CalibrationProcess buildProcess(List<Calibration> calibrations){
		return Optional
				.ofNullable(calibrationProcess)
				.filter(cp->!cp.isCompleted())
				.filter(cp->{
					boolean isSet = true;
					if(!cp.inProgress())
						isSet = cp.setMode(calibrations);
					return isSet;
				})
				.orElseGet(()->{

					if(calibrations.isEmpty())
						return null;

					CalibrationProcess cp;
					switch(calibrations.get(0)){
					case GAIN:
						cp = null;
						break;
					default:
						cp = new PowerCalibration();
						cp.setMode(calibrations);
					}
					return cp;
				});
	}
}
