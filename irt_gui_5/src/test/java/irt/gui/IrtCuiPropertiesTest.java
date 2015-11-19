
package irt.gui;

import static org.junit.Assert.*;

import org.junit.Test;

public class IrtCuiPropertiesTest {

	@Test
	public void test() {
		System.out.println(IrtCuiProperties.getProperty("gui.panel.bias.css"));
		assertEquals("test", IrtCuiProperties.getProperty("test"));
	}

}
