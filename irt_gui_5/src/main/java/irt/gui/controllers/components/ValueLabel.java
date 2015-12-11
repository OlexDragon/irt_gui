
package irt.gui.controllers.components;

import java.util.Observable;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;

public class ValueLabel extends ScheduledNode {

	@FXML private Label label;
	@FXML private Menu menuValues;

	@Override
	public void setName(String name) {
		
	}
	@Override
	public void update(Observable observable, Object arg) {
		logger.entry(observable, arg);
	}
}
