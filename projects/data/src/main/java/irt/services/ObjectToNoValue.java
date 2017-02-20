package irt.services;

import java.util.InputMismatchException;

import irt.services.ObjectToAbstract;

public class ObjectToNoValue extends ObjectToAbstract<Void> {

	@Override
	public Void setValue(Object value) {

		if(value==null)
			return null;

		throw new InputMismatchException("This object do not accepts eny values.");
	}

	@Override
	public String toPrologixCode() {
		return "";
	}

	@Override
	public Void getValue(byte... bs) {
		return null;
	}

}
