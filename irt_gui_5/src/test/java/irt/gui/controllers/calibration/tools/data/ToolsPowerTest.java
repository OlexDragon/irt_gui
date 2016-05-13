
package irt.gui.controllers.calibration.tools.data;

import static org.junit.Assert.*;

import org.junit.Test;

import irt.gui.controllers.calibration.tools.enums.PowerUnits;

public class ToolsPowerTest {

	@Test
	public void doubleTest() {
		ToolsPower tolls = new ToolsPower(1, null);
		assertEquals("1 DBM", tolls.getValue());

		tolls = new ToolsPower(0.1, null);
		assertEquals("0.1 DBM", tolls.getValue());

		tolls = new ToolsPower(0.1, PowerUnits.DB);
		assertEquals("0.1 DB", tolls.getValue());
	}

	@Test
	public void stringTest() {
		ToolsPower tools = new ToolsPower("0");
		assertEquals("0 DBM", tools.getValue());

		tools = new ToolsPower("1.0 DB");
		assertEquals("1 DB", tools.getValue());

		tools = new ToolsPower("1.0");
		assertEquals("1 DBM", tools.getValue());

		tools = new ToolsPower("0.1 DBM");
		assertEquals("0.1 DBM", tools.getValue());

		tools = new ToolsPower("0.1DBM");
		assertEquals("0.1 DBM", tools.getValue());
	}
}
