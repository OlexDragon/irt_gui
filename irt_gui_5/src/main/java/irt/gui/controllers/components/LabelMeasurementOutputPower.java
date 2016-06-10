package irt.gui.controllers.components;

import irt.gui.data.packet.observable.measurement.OutputPowerPacket;
import irt.gui.data.value.Value;
import irt.gui.data.value.ValueDouble;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class LabelMeasurementOutputPower extends LabelAbstract{

	@FXML protected void initialize() {
		try {

			final OutputPowerPacket packet = new OutputPowerPacket();
			final Value value = new ValueDouble(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1);
			value.setPrefix(packet.getPrefix());
			setValue(value);
			addPacket(packet);

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
    }
}
