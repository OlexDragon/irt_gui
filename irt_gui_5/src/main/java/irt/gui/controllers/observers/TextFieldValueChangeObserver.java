
package irt.gui.controllers.observers;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.value.Value;
import irt.gui.data.value.ValueDouble;
import irt.gui.data.value.enums.ValueStatus;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;

public class TextFieldValueChangeObserver implements Observer {
	private Logger logger = LogManager.getLogger();

	private Slider slider;

	private ChangeListener<? super Number> sliderValueChangeListener;

	private int multiplier = 1;
	
	public TextFieldValueChangeObserver(Slider slider, ChangeListener<? super Number> sliderValueChangeListener) {
		this.slider = slider;
		this.sliderValueChangeListener = sliderValueChangeListener;
	}

	@Override
	public void update(Observable o, Object arg) {

		Value v = (Value) o;
		if(arg==ValueStatus.IN_RANGE){

			final Long value = v.getValue();

			if(o instanceof ValueDouble){

				double rv = (double)value/v.getFactor();
				if(Double.compare(slider.getValue() ,rv)!=0)
					setSliderValue(rv);

			}else{

				Long rv = value / multiplier;
//				logger.error("v={}; value={}; rv={}; multiplier=-{}", v, value, rv, multiplier);
				if(Double.compare(slider.getValue(), rv)!=0)
					setSliderValue(rv);

			}
		}else
			logger.warn("The value {} is out of range", v.getOriginalValue());
	}

	public void setSliderValue(double rv) {
		logger.traceEntry("{}", rv);
		Platform.runLater(()->{

			final DoubleProperty valueProperty = slider.valueProperty();
			valueProperty.removeListener(sliderValueChangeListener);
			slider.setValue(rv);
			valueProperty.addListener(sliderValueChangeListener);
		});
	}

	public void setMultiplier(int multiplier){
		this.multiplier = multiplier;
	}
}
