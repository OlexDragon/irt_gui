package irt.data;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public final class FractionalNumberPlusPrefixChecker implements ChangeListener<String> {

	private static final String Digits = "(\\p{Digit}+)";
	private static final String HexDigits = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally
	// signed decimal integer.
	private static final String Exp = "[eE][+-]?" + Digits;
	final static String fpRegex = ("[\\x00-\\x20]*" + // Optional leading "whitespace"
			"[+-]?(" + // Optional sign character
			"NaN|" + // "NaN" string
			"Infinity|" + // "Infinity" string

	// A decimal floating-point string representing a finite positive
	// number without a leading sign has at most five basic pieces:
	// Digits . Digits ExponentPart FloatTypeSuffix
	//
	// Since this method allows integer-only strings as input
	// in addition to strings of floating-point literals, the
	// two sub-patterns below are simplifications of the grammar
	// productions from the Java Language Specification, 2nd
	// edition, section 3.10.2.

	// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
	"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

	// . Digits ExponentPart_opt FloatTypeSuffix_opt
			"(\\.(" + Digits + ")(" + Exp + ")?)|" +

	// Hexadecimal strings
			"((" +
			// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
			"(0[xX]" + HexDigits + "(\\.)?)|" +

	// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
			"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

	")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional trailing "whitespace"

	private double maximum = Double.MAX_VALUE; 		public double getMaximum() { return maximum; } 		public void setMaximum(double maximum) { this.maximum = maximum; }

	public FractionalNumberPlusPrefixChecker() {
	}

	public FractionalNumberPlusPrefixChecker(StringProperty stringProperty) {
		stringProperty.addListener(this);
	}

	@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

		int index = lastDigitIndex(newValue);
		String substring = null;
		if(index>=0)
			substring = newValue.substring(0, ++index);

		// If no digit or value bigger then max leave the old value
		if (index<0 || !substring.matches(fpRegex) || Double.compare(Double.parseDouble(substring), maximum)>0) {
			final StringProperty stringProperty = (StringProperty) observable;
			stringProperty.removeListener(this);
			stringProperty.setValue(oldValue);
			stringProperty.addListener(this);
		}
	}

	protected int lastDigitIndex(String newValue) {

		if(newValue==null || newValue.isEmpty())
			return -1;

		final char[] charArray = newValue.toCharArray();
		int index=charArray.length;
		for(; index>0;)
			if(charArray[--index]>='0' && charArray[index]<='9' || charArray[index]=='.')
				break;
		return index;
	}
}
