package irt.gui.controllers.calibration.tools.data;

import java.util.Arrays;
import java.util.Objects;

import irt.gui.controllers.calibration.tools.enums.PowerUnits;

/**
 * 'value' may be up to 4 digits plus a sign if applicable, e.g. - 127.1 or maximum resolution of .l dB, ,001 mV, .Ol pV. 'powerUnits' may be DBM, MV, UV, MVEMF, UVEMF, DBUV, DBUVEMF.
 *  If in reference mode only DB or DBM are allowed.
 */
public class ToolsPower {

	private String value; public String getValue() { return value; }

	public ToolsPower(double value, PowerUnits powerUnits) {
		if(powerUnits!=null)
			switch(powerUnits){
			case DBM:
			case DB:
				this.value = powerUnits.valueOf(value);
				break;
			}
		else
			this.value = PowerUnits.DBM.valueOf(value);
	}

	public ToolsPower(String value) {

		Objects.requireNonNull(value);

		String dStr = value.replaceAll("[^\\d.-]", "");
		double d = dStr.isEmpty() ? 0 : Double.parseDouble(dStr);

		final String uStr = value.toUpperCase().replaceAll("[^DBM]", "");
		PowerUnits unit = Arrays
								.stream(PowerUnits.values())
								.parallel()
								.filter(u->!uStr.isEmpty())
								.filter(u->u.name().equals(uStr))
								.findAny()
								.orElse(PowerUnits.DBM);

		this.value = unit.valueOf(d);
	}

	@Override public String toString() {
		return value;
	}

}
