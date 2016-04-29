
package irt.gui.flash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import irt.gui.controllers.flash.ButtonRead;

public class ButtonReadTest {

	@Test
	public void removeFFTest() {
		byte[] a = new byte[]{0x79, 0x39, 0x3A, 0x35, 0x33, 0x3A, 0x30, (byte)0xFF, 0x2E, 0x36, 0x34, 0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
		final byte[] b = ButtonRead.removeFF(a);

		assertNotEquals(a.length, b.length);
		assertEquals(12, b.length);
	}

}
