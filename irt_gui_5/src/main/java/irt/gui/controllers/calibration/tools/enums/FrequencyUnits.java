package irt.gui.controllers.calibration.tools.enums;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

public enum FrequencyUnits {

	GHZ(1000000000),
	MHZ(1000000),
	KHZ(1000),
	HZ(1);

	private int divider;
	private FrequencyUnits(int divider){
		this.divider = divider;
	}

	public final static NumberFormat FORMAT = new DecimalFormat("#0.#########");

	public static String toString(double value){
		FrequencyUnits unit = Arrays
								.stream(FrequencyUnits.values())
								.filter(u->u!=GHZ)		//signal generator 8648A/B/C/D... has MHz as max unit
								.filter(fu->(value%fu.divider) == 0)
								.findFirst()
								.orElse(HZ);

		double d = value/unit.divider;

		return FORMAT.format(d) + " " + unit;
	}

	public String valueOf(double value){

		double d = value*divider;
		return toString(d);
	}
}
