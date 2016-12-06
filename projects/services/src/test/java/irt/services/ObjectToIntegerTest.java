
package irt.services;

import static org.junit.Assert.*;

import java.util.InputMismatchException;

import org.junit.Test;

public class ObjectToIntegerTest {

	@Test
	public void test() {
		final ObjectToInteger o = new ObjectToInteger();

		assertNull( o.setValue(111));	// Returns old value 
		assertEquals( 111, (int)o.getValue());

		int oldValue = o.setValue("123");
		assertEquals( 111, oldValue);
		assertEquals( 123, (int)o.getValue());

		oldValue = o.setValue("Trgfgd23 ghjuy");
		assertEquals( 123, oldValue);
		assertEquals( 23, (int)o.getValue());
	}

	@Test(expected=InputMismatchException.class)
	public void exceptionTest() {

		final ObjectToInteger o = new ObjectToInteger(5, 25);
		o.setValue(111);
	}
}
