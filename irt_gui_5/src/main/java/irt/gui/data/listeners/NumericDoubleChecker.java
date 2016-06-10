package irt.gui.data.listeners;

import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class NumericDoubleChecker implements ChangeListener<String> {

	private double maximum = Double.MAX_VALUE; public double getMaximum() { return maximum; } public void setMaximum(double maximum) { this.maximum = maximum; }

	public NumericDoubleChecker(){}

	public NumericDoubleChecker(StringProperty textProperty){
		textProperty.addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		Optional
		.ofNullable(newValue)
		.map(nv->nv.trim().replaceAll(",", ""))
		.filter(nv->nv.matches(FractionalNumberPlusPrefixChecker.fpRegex))
		.map(Double::parseDouble)
		.map(d->{

			if(Double.compare(d, maximum)>0){
				String text = Double.toString(maximum);
				setText((StringProperty)observable, text);
			}

			return newValue;
		})
		.orElseGet(()->{

			final String text = Optional
							.ofNullable(oldValue)
							.map(ov->ov.trim().replaceAll(",", ""))
							.filter(ov->!ov.isEmpty())
							.map(Double::parseDouble)
							.map(d->{
								return Double.compare(d, maximum)>0 ? Double.toString(maximum) : oldValue;
							})
							.orElse("1");

			setText((StringProperty)observable, text);
			return null;
		});
	}

	private void setText(StringProperty stringProperty, String text){
		Platform.runLater(()->{
			stringProperty.removeListener(this);;
			stringProperty.setValue(text);
			stringProperty.addListener(this);
		});
	}
}
