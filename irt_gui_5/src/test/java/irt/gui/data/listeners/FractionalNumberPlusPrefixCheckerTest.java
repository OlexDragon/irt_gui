
package irt.gui.data.listeners;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import javafx.beans.property.SimpleStringProperty;

public class FractionalNumberPlusPrefixCheckerTest {

	javafx.beans.property.StringProperty property;

	@Before
	public void setup(){
		property = new SimpleStringProperty("No");
		property.addListener(new FractionalNumberPlusPrefixChecker());
	}

	@Test
	public void test1() {
		property.set("Yes");

		assertEquals("No", property.get());
	}

	@Test
	public void test5() {
		property.set("1A23.");

		assertEquals("No", property.get());
	}

	@Test
	public void test6() {
		property.set("1A23.45.67");

		assertEquals("No", property.get());
	}

	@Test
	public void test7() {
		property.set("1A23.45.67 kHz");

		assertEquals("No", property.get());
	}

	@Test
	public void test9() {
		property.set("");

		assertEquals("No", property.get());
	}

	@Test
	public void test10() {
		property.set(null);

		assertEquals("No", property.get());
	}

	@Test
	public void test2() {
		property.set("123.456");

		assertEquals("123.456", property.get());
	}

	@Test
	public void test3() {
		property.set("123.456MHz");

		assertEquals("123.456MHz", property.get());
	}

	@Test
	public void test4() {
		property.set("123.");

		assertEquals("123.", property.get());
	}

	@Test
	public void test8() {
		property.set("123.45 kHz");

		assertEquals("123.45 kHz", property.get());
	}

	@Test
	public void test11() {
		property.set(".45 kHz");

		assertEquals(".45 kHz", property.get());
	}
}
