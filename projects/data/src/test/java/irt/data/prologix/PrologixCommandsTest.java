
package irt.data.prologix;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrologixCommandsTest {

	@Test public void addrTest() {

		final PrologixCommands addr = PrologixCommands.ADDR;

		assertNull(addr.getValue());
		assertNull(addr.getOldValue());

		addr.setValue("19");
		String string = addr.toString();

		assertEquals("command=++addr; value=19; oldValue=null", string);
		assertEquals(19, addr.getValue());
		assertNull(addr.getOldValue());

		addr.setValue("17");
		string = addr.toString();

		assertEquals("command=++addr; value=17; oldValue=null", string);
		assertEquals(17, addr.getValue());
		assertNull(addr.getOldValue());

		final byte[] command = addr.getCommand();
		string = addr.toString();

		assertEquals("++addr 17\n", new String(command));
		assertEquals("command=++addr; value=null; oldValue=17", string);
		assertNull(addr.getValue());
		assertEquals(17, addr.getOldValue());
	}

	@Test public void savecfgTest() {

		final PrologixCommands savecfg = PrologixCommands.SAVECFG;

		assertNull(savecfg.getValue());
		assertNull(savecfg.getOldValue());

		savecfg.setValue(null);
		assertNull(savecfg.getValue());
		assertNull(savecfg.getOldValue());

		savecfg.getCommand();
		assertNull(savecfg.getValue());
		assertNull(savecfg.getOldValue());
	}
}
