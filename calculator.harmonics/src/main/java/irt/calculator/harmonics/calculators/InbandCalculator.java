package irt.calculator.harmonics.calculators;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import irt.calculator.harmonics.beans.Frequency;
import irt.calculator.harmonics.beans.Harmonic;
import irt.calculator.harmonics.beans.Harmonics;

public class InbandCalculator implements Calculator {

	public final static String TEMPLATE = "Task:\n" +
											"Input: %s\n" +
											"LO1: %s\n" +
											"LO2: %s\n" +
											"Output: %s - %s\n\n" +
											"result:\n" +
											"%s";

	private final String input;
	private final String outStart;
	private final String outStop;
	private final String lo1;
	private final String lo2;

	private final Harmonics harmonics = new Harmonics();

	public InbandCalculator(String input, String outStart, String outStop, String lo1, String lo2) {

		this.outStart = Optional.ofNullable(outStart).map(String::trim).orElse(null);
		this.outStop = Optional.ofNullable(outStop).map(String::trim).orElse(null);

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

		String result;
		if(outStart==null || outStop==null)
			result = "Have to enter the output frequency.";

			else {
				final BigDecimal lookFrom = Frequency.toBigDecimal(outStart);
				final BigDecimal lookTo = Frequency.toBigDecimal(outStop);
				result = allHarmonics.parallelStream().map(fr->fr.getFrequency()).filter(fr->fr.getFrequency().compareTo(lookFrom)>=0 && fr.getFrequency().compareTo(lookTo)<=0).map(Object::toString).collect(Collectors.joining("\n"));
			}

		return String.format(TEMPLATE, Calculator.format(input), Calculator.format(lo1), Calculator.format(lo2), Calculator.format(outStart), Calculator.format(outStop), result);
	}
}
