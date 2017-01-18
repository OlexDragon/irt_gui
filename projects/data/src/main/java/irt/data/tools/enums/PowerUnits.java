package irt.data.tools.enums;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public enum PowerUnits {
	DBM,
	DB;

	private final static NumberFormat FORMAT = new DecimalFormat("#0.#");

	public String valueOf(double value){

		return FORMAT.format(value) + " " + name();
	}

	public static String toString(byte[] values) {

		if(values==null)
			return null;

		String str = new String(values);
		final double parseDouble = Double.parseDouble(str);
		return FORMAT.format(parseDouble) + " dBm";
	}
}
