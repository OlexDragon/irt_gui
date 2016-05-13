
package irt.gui.controllers.calibration.tools.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToolsFrequencyTest {

	@Test
	public void doubleTest() {
		ToolsFrequency fr = new ToolsFrequency(1);
		assertEquals("1 HZ", fr.getValue());

		fr = new ToolsFrequency(1000);
		assertEquals("1 KHZ", fr.getValue());

		fr = new ToolsFrequency(1000000);
		assertEquals("1 MHZ", fr.getValue());

		fr = new ToolsFrequency(1000000000);
		assertEquals("1000 MHZ", fr.getValue());

		fr = new ToolsFrequency(1000500000);
		assertEquals("1000500 KHZ", fr.getValue());

		fr = new ToolsFrequency(1000500001);
		assertEquals("1000500001 HZ", fr.getValue());
	}

	@Test
	public void stringTest() {
		ToolsFrequency fr = new ToolsFrequency("1.0 GHz");
		assertEquals("1000 MHZ", fr.getValue());

		fr = new ToolsFrequency("1m");
		assertEquals("1 MHZ", fr.getValue());

		fr = new ToolsFrequency("1khz");
		assertEquals("1 KHZ", fr.getValue());

		fr = new ToolsFrequency("1 HZ");
		assertEquals("1 HZ", fr.getValue());

		fr = new ToolsFrequency("1000 k");
		assertEquals("1 MHZ", fr.getValue());

		fr = new ToolsFrequency("1000500001");
		assertEquals("1000500001 HZ", fr.getValue());
	}
}
