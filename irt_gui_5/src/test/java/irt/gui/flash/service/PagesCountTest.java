
package irt.gui.flash.service;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class PagesCountTest {
	private final Logger logger = LogManager.getLogger();

	@Test(expected=NoSuchElementException.class)
	public void fileSize0Test() {
		final PagesCount supplier = new PagesCount(0);
		logger.trace("*** {} ***", supplier);
	}

	@Test(expected=NoSuchElementException.class)
	public void fileOverSizeTest() {
		final PagesCount supplier = new PagesCount(Integer.MAX_VALUE);
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void firstPageTest1() {
		final PagesCount supplier = new PagesCount(110);
		assertEquals(0, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0,0,0,0}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void firstPageTest2() {
		final PagesCount supplier = new PagesCount(16384);
		assertEquals(0, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0,0,0,0}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void secondtPageTest1() {
		final PagesCount supplier = new PagesCount(16385);
		assertEquals(1, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0, 1, 0, 0, 0, 1}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void secondPageTest2() {
		final PagesCount supplier = new PagesCount(32768);
		assertEquals(1, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0, 1, 0, 0, 0, 1}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void page24Test1() {
		final PagesCount supplier = new PagesCount(1966081);
		assertEquals(23, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0, 23, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8, 0, 9, 0, 10, 0, 11, 0, 12, 0, 13, 0, 14, 0, 15, 0, 16, 0, 17, 0, 18, 0, 19, 0, 20, 0, 21, 0, 22, 0, 23}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}

	@Test
	public void page24Test2() {
		final PagesCount supplier = new PagesCount(2097152);
		assertEquals(23, supplier.getPagesCount());
		assertArrayEquals(new byte[]{0, 23, 0, 0, 0, 1, 0, 2, 0, 3, 0, 4, 0, 5, 0, 6, 0, 7, 0, 8, 0, 9, 0, 10, 0, 11, 0, 12, 0, 13, 0, 14, 0, 15, 0, 16, 0, 17, 0, 18, 0, 19, 0, 20, 0, 21, 0, 22, 0, 23}, supplier.getPages());
		logger.trace("*** {} ***", supplier);
	}
}
