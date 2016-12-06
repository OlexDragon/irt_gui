
package org.data;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import irt.data.DacValue;

public class DacValueTest {

	@Test
	public void test() {
		final Random r = new Random();

		final byte dn = (byte) r.nextInt(Byte.MAX_VALUE);
		final short dv = (short) r.nextInt(Short.MAX_VALUE);

		final DacValue dacValue = new DacValue(dn, dv);

		assertEquals(dn, dacValue.getDacNumber());
		assertEquals(dv, dacValue.getDacValue());
		assertEquals(dv, dacValue.getIntDacValue());

		assertTrue(Short.class.isInstance(dacValue.getDacValue()));
		assertTrue(Integer.class.isInstance(dacValue.getIntDacValue()));
	}

}
