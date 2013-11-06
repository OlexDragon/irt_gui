package irt.tools.panel.subpanel.progressBar;

import irt.data.value.Value;
import irt.data.value.Value.Status;
import irt.data.value.ValueDouble;
import irt.tools.panel.head.IrtStylePanel;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class Section extends IrtStylePanel implements Observer{
	private static final long serialVersionUID = -3714457770555564111L;

	private static final Logger logger = (Logger) LogManager.getLogger();

	private Color inRangeColor = Color.GREEN;
	private Color underRangeColor = Color.WHITE;
	private Color moreThenRangeColor = Color.RED;
	private Value value = new ValueDouble(0, 0, Long.MAX_VALUE, 0);

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Color getInRangeColor() {
		return inRangeColor;
	}

	public void setInRangeColor(Color inRangeColor) {
		this.inRangeColor = inRangeColor;
	}

	public void setMinMaxValue(long minimum, long maximum) {
		value.setMinMax(minimum, maximum);
		setValue(value.getValue());
	}

	public void setValue(long value) {
		logger.trace("setValue({})", value);
		this.value.setValue(value);
		logger.trace("setted value={}, min={}, max={}", this.value, this.value.getMinValue(), this.value.getMaxValue());

		if(this.value.isError()){
			if(this.value.getMaxValue()<value){
				logger.trace("(this.value.getMaxValue()<value)");
				setBackground(moreThenRangeColor);
			}else{
				logger.trace("(this.value.getMaxValue()>value)");
				setBackground(underRangeColor);
			}
		}else{
			logger.trace("this.value.noError()");
			setBackground(inRangeColor);
		}
	}

	@Override
	public void update(Observable o, Object obj) {
		Value v = (Value) o;
		Status s = (Status) obj;

		if(s==Status.MORE_THEN_RANGE)
			setValue(v.getValue()+1);
		else if(s==Status.UNDER_RANGE)
			setValue(v.getValue()-1);
		else
			setValue(v.getValue());
	}

	public Color getUnderRangeColor() {
		return underRangeColor;
	}

	public Color getMoreThenRangeColor() {
		return moreThenRangeColor;
	}

	public void setUnderRangeColor(Color underRangeColor) {
		this.underRangeColor = underRangeColor;
	}

	public void setMoreThenRangeColor(Color moreThenRangeColor) {
		this.moreThenRangeColor = moreThenRangeColor;
	}

}
