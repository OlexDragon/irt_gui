package irt.gui.controllers.components;

import irt.gui.data.packet.observable.device_debug.OutputPowerDetectorPacket;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class LabelRegisterOutputPowerDetector extends LabelAbstract{

	@FXML protected void initialize() {
		try {

			final Value value = new Value(0L, 0L, Long.MAX_VALUE, 0);
			value.setPrefix(" mV");
			setValue(value);
			addPacket(new OutputPowerDetectorPacket());

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
    }
}
