
package irt.gui.controllers.calibration.process;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import irt.gui.controllers.calibration.enums.Calibration;

public class PowerCalibrationTest {

	@Test
	public void test() {
		final PowerCalibration pc = new PowerCalibration();
		final ArrayList<Calibration> calibrations = new ArrayList<>();

		calibrations.add(Calibration.GAIN);
		assertFalse(pc.setMode(calibrations));

		calibrations.add(Calibration.INPUT_POWER);
		assertFalse(pc.setMode(calibrations));

		calibrations.add(Calibration.OUTPUT_POWER);
		assertFalse(pc.setMode(calibrations));

		calibrations.remove(Calibration.GAIN);
		assertTrue(pc.setMode(calibrations));

		calibrations.remove(Calibration.INPUT_POWER);
		assertTrue(pc.setMode(calibrations));

		calibrations.remove(Calibration.OUTPUT_POWER);
		assertFalse(pc.setMode(calibrations));
	}

}
