package irt.data.packets.interfaces;

import java.util.Observer;

import irt.data.Value;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;

public interface SliderListener {

	void addFocusListener(ChangeListener<Boolean> focusListener);
	void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver);
	void setText(double value);
	void onActionTextField();
	int getMultiplier();
	Value getValue();

}
