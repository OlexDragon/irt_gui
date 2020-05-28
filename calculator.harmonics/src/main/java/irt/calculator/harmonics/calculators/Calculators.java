package irt.calculator.harmonics.calculators;

import java.util.Map;

public class Calculators {

	public static Calculator getCalculator(Map<String, String> map) {

		final String spur = map.get("tfSpuriousFrequency");
		final String input = map.get("tfInputStart");
		final String lo1 = map.get("tfLo1Start");
		final String lo2 = map.get("tfLo2Start");

		if(spur!=null)
			return new SpurSourceCalculator(spur, input, lo1, lo2);

		final String outStart = map.get("tfOutputStart");
		final String outStop = map.get("tfOutputStop");

		final String inputStop = map.get("tfInputStop");
		final String lo1Stop = map.get("tfLo1Stop");
		final String lo2Stop = map.get("tfLo2Stop");

		if(inputStop==null && lo1Stop==null && lo2Stop==null)
			return new InbandCalculator(input, outStart, outStop, lo1, lo2);

		return new RangeCalculator();
	}
}
