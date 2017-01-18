package irt.data.tools.enums;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

public enum FrequencyUnits{

	GHZ((int)Math.pow(10, 9), "GHz"),
	MHZ((int)Math.pow(10, 6), "MHz"),
	KHZ((int)Math.pow(10, 3), "KHz"),
	HZ(1, "Hz");

	private int divider;
	private String prefix;

	private FrequencyUnits(int divider, String prefix){
		this.divider = divider;
		this.prefix = prefix;
	}

	public final static NumberFormat FORMAT = new DecimalFormat("#0.#########");

	public static String toString(byte... values){

		if(values==null)
			return null;

		String str = new String(values);
		return toString(Double.parseDouble(str));
	}

	public static String toString(double value){
		FrequencyUnits unit = Arrays
								.stream(FrequencyUnits.values())
								.filter(u->u!=GHZ)		//signal generator 8648A/B/C/D... has MHz as max unit
								.filter(fu->(value%fu.divider) == 0)
								.findFirst()
								.orElse(HZ);

		double d = value/unit.divider;

		return FORMAT.format(d) + " " + unit.prefix;
	}

	public String valueOf(double value){

		double d = value*divider;
		return toString(d);
	}
}
