package irt.gui.controllers.calibration.process;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalibrationMap extends TreeMap<Number, Number> {
	private static final long serialVersionUID = -6194375817528575601L;
	public static final String lineSeparator = System.getProperty("line.separator");

	private int beginning = 5; 

	@Override public synchronized Number put(Number key, Number value) {

		if(!isEmpty()){

			final Entry<Number, Number> lastEntry = lastEntry();
			final Number lastKey = lastEntry.getKey();

			int compareTo = new BigDecimal(lastKey.toString()).compareTo(new BigDecimal(key.toString()));
			logger.error("{}!!{}={}; size={}", lastKey, key, compareTo, size());
			if(compareTo>=0)
				if(size()>beginning)
					throw new UnsupportedOperationException("The key can not be less than or equal to the existing");
				else
					clear();

			final Number lastValue = lastEntry.getValue();

			compareTo = new BigDecimal(lastValue.toString()).compareTo(new BigDecimal(value.toString()));
			if(compareTo>=0){
				if(size()>beginning)
					throw new RuntimeException("The value is no longer changing.");

				clear();
				value = lastValue;
			}
		}
		return super.put(key, value);
	}

	public Number add(Entry<? extends Number, ? extends Number> entry){
		return put(entry.getKey(), entry.getValue());
	}

	public synchronized String toString(String variableName ) {
		final StringBuilder sb = new StringBuilder();
		sb.append(variableName).append("size\t").append(size());

		entrySet()
		.stream()
		.forEach(e->{
			sb.append(lineSeparator).append(variableName).append("entry \t").append(e.getValue()).append(" \t").append(e.getKey());
		});

		return sb.toString();
	}

	private Entry<Number, Number> firstEntry;
	private Entry<Number, Number> secondEntry;
	public CalibrationMap getWith(double accuracy) {
		CalibrationMap cm = new CalibrationMap();

		entrySet()
		.stream()
		.forEach(e->{

			//if 0 return all entries
			if(accuracy<=0){
				cm.add(e);
				return;
			}

			if(cm.size()>=2){
				ComparResult comp = comparTo(e, accuracy);
				logger.debug("{}", comp);
				switch (comp) {
				case EQUAL:
					cm.remove(secondEntry.getKey());
					cm.add(e);
					secondEntry = e;
					break;

				case IN_RANG:
					break;
				default:
					cm.add(e);
					firstEntry = secondEntry;
					secondEntry = e;
					break;
				}
				
			}else{
				cm.add(e);
				firstEntry = secondEntry;
				secondEntry = e;
			}
		});

		if(cm.size()<size() && !cm.lastEntry().equals(lastEntry()))
			cm.add(lastEntry());

		return cm;
	}

	Logger logger = LogManager.getLogger();
	private ComparResult comparTo(Entry<Number, Number> entry, double accuracy) {

		//   x1 + (y3-y1)/((y2-y1)/(x2-x1)) 
		final double x1 = firstEntry.getKey().doubleValue();
		final double y1 = firstEntry.getValue().doubleValue();
		final double x2 = secondEntry.getKey().doubleValue();
		final double y2 = secondEntry.getValue().doubleValue();
		final double y3 = entry.getValue().doubleValue();

		final double mustBe = x1 + (y3-y1)/((y2-y1)/(x2-x1)) ;

		final double key2 = entry.getKey().doubleValue();
		final double difference = Math.abs(mustBe - key2);

		logger.debug("firstEntry={}; secondEntry={}; entry={}; {} = {} + ({}-X{})/(({}-{})/({}-{}))", firstEntry, secondEntry, entry, mustBe, x1, y3, y1, y2, y1, x2, x1);

		if(difference<0.00001)
			return ComparResult.EQUAL;

		else if(difference<accuracy)
			return ComparResult.IN_RANG;

		return ComparResult.OUT_OF_RANGE;
	}

	private enum ComparResult{
		EQUAL,
		IN_RANG,
		OUT_OF_RANGE
	}
}
