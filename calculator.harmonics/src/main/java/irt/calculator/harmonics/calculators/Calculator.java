package irt.calculator.harmonics.calculators;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

public interface Calculator {

	public final static int HUMBER_OF_HARMONICS = 7;
	public final static NumberFormat NUMBER_FORMAT = new DecimalFormat("#0.0##### MHz");

	String calculate();

	static String format(String value) {

		if(!Optional.ofNullable(value).filter(v->!v.isEmpty()).isPresent())
			return "N/A";

		return NUMBER_FORMAT.format(new BigDecimal(value));
	}
}
