
package irt.serial.port.listener;

import irt.services.AddressIntegerStringConverter;
import irt.services.NumericChecker;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class AddressListener extends NumericChecker {

	public AddressListener(StringProperty textProperty) {
		super(textProperty);
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if(!AddressIntegerStringConverter.CONVERTER.equals(newValue))
			super.changed(observable, oldValue, newValue);
	}

}