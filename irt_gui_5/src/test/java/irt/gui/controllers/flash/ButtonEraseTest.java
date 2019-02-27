package irt.gui.controllers.flash;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import irt.gui.controllers.flash.enums.UnitAddress;

public class ButtonEraseTest {

	@Test
	public void getPagesToExtendedEraseTest() {
		byte[] pagesToErase = ButtonErase.getPagesToExtendedErase(UnitAddress.CONVERTER.getAddr(), 10 * ButtonErase.KB);
		System.out.println(Arrays.toString(pagesToErase));
		assertEquals(4, pagesToErase.length);
		assertEquals(0, pagesToErase[0]);
		assertEquals(0, pagesToErase[1]);
		assertEquals(0, pagesToErase[2]);
		assertEquals(10, pagesToErase[3]);

		pagesToErase = ButtonErase.getPagesToExtendedErase(UnitAddress.BIAS.getAddr(), 1 * ButtonErase.KB);
		System.out.println(Arrays.toString(pagesToErase));
		assertEquals(4, pagesToErase.length);
		assertEquals(0, pagesToErase[0]);
		assertEquals(0, pagesToErase[1]);
		assertEquals(0, pagesToErase[2]);
		assertEquals(11, pagesToErase[3]);

		pagesToErase = ButtonErase.getPagesToExtendedErase(UnitAddress.BIAS.getAddr(), 129 * ButtonErase.KB);
		System.out.println(Arrays.toString(pagesToErase));
		assertEquals(6, pagesToErase.length);
		assertEquals(0, pagesToErase[0]);
		assertEquals(1, pagesToErase[1]);
		assertEquals(0, pagesToErase[2]);
		assertEquals(11, pagesToErase[3]);
		assertEquals(0, pagesToErase[4]);
		assertEquals(12, pagesToErase[5]);
	}

}
