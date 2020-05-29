package irt.calculator.harmonics.beans;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Harmonics {

	private final Set<Frequency> frequencies = new TreeSet<>();

	public boolean add(String name, String frequency) {

		// Do not add if name exists
		if(frequencies.parallelStream().filter(fr->fr.getName().equals(name)).findAny().isPresent())
			return false;

		return frequencies.add(new Frequency(name, frequency));
	}

	public Set<Harmonic> getAllHarmonics(int harmonics){

		// Create a sets of all harmonic of the frequencies
		final Set<Frequency> allfrequency = IntStream.rangeClosed(0, harmonics).parallel()

				.mapToObj(
						harmonic->
						this.frequencies.stream()
						.flatMap(fr->Stream.of(fr.getHarmonic(harmonic), new Frequency(fr.getInitialName(), fr.getFirstHarmonic().multiply(Frequency.toBigDecimal(-1))).getHarmonic(harmonic)))
						.collect(Collectors.toList()))
				.flatMap(List::stream)
				.collect(Collectors.toSet());

		Set<Harmonic> collector = allfrequency.parallelStream().filter(fr->fr.getHarmonic()>0).map(Harmonic::new).collect(Collectors.toSet());

		getHarmonics(allfrequency, collector);

		return collector;
	}

	private void getHarmonics(Set<Frequency> allfrequency, Set<Harmonic> collector) {

		final Set<Harmonic> newHarmonics = allfrequency.parallelStream().flatMap(fr->collector.parallelStream().map(h->h.getNewHarmonic(fr))).collect(Collectors.toSet());

		if(collector.addAll(newHarmonics))
			getHarmonics(allfrequency, collector);
	}
}
