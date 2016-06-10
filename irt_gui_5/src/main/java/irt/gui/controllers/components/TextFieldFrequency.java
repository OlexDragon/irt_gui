package irt.gui.controllers.components;

import javafx.fxml.FXML;

public class TextFieldFrequency{
	
	@FXML private TextFieldConfiguration frequencyController;

	@FXML protected void initialize() {
		frequencyController.setKeyStartWith("gui.control.frequency.");
    }

	public synchronized void doUpdate(boolean doUpdate) {
		frequencyController.doUpdate(doUpdate);
	}
}
