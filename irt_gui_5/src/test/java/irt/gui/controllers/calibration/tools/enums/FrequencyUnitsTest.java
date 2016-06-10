
package irt.gui.controllers.calibration.tools.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class FrequencyUnitsTest {

	@Test
	public void test() {
		assertEquals("1 HZ", FrequencyUnits.HZ.valueOf(1));
		assertEquals("1 KHZ", FrequencyUnits.KHZ.valueOf(1));
		assertEquals("1 MHZ", FrequencyUnits.MHZ.valueOf(1));
		assertEquals("1000 MHZ", FrequencyUnits.GHZ.valueOf(1));
	}
}
