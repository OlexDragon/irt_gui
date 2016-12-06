
package irt.service;

import java.util.InputMismatchException;

import irt.fx.control.prologix.enums.Eos;
import irt.services.ObjectToAbstract;

public class ObjectToNoValue extends ObjectToAbstract<Eos> {

	@Override
	public Eos setValue(Object value) {
		throw new InputMismatchException("This object do not accepts eny values.");
	}

	@Override
	public String toPrologixCode() {
		return "";
	}

}
