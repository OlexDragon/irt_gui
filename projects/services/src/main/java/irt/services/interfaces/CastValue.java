package irt.services.interfaces;

public interface CastValue<T> {

	T getValue();
	T getValue(byte... bs);
	T setValue(Object value);
	String toPrologixCode();
}
