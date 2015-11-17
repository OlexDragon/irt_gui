package irt.gui.data.listeners;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public final class NumericChecker implements ChangeListener<String> {
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		// If no digit leave the old value
		if(!newValue.matches("\\d*"))
			((StringProperty)observable).setValue(oldValue);
	}
}
