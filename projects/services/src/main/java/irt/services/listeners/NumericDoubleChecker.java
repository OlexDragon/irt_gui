package irt.services.listeners;

import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class NumericDoubleChecker implements ChangeListener<String> {

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
		.filter(nv->nv.matches(fpRegex))
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
