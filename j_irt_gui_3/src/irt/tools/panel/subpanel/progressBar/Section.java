package irt.tools.panel.subpanel.progressBar;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.MyThreadFactory;
import irt.data.value.Value;
import irt.data.value.Value.Status;
import irt.data.value.ValueDouble;
import irt.tools.panel.head.IrtStylePanel;

public class Section extends IrtStylePanel implements Observer{
	private static final long serialVersionUID = -3714457770555564111L;

	private static final Logger logger = LogManager.getLogger();

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
		logger.debug("to set: {}; setted value={}, min={}, max={}, isError={}", value, this.value, this.value.getMinValue(), this.value.getMaxValue(), this.value.isError());

		Color backgroundColor;
		if(this.value.isError()){
			if(this.value.getMaxValue()<value){
				logger.trace("(this.value.getMaxValue()<value)");
				backgroundColor = moreThenRangeColor;
			}else{
				logger.debug("(this.value.getMaxValue()>value);)");
				backgroundColor = underRangeColor;
			}
		}else{
			logger.trace("this.value.noError()");
			backgroundColor = inRangeColor;
		}

		logger.debug("setBackground({})", backgroundColor);
		SwingUtilities.invokeLater(
				()->{
					if(getBackground().equals(backgroundColor))
						return;

					setBackground(backgroundColor);
					invalidate();
				});
	}

	@Override
	public void update(Observable o, Object obj) {
		new MyThreadFactory(()->{
			Value v = (Value) o;
			Status s = (Status) obj;

			if(s==Status.MORE_THEN_RANGE)
				setValue(v.getValue()+1);
			else if(s==Status.UNDER_RANGE)
				setValue(v.getValue()-1);
			else
				setValue(v.getValue());
		}, "Section");
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
