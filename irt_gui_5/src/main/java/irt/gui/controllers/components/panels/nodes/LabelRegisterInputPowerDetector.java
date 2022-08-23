package irt.gui.controllers.components;

import java.util.Observer;

import irt.gui.data.packet.observable.device_debug.InputPowerDetectorPacket;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class LabelRegisterInputPowerDetector extends LabelAbstract{

	@FXML protected void initialize() {
		try {

			final Value value = new Value(0L, 0L, Long.MAX_VALUE, 0);
			value.setPrefix(" mV");
			setValue(value);
			addPacket(new InputPowerDetectorPacket());

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
    }

	public void get(Observer observer) {
		packets.
		stream()
		.forEach(p->{
			p.addObserver(observer);
		});
		send();
	}
}
