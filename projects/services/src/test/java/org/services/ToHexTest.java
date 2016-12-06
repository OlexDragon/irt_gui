
package org.services;

import static org.junit.Assert.*;

import org.junit.Test;

import irt.services.ToHex;

public class ToHexTest {

	@Test
	public void test() {
		final String bytesToHex = ToHex.bytesToHex((byte)1, (byte)2, (byte)3, (byte)188 , (byte)255);

		assertEquals(0xBC, 188);
		assertEquals(0xFF, 255);
		assertEquals("01 02 03 BC FF ", bytesToHex);
	}

}
