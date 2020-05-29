package irt.calculator.harmonics.calculators;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import irt.calculator.harmonics.beans.Frequency;
import irt.calculator.harmonics.beans.Harmonic;
import irt.calculator.harmonics.beans.Harmonics;

public class SpurSourceCalculator implements Calculator {

	public final static String TEMPLATE = "Task:\n" +
											"Spurious: %s\n" +
											"Input: %s\n" +
											"LO1: %s\n" +
											"LO2: %s\n\n" +
											"result:\n" +
											"%s";

	private final Harmonics harmonics = new Harmonics();

	private final String spur;
	private final String input;
	private final String lo1;
	private final String lo2;

	public SpurSourceCalculator(String spur, String input, String lo1, String lo2) {

		this.spur = Optional.ofNullable(spur).map(String::trim).orElse(null);

		final Optional<String> oInput = Optional.ofNullable(input).map(String::trim);
		oInput.ifPresent(frequency->harmonics.add("Input", frequency));
		this.input = oInput.orElse(null);

		final Optional<String> oLo1 = Optional.ofNullable(lo1).map(String::trim);
		oLo1.ifPresent(frequency->harmonics.add("LO1", frequency));
		this.lo1 = oLo1.orElse(null);

		final Optional<String> oLo2 = Optional.ofNullable(lo2).map(String::trim);
		oLo2.ifPresent(frequency->harmonics.add("LO2", frequency));
		this.lo2 = oLo2.orElse(null);
	}

	@Override
	public String calculate() {

		final Set<Harmonic> allHarmonics = harmonics.getAllHarmonics(Calculator.HUMBER_OF_HARMONICS);
		final BigDecimal lookFor = Frequency.toBigDecimal(spur);

		final String result = allHarmonics.parallelStream().map(fr->fr.getFrequency()).filter(fr->fr.getFrequency().equals(lookFor)).sorted().map(Object::toString).collect(Collectors.joining("\n"));

		return String.format(TEMPLATE, Calculator.format(spur), Calculator.format(input), Calculator.format(lo1), Calculator.format(lo2), result);
	}
}
