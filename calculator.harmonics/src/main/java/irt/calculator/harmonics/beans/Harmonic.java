package irt.calculator.harmonics.beans;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public class Harmonic implements Comparable<Harmonic>{

	private final Set<Frequency> frequencies = new TreeSet<>();

	public Harmonic(Frequency frequency) {
		add(frequency);
	}

	public List<String> getFrequencyNames() {
		return frequencies.parallelStream().map(Frequency::getName).distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * @param frequency
	 * @return true if frequencies been added
	 */
	public boolean add(Frequency frequency) {

		if(frequency.getHarmonic()==0)
			return false;

		final boolean present = frequencies.parallelStream().map(Frequency::getInitialName).filter(name->name.equals(frequency.getInitialName())).findAny().isPresent();

		if(present)
			return false;

		return frequencies.add(frequency);
	}

	public boolean add(String name, double frequency) {
		return add(new Frequency(name, frequency));
	}

	public Frequency getFrequency(){

		StringBuffer sb = new StringBuffer();

		double sum = getSum(sb, frequencies.stream());

		if(Double.compare(sum, 0)<0) {
			sb.setLength(0);
			sum = getSum(sb, frequencies.stream().map(fr->new Frequency(fr.getInitialName(), fr.getFirstHarmonic().multiply(Frequency.toBigDecimal(-1))).getHarmonic(fr.getHarmonic())));
		}

		return new Frequency(sb.toString(), sum);
	}

	private double getSum(StringBuffer sb, final Stream<Frequency> stream) {

		return stream.sorted()

				.mapToDouble(
						fr->{
							final BigDecimal frequency = fr.getFrequency();
							sb.append(frequency.compareTo(BigDecimal.ZERO)<0 ? "-" : "+").append(fr.getName());
							return frequency.doubleValue();
						})
				.sum();
	}

	public Harmonic getNewHarmonic(Frequency fr) {

		final Harmonic harmonic = new Harmonic(fr);
		frequencies.forEach(harmonic::add);

		return harmonic;
	}

	public int size() {
		return frequencies.size();
	}

	@Override
	public int compareTo(Harmonic harmonic) {

		final String names = frequencies.parallelStream().map(Frequency::getInitialName).distinct().sorted().collect(Collectors.joining(" "));
		final int compareTo = names.compareTo(harmonic.frequencies.parallelStream().map(Frequency::getInitialName).distinct().sorted().collect(Collectors.joining(" ")));

		if(compareTo!=0)
			return compareTo;
		final Frequency frequency1 = getFrequency();
		final Frequency frequency2 = harmonic.getFrequency();

		return frequency1.getFrequency().compareTo(frequency2.getFrequency());
	}

	@Override
	public int hashCode() {
		
		return getFrequency().hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		return obj.hashCode() == hashCode();
	}

	@Override
	public String toString() {
		return "Harmonic = " + getFrequency().toString();
	}
}
