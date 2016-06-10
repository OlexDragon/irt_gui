
package irt.gui.data.listeners;

import java.util.Objects;

import irt.gui.controllers.components.StartStopAbstract;
import irt.gui.controllers.components.TextFieldAbstract;
import irt.gui.data.value.Value;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class TextChangeListener implements ChangeListener<String> {

	private Value value;
	private StartStopAbstract startStop;
	private TextField textField;

	public TextChangeListener(StartStopAbstract startStop, TextField textField, Value value) {
		this.startStop = Objects.requireNonNull(startStop);
		this.textField = Objects.requireNonNull(textField);
		this.value = Objects.requireNonNull(value);

		textField.textProperty().addListener(this);
		textField.setOnKeyReleased(e->{ if(e.getCode()==KeyCode.ESCAPE) start(); });
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		final Value copy = value.getCopy().setValue(newValue);

			if(value.equals(copy))
				start();
			else
				stot();
	}

	private void stot() {
		final ObservableList<String> styleClass = textField.getStyleClass();
		Platform.runLater(() -> {
			if (!styleClass.contains(TextFieldAbstract.CLASS_HAS_CHANGED)) {

				styleClass.add(TextFieldAbstract.CLASS_HAS_CHANGED);
				startStop.stop(true);
			}
		});
	}

	private void start() {

		if(startStop.start()){

			final ObservableList<String> styleClass = textField.getStyleClass();

			if (styleClass.size() > 0) 							// if size = 0 throw java.lang.ArrayIndexOutOfBoundsException
				Platform.runLater(()->styleClass.remove(TextFieldAbstract.CLASS_HAS_CHANGED));
		}
	}
}
