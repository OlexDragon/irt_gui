package irt.calculator.harmonics.beans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @EqualsAndHashCode
public class Frequency implements Comparable<Frequency>{

	private final static NumberFormat formatter = new DecimalFormat("#0.0#####"); 

	private final String name;
	private final BigDecimal frequency;
	private final int harmonic;

	private Frequency(String name, BigDecimal frequency, int harmonic) {

		if(harmonic<0)
			throw new IllegalArgumentException("The harmonic value must be greater than 0. Your value is " + harmonic);

		this.name = name;
		this.frequency = frequency.setScale(6, RoundingMode.HALF_UP);
		this.harmonic = harmonic;
	}

	public Frequency(String name, BigDecimal frequency) {
		this(name, frequency, 1);
	}

	public Frequency(String name, double frequency) {
		this(name, BigDecimal.valueOf(frequency));
	}

	public Frequency(String name, String frequency) {
		this(name, new BigDecimal(frequency));
	}

	public BigDecimal getFirstHarmonic() {
		return frequency;
	}

	public Frequency getHarmonic(int harmonic) {
		return new Frequency(name, frequency, harmonic);
	}

	public BigDecimal getFrequency() {

		return frequency.multiply(toBigDecimal(harmonic));
	}

	public String getInitialName() {
		return name;
	}

	public String getName() {

		return Optional.of(harmonic).filter(h->h>1).map(h->String.format("(%s x %d)", name, h)).orElse(name);
	}

	@Override
	public int compareTo(Frequency frequency) {

		final BigDecimal frThis = this.getFrequency();
		final BigDecimal frOther = frequency.getFrequency();

		int compareTo = frThis.compareTo(frOther);

		if(compareTo!=0) 
			return compareTo;

		return name.compareTo(frequency.name);
	}

	@Override
	public String toString() {

		return getName() + ": " + formatter.format(getFrequency()) + " MHz(" + formatter.format(frequency) + ")";
	}

	public static BigDecimal toBigDecimal(String frequency) {

		if(frequency==null)
			return null;

		return new BigDecimal(frequency).setScale(6, RoundingMode.HALF_UP);
	}

	public static BigDecimal toBigDecimal(double frequency) {
		return toBigDecimal(Double.toString(frequency));
	}
}
