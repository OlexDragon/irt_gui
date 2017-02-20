
package irt.services;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ObjectToBooleanTest {

	@Test
	public void test() {
		ObjectToBoolean o = new ObjectToBoolean();
		assertNull(o.getValue());
		assertNull(o.setValue(null));
	}

}
