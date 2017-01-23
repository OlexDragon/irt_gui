package irt.data.tools.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class Commands83640BTest {

	@Test
	public void test() {
		byte[] command = Commands83640B.FREQUENCY.getCommand();
		assertEquals("FREQ:CW 950MHz\n", new String(command));
	}

}
