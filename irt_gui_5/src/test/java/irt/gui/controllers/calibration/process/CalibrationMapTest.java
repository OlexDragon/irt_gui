
package irt.gui.controllers.calibration.process;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.junit.Test;

public class CalibrationMapTest {

	@Test
	public void developmentTest() {
		//linear
		double x1 = -51;
		double y1 = 500;
		double x2 = -47;
		double y2 = 1000;
		double y3 = 1500;

		LogManager.getLogger().trace("x2-x1={}; y2-y1={}; {} : {}", x2-x1, y2-y1, (y2-y1)/(x2-x1), x1 + (y3-y1)/((y2-y1)/(x2-x1)));
		assertEquals(-43.0,x1 + ( y3-y1)/((y2-y1)/(x2-x1)), 0.0001);
	}

	@Test
	public void test() {
		CalibrationMap map = new CalibrationMap();
		map.put(1, 11);
		map.put(2, 22);
		map.put(3, 2);
		map.put(4, 7);
		map.put(5, -52);

		assertEquals(1, map.size());
		assertEquals(22, map.get(5));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void test2() {
		CalibrationMap map = new CalibrationMap();
		map.put(1, 11);
		map.put(2, 22);
		map.put(3, 23);
		map.put(4, 24);
		map.put(5, 52);
		map.put(6, 53);
		map.put(0, 22);
	}

	@Test
	public void getWithTest1() {
		CalibrationMap map = new CalibrationMap();
		map.put(1, 11);
		map.put(2, 12);
		map.put(3, 13);
		map.put(4, 14);
		map.put(5, 15);

		CalibrationMap m = map.getWith(0);
		assertEquals(5, m.size());
	}

	@Test
	public void getWithTest2() {
		CalibrationMap map = new CalibrationMap();
		map.put(1, 100);
		map.put(2, 200);
		map.put(3, 349);
		map.put(4, 351);
		map.put(5, 500);

		map = map.getWith(.5);
		assertEquals(2, map.size());
		assertNotNull(map.get(1));
		assertNotNull(map.get(5));
	}

	@Test
	public void getWithTest3() {
		CalibrationMap map = new CalibrationMap();
		map.put(1, 11);
		map.put(2, 12);
		map.put(3, 13);
		map.put(4, 14);
		map.put(5, 15);
		map.put(15, 15);

		map = map.getWith(1);
		assertEquals(1, map.size());
		assertNotNull(map.get(15));
	}

	@Test
	public void getWithTest4() {
		CalibrationMap map = new CalibrationMap();
		map.put(-60, 823);
		map.put(-54, 829);
		map.put(-53, 835);
		map.put(-52, 838);
		map.put(-50, 850);
		map.put(-49, 858);
		map.put(-48, 868);
		map.put(-46, 895);
		map.put(-45, 913);
		map.put(-44, 937);
		map.put(-43, 958);
		map.put(-42, 988);
		map.put(-41, 1015);
		map.put(40, 1062);

		map = map.getWith(.5);
		assertEquals(10, map.size());
	}
}
