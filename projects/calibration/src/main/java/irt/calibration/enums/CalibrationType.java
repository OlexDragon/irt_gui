package irt.calibration.enums;

import java.math.BigDecimal;
import java.util.function.BiPredicate;

import irt.data.IrtGuiProperties;

public enum CalibrationType {

	INPUT_PORER	("power.input", 	"power.dbm",	"in-power-lut-", (a, b)->new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()))>0),
	OUTPUT_POWER("power.output",	"power.dbm",	"out-power-lut-", (a, b)->false),
	GAIN		("gain", 			"gain.db", 		"gain-lut-", (a, b)->false);

	
	private final String bundle;
	private final String valueName;
	private final String tableName;
	private final BiPredicate<Number, Number> predicate;

	private CalibrationType(String bundle, String valueName, String tableName, BiPredicate<Number, Number> predicate){
		this.bundle = bundle;
		this.valueName = valueName;
		this.tableName = tableName;
		this.predicate = predicate;
	}

	public String getTitle(){
		return IrtGuiProperties.BUNDLE.getString(bundle);
	}

	public String getValueName(){
		return IrtGuiProperties.BUNDLE.getString(valueName);
	}

	public String getTableName() {
		return tableName;
	}

	public BiPredicate<Number, Number> getPredicate() {
		return predicate;
	}
}
