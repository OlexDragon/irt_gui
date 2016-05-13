package irt.gui.controllers.calibration.tools.enums;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public enum PowerUnits {
	DBM,
	DB;

	private final static NumberFormat FORMAT = new DecimalFormat("#0.#");

	public String valueOf(double value){

		return FORMAT.format(value) + " " + name();
	}
}
