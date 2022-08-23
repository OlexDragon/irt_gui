package irt.gui.controllers.components;

import java.util.Observer;

import javafx.fxml.FXML;

public class TextFieldAttenuation{

	@FXML private TextFieldConfiguration attenuationController;

	@FXML protected void initialize() {
		attenuationController.setKeyStartWith("gui.control.attenuation.");
    }

	public synchronized void doUpdate(boolean doUpdate) {
		attenuationController.doUpdate(doUpdate);
	}

	public void get(Observer observer) {
		attenuationController
		.packets.
		stream()
		.forEach(p->{
			p.addObserver(observer);
		});
		attenuationController.send();
	}
}
