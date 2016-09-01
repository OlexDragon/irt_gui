package irt.data;

import java.util.Optional;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class NumericChecker implements ChangeListener<String> {

	private long maximum = Long.MAX_VALUE; public long getMaximum() { return maximum; } public void setMaximum(long maximum) { this.maximum = maximum; }

	public NumericChecker(){}

	public NumericChecker(StringProperty textProperty){
		textProperty.addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		Optional
		.ofNullable(newValue)
		.map(mv->mv.replaceAll(",", ""))
		// If is not digit or value bigger then max, leave the old value
		.filter(nv->!nv.matches("\\d++") || Integer.parseInt(nv)>maximum)
		.ifPresent(nv->{
			
			final StringProperty stringProperty = (StringProperty)observable;
			stringProperty.removeListener(this);
			stringProperty.setValue(oldValue);
			stringProperty.addListener(this);
		});
	}
}
