package irt.gui.controllers.interfaces;

import java.util.Observer;

import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.value.Value;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;

public interface SliderListener {

	void addFocusListener(ChangeListener<Boolean> focusListener);
	void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver, NumericChecker stepNumericChecker);
	void setText(double value);
	void onActionTextField();
	int getMultiplier();
	Value getValue();

}
