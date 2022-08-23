package irt.gui.controllers.components;

import irt.gui.data.packet.observable.measurement.InputPowerPacket;
import irt.gui.data.value.Value;
import irt.gui.data.value.ValueDouble;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class LabelMeasurementInputPower extends LabelAbstract{

	@FXML protected void initialize() {
		try {

			final InputPowerPacket packet = new InputPowerPacket();
			final Value value = new ValueDouble(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1);
			value.setPrefix(packet.getPrefix());
			setValue(value);
			addPacket(packet);

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
    }
}
