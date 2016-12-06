package irt.services.interfaces;

public interface CastValue<T> {

	T getValue();
	T setValue(Object value);
	String toPrologixCode();
}
