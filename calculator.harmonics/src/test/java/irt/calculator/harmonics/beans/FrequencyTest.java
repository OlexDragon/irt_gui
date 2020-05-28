package irt.calculator.harmonics.beans;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class FrequencyTest {
	private final Logger logger = LogManager.getLogger();

	@Test
	public void test() {

		Frequency frequency = new Frequency("name", 5);
		assertEquals("name: 5.0 MHz(5.0)", frequency.toString());
		assertEquals(new Frequency("name", 5), frequency);

		frequency = new Frequency("name", 5).getHarmonic(2);
		logger.error(frequency);
		assertEquals("(name x 2): 10.0 MHz(5.0)", frequency.toString());

		frequency = new Frequency("name", 5).getHarmonic(3);
		assertEquals("(name x 3): 15.0 MHz(5.0)", frequency.toString());

		frequency = new Frequency("name", 5).getHarmonic(4);
		assertEquals("(name x 4): 20.0 MHz(5.0)", frequency.toString());

		frequency = new Frequency("name", 5).getHarmonic(5);
		assertEquals("(name x 5): 25.0 MHz(5.0)", frequency.toString());
	}

	@Test
	public void sortTest() {

		final Frequency frequencyA = new Frequency("a", 1);
		final Frequency frequencyB = new Frequency("b", 2);
		final Frequency frequencyC = new Frequency("c", 3);
		final Frequency frequencyD = new Frequency("d", 3);
		final Frequency frequencyE = new Frequency("e", 2);
		final Frequency frequencyF = new Frequency("f", 1);

		final List<Frequency> list1 = Arrays.asList(frequencyA, frequencyA.getHarmonic(5), frequencyB, frequencyC, frequencyD, frequencyD.getHarmonic(2), frequencyE, frequencyF);
		final List<Frequency> list2 = Stream.of(frequencyF, frequencyD.getHarmonic(2), frequencyE, frequencyD, frequencyC, frequencyA.getHarmonic(5), frequencyB, frequencyA).sorted().collect(Collectors.toList());

		logger.error("\n{}\n{}", list1, list2);

		assertEquals(list1, list2);
	}
}
