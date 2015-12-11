package irt.gui.data.listeners;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public final class NumericChecker implements ChangeListener<String> {

	private long maximum = Long.MAX_VALUE; public long getMaximum() { return maximum; } public void setMaximum(long maximum) { this.maximum = maximum; }

	public NumericChecker(){}

	public NumericChecker(StringProperty stringProperty){
		stringProperty.addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		// If no digit or value bigger then max leave the old value
		if(!newValue.matches("\\d++") || Integer.parseInt(newValue)>maximum){
			final StringProperty stringProperty = (StringProperty)observable;
			stringProperty.removeListener(this);
			stringProperty.setValue(oldValue);
			stringProperty.addListener(this);
		}
	}
}
