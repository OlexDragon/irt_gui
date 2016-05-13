package irt.gui.controllers.calibration.tools.data;

import java.util.Arrays;
import java.util.Objects;

import irt.gui.controllers.calibration.tools.enums.FrequencyUnits;

/**
 * 'value' may be up to 9 digits with a maximum of 10 Hz resolution. 'units' may be MHZ, KHZ or HZ.
 */
public class ToolsFrequency {

	private String value; public String getValue() { return value; }

	public ToolsFrequency(double value) {
		this.value = FrequencyUnits.toString(value);
	}

	public ToolsFrequency(String value) {

		Objects.requireNonNull(value);

		String dStr = value.replaceAll("[^\\d.-]", "");
		double d = dStr.isEmpty() ? 0 : Double.parseDouble(dStr);

		final String uStr = value.toUpperCase().replaceAll("[^MGK]", "");
		FrequencyUnits unit = Arrays
								.stream(FrequencyUnits.values())
								.parallel()
								.filter(u->!uStr.isEmpty())
								.filter(u->u.name().charAt(0)==uStr.charAt(0))
								.findAny()
								.orElse(FrequencyUnits.HZ);

		this.value = unit.valueOf(d);
	}

	@Override public String toString() {
		return value;
	}
}
