package irt.fx.control.buc;

import irt.packet.observable.measurement.InputPowerPacket;

public class LabelMeasurementInputPowerFx extends LabelMeasurementFx{
	public static final String TOOLTIP_BUNDLE_KEY = "power.input";

	public LabelMeasurementInputPowerFx() {
		super(new InputPowerPacket(), TOOLTIP_BUNDLE_KEY);
	}
}
