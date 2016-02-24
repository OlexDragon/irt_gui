
package irt.gui.data.listeners;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;

public class TextFieldFocusListener implements ChangeListener<Boolean>{

	private static final String FOCUS_GAINED = "focusGained";
	private final TextField textField;

	/**
	 *  When focus gained add css class 'focusGained',
	 *  and remove it when focus lost
	 */
	public TextFieldFocusListener(TextField textField) {
		this.textField = textField;
		textField.focusedProperty().addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		if (newValue.booleanValue()) {
			if(textField.isEditable())
				focusGained();
        } else {
            focusLost();
        }
	}

	private void focusGained() {
		Platform.runLater(()->{

			final ObservableList<String> styleClass = textField.getStyleClass();

			if(styleClass.size()>0)
			styleClass.remove(FOCUS_GAINED);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException
			styleClass.add(FOCUS_GAINED);
		});
	}

	private void focusLost() {
		Platform.runLater(()->{

			final ObservableList<String> styleClass = textField.getStyleClass();
			if(styleClass.size()>0)
				styleClass.remove(FOCUS_GAINED);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException
		});
	}

}
