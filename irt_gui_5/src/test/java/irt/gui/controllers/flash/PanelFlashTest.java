
package irt.gui.controllers.flash;

import static org.junit.Assert.*;

import org.junit.Test;

import irt.gui.data.ToHex;

public class PanelFlashTest {

	@Test
	public void getCheckSumTest() {

		final byte checkSum = PanelFlash.getCheckSum(new byte[]{0,0,0,10});
		final String bytesToHex = ToHex.bytesToHex( checkSum);

		System.out.println(bytesToHex);
		assertEquals(10,  checkSum);
	}

}
