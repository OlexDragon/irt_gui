
package org.data;

import static org.junit.Assert.*;

import org.junit.Test;

import irt.data.StringData;

public class StringDataTest {

	@Test
	public void testForNull() {
		final StringData sd = new StringData(null);
		assertEquals("N/A", sd.toString());
	}

	@Test
	public void test() {
		final StringData sd = new StringData((byte)84, (byte)101, (byte)115, (byte)116);

		assertEquals('T', 84);
		assertEquals('e', 101);
		assertEquals('s', 115);
		assertEquals('t', 116);
		assertEquals("Test", sd.toString());
	}
}
