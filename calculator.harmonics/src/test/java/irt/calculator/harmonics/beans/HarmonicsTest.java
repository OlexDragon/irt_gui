package irt.calculator.harmonics.beans;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class HarmonicsTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	public void oneCurrierTest() {

		final Harmonics harmonics = new Harmonics();

		Set<Harmonic> allHarmonics = harmonics.getAllHarmonics(10);

		assertEquals(0, allHarmonics.size());

		harmonics.add("Input", "950");
		allHarmonics = harmonics.getAllHarmonics(5);
		logger.error(allHarmonics);

		assertEquals(5, allHarmonics.size());

		// This frequency already exists. This should not be added.
		harmonics.add("Input", "950");
		allHarmonics = harmonics.getAllHarmonics(5);
		logger.error(allHarmonics);

		assertEquals(5, allHarmonics.size());

		// This frequency Name already exists. This should not be added.
		harmonics.add("Input", "4900");
		allHarmonics = harmonics.getAllHarmonics(5);

		assertEquals(5, allHarmonics.size());

		harmonics.add("LO", "4900");
		allHarmonics = harmonics.getAllHarmonics(1);
		logger.error(allHarmonics);

		assertEquals(4, allHarmonics.size());
	}

	@Test
	public void dualCurrierTest() {

		final Harmonics harmonics = new Harmonics();
		harmonics.add("Input", "950");
		harmonics.add("LO", "4900");

		final Set<Harmonic> allHarmonics = harmonics.getAllHarmonics(2);
		logger.error(allHarmonics);

		assertEquals(12, allHarmonics.size());
	}

	@Test
	public void tripleCurrierTest() {

		final Harmonics harmonics = new Harmonics();
		harmonics.add("Input", "950");
		harmonics.add("LO1", "4900");
		harmonics.add("LO2", "6000");

		Set<Harmonic> allHarmonics = harmonics.getAllHarmonics(1);

		assertEquals(13, allHarmonics.size());

		allHarmonics = harmonics.getAllHarmonics(2);
		logger.error(allHarmonics);

		assertEquals(62, allHarmonics.size());
	}
}
