
package irt.data.prologix;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrologixCommandsTest {

	@Test public void addrTest() {

		assertNull(PrologixCommands.ADDR.getValue());
		assertNull(PrologixCommands.ADDR.getOldValue());

		PrologixCommands.ADDR.setValue("19");
		String string = PrologixCommands.ADDR.toString();

		assertEquals("command=++addr; value=19; oldValue=null", string);
		assertEquals(19, PrologixCommands.ADDR.getValue());
		assertNull(PrologixCommands.ADDR.getOldValue());

		PrologixCommands.ADDR.setValue("17");
		string = PrologixCommands.ADDR.toString();

		assertEquals("command=++addr; value=17; oldValue=null", string);
		assertEquals(17, PrologixCommands.ADDR.getValue());
		assertNull(PrologixCommands.ADDR.getOldValue());

		final byte[] command = PrologixCommands.ADDR.getCommand();
		string = PrologixCommands.ADDR.toString();

		assertEquals("++addr 17\n", new String(command));
		assertEquals("command=++addr; value=null; oldValue=17", string);
		assertNull(PrologixCommands.ADDR.getValue());
		assertEquals(17, PrologixCommands.ADDR.getOldValue());
	}

}
