package irt.calculator.harmonics.beans;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
		final Set<Frequency> allfrequency = IntStream.rangeClosed(0, harmonics)

				.mapToObj(
						harmonic->
						this.frequencies.parallelStream()
						.map(fr->fr.getHarmonic(harmonic))
						.collect(Collectors.toList()))
				.flatMap(List::stream)
				.collect(Collectors.toSet());

		// Negative frequencies
		final List<Frequency> negative = allfrequency.parallelStream().map(fr->new Frequency(fr.getInitialName(), fr.getFirstHarmonic().multiply(BigDecimal.valueOf(-1))).getHarmonic(fr.getHarmonic())).collect(Collectors.toList());

		allfrequency.addAll(negative);

		Set<Harmonic> collector = allfrequency.parallelStream().filter(fr->fr.getHarmonic()>0).map(Harmonic::new).collect(Collectors.toSet());

		getHarmonics(allfrequency, collector);

		return new TreeSet<Harmonic>(collector);
	}

	private void getHarmonics(Set<Frequency> allfrequency, Set<Harmonic> collector) {

		final int size = collector.size();

		final Set<Harmonic> newHarmonics = allfrequency.parallelStream().flatMap(fr->collector.stream().map(h->h.getNewHarmonic(fr))).collect(Collectors.toSet());
		collector.addAll(newHarmonics);

		if(size<collector.size())
			getHarmonics(allfrequency, collector);
	}
}
