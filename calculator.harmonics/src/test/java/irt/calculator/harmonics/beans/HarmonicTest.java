package irt.calculator.harmonics.beans;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class HarmonicTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	public void test1() {
		final String name = "name";
		final double frequency = 5.6;
		final Harmonic harmonic = new Harmonic(new Frequency(name, frequency));

		assertEquals(new Frequency("+" + name, frequency), harmonic.getFrequency());
	}

	@Test
	public void positiveFrequencyTest() {

		final String name = "first";
		final double frequency = 5.6;
		final double frequency2 = 1.7;
		final int h = 4;

		final Harmonic harmonic = new Harmonic(new Frequency(name, frequency));
		harmonic.add(new Frequency("second", frequency2).getHarmonic(h));

		final Frequency fr = harmonic.getFrequency();
		logger.error(fr);

		final double d = (frequency+(frequency2*h));
		assertEquals(new Frequency("+first+(second x 4)", d), fr);
		assertEquals(BigDecimal.valueOf(d).setScale(6, RoundingMode.HALF_UP), fr.getFrequency());
	}

	@Test
	public void negativeFrequencyTest() {

		final String name = "first";
		final double frequency = 5.6;
		final double frequency2 = -1.7;
		final int h = 4;

		final Harmonic harmonic = new Harmonic(new Frequency(name, frequency));
		harmonic.add(new Frequency("second", frequency2).getHarmonic(h));

		final Frequency fr = harmonic.getFrequency();
		logger.error(fr);

		// positive frequency test
		final double d = (frequency+(frequency2*h)) *-1;
		assertEquals(new Frequency("-first+(second x 4)", d), fr);
		assertEquals(BigDecimal.valueOf(d).setScale(6, RoundingMode.HALF_UP), fr.getFrequency());
	}

	@Test
	public void compareToTest() {

		final Harmonic harmonic1 = new Harmonic(new Frequency("name", 5));
		final Harmonic harmonic2 = new Harmonic(new Frequency("name", 5));

		assertEquals(harmonic1, harmonic2);

		final Frequency frequency = new Frequency("second", 33);
		harmonic2.add(frequency);

		assertNotEquals(harmonic1, harmonic2);
		assertTrue(harmonic1.compareTo(harmonic2)<0);

		harmonic1.add(frequency);
		logger.error("\n{} : {}\n{} : {}\nequals: {}", harmonic1, harmonic1.hashCode(), harmonic2, harmonic2.hashCode(), harmonic1.equals(harmonic2));

		assertEquals(harmonic1, harmonic2);
		assertEquals(0, harmonic1.compareTo(harmonic2));

		assertFalse(harmonic1.add(frequency.getHarmonic(1)));
	}

	@Test
	public void toStringTest() {

		final Harmonic harmonic = new Harmonic(new Frequency("input", 5));
		logger.error(harmonic);

		assertEquals("Harmonic = +input: 5.0 MHz(5.0)", harmonic.toString());

		Frequency frequency = harmonic.getFrequency();
		logger.error(frequency);
		assertEquals("+input: 5.0 MHz(5.0)", frequency.toString());

		harmonic.add("second", 12.0);
		logger.error(harmonic);

		assertEquals("Harmonic = +input+second: 17.0 MHz(17.0)", harmonic.toString());

		frequency = harmonic.getFrequency();
		logger.error(frequency);
		assertEquals("+input+second: 17.0 MHz(17.0)", frequency.toString());

		final double sum = harmonic.getFrequencies().parallelStream().map(Frequency::getFrequency).mapToDouble(BigDecimal::doubleValue).sum();
		assertEquals(BigDecimal.valueOf(sum).setScale(6, RoundingMode.HALF_UP), frequency.getFrequency());
	}
}
