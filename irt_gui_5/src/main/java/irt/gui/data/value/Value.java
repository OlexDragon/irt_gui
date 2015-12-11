package irt.gui.data.value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Observable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Value extends Observable{

	protected final Logger logger = LogManager.getLogger();

	public enum Status{
				IN_RANGE,
				UNDER_RANGE,
				MORE_THEN_RANGE,
				RANGE_SET
	}
	private int type = 0;

	protected Long oldValue;
	protected Long value;

	private long minValue;
	private long maxValue;
	protected int factor;
	protected String prefix;

	protected boolean error;

	protected Value(){}

	public Value(Long value, long minValue, long maxValue, int precision){
		setFactor(precision);
		setMinMax(minValue, maxValue);
		setValue(value);
		setPrefix();
	}

	public Value(long value, double minValue, double maxValue, int precision){
		setFactor(precision);
		setMinMax(Math.round(minValue*factor), Math.round(maxValue*factor));
		setValue(value);
		setPrefix();
	}

	public Value(String value, String minValue, String maxValue, int precision) {
		setFactor(precision);
		setMinMax(parseLong(minValue)*factor, parseLong(maxValue)*factor);
		setValue(value!=null ? parseLong(value) : 0);
		setPrefix();
	}

	public Value(Value value) {
		setMinMax(value.getMinValue(), value.getMaxValue());
		setValue(value.getValue());
		factor = value.getFactor();
		prefix = value.getPrefix();
	}

	private void setFactor(int precision) {
		factor = (int) Math.pow(10, precision);
	}

	public void setMinMax(long minValue, long maxValue) {
		setChanged();
		if (minValue < maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		} else {
			this.minValue = maxValue;
			this.maxValue = minValue;
		}
		notifyObservers(Status.RANGE_SET);

		setValue(value);
	}

	public void setMinMax(String minValue, String maxValue) {
		setMinMax(parseLong(minValue), parseLong(maxValue));
	}

	public long getMinValue() {
		return minValue;
	}

	public int getRelativeMinValue() {
		return 0;
	}

	protected void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	protected void setMinValue(String minValue) {
		long min = parseLong(minValue);
		setMinValue(min);
	}

	public long getMaxValue() {
		return maxValue;
	}

	public long getRelativeMaxValue() {
		return maxValue-minValue;
	}

	protected void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	protected void setMaxValue(String maxValue) {
		long max = parseLong(maxValue);
		setMaxValue(max);
	}

	public Value setValue(Object value){
		if(value != null){
			switch(value.getClass().getSimpleName()){
			case "String":
				setValue((String)value);
				break;
			case "Short":
			case "Integer":
			case "Long":
				setValue(((Number)value).longValue());
				break;
			case "Double":
				setValue((double)value);
				break;
			default:
				throw new IllegalStateException("The class " + value.getClass().getSimpleName() + " is not acceptable");
			}
		}
		return this;
	}

	private void setValue(Long value) {

		if(value==null)
			return;

		if (this.value != value || value<minValue || value>maxValue) {
			oldValue = this.value;
			setChanged();

			if (value > maxValue) {
				this.value = maxValue;
				error = true;
				notifyObservers(Status.MORE_THEN_RANGE);
			} else if (value < minValue) {
				error = true;
				this.value = minValue;
				notifyObservers(Status.UNDER_RANGE);
			} else {
				error = false;
				this.value = value;
				notifyObservers(Status.IN_RANGE);
			}
		}else
			error = false;
	}

	private void setValue(String text) {
		setValue(parseLong(text));
	}

	private void setValue(Double value) {
		this.value = (long) (value * factor);
	}

	public Long getValue() {
		return value;
	}

	public long getValue(int relativeValue) {
		return relativeValue + minValue;
	}

	/**
	 * @return 'value - minValue'. if value==null return 'minValue'
	 */
	public int getRelativeValue() {
		return (int) (value!=null ? value - minValue : minValue);
	}

	public void setRelativeValue(int relValue) {
		setValue(relValue + minValue);
	}

	public Long parseLong(String text) {

		Long value;

		if(text==null || text.trim().isEmpty()){

			value = this.value;
			error = true;
		}else{

			text = text.toUpperCase().replaceAll("[^\\d.E-]", "");

			if(!text.isEmpty() && Character.isDigit(text.charAt(text.length()-1)))
				try {

					value = Math.round(Double.parseDouble(text)*factor);
					error = false;

				} catch (Exception e) {

					value = this.value;
					error = true;
					logger.catching(e);
				}
			else{
				value = this.value;
				error = true;
			}

		}
		return value;
	}

	public int getFactor() {
		return factor;
	}

	protected NumberFormat getInstance() {
		return NumberFormat.getIntegerInstance();
	}

	protected void setMinValue(long minValue) {
		this.minValue = minValue;
	}

	protected void setMaxValue(long maxValue) {
		this.maxValue = maxValue;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix!= null ? prefix : "";
	}

	public void setPrefix() {
		this.prefix = "";
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Value getCopy() {
		Value value;
		switch(getClass().getSimpleName()){
		case "ValueFrequency":
			value = new ValueFrequency(this);
			break;
		case "ValueDouble":
			value = new ValueDouble(this);
			break;
		case "Value":
			value = new ValueDouble(this);
			break;
		default:
			value = new Value(this);
		}
		return value;
	}

	public Long getOldValue() {
		return oldValue;
	}

	public void add(long value) {
		setValue(value+this.value);
	}

	public void subtract(long value) {
		setValue(this.value-value);
	}

	public boolean hasChanged() {
		return oldValue!=null && oldValue!=value;
	}

	public String getExponentialValue() {
		DecimalFormat df = new DecimalFormat("0.00000000000000E000");
		return df.format((double)value/factor);
	}

	public boolean isError() {
		return error;
	}

	@Override
	public int hashCode() {
		return new Long(value).hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if(!(obj instanceof Value))
			return false;

		Value other = (Value)obj;
		
		return value!=null ? value.equals(other.value) : other.value!=null;
	}

	public String toString(long value) {
		NumberFormat numberFormat = getInstance();
		double result = (double)value/factor;
		return numberFormat.format(result)+prefix;
	}

	@Override
	public String toString() {
		return value!=null ? toString(value) : "";
	}
}
