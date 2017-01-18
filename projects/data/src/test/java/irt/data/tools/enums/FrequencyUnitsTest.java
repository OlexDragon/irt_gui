
package irt.data.tools.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class FrequencyUnitsTest {

	@Test
	public void test() {
		String valueOf = FrequencyUnits.GHZ.valueOf(5);
		assertEquals("5000 MHz", valueOf);

		valueOf = FrequencyUnits.toString(6000000000.0);
		assertEquals("6000 MHz", valueOf);

		 valueOf = FrequencyUnits.MHZ.valueOf(5);
		assertEquals("5 MHz", valueOf);

		valueOf = FrequencyUnits.toString(6000000);
		assertEquals("6 MHz", valueOf);

		 valueOf = FrequencyUnits.KHZ.valueOf(5);
		assertEquals("5 KHz", valueOf);

		valueOf = FrequencyUnits.toString(6000);
		assertEquals("6 KHz", valueOf);

		 valueOf = FrequencyUnits.HZ.valueOf(5);
		assertEquals("5 Hz", valueOf);

		valueOf = FrequencyUnits.toString(6);
		assertEquals("6 Hz", valueOf);
}

}
