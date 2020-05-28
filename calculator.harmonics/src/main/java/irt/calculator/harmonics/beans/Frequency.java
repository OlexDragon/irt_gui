package irt.calculator.harmonics.beans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
		return frequency.multiply(BigDecimal.valueOf(harmonic));
	}

	public String getInitialName() {
		return name;
	}

	public String getName() {

		String result;

		if(harmonic>1) 
			result = String.format("(%s x %d)", name, harmonic);
		else
			result = name;

		return result;
	}

	@Override
	public int compareTo(Frequency frequency) {

		int compareTo = name.compareTo(frequency.name);

		if(compareTo==0) 
			compareTo = this.getFrequency().compareTo(frequency.getFrequency());

		return compareTo;
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
