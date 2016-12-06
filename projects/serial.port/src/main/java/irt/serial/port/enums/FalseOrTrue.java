package irt.serial.port.enums;

import java.util.Optional;

public enum FalseOrTrue {

	FALSE,
	TRUE;

	public Boolean getValue(){
		Boolean value;
		switch(this){
		case FALSE:
			value = false;
			break;
		case TRUE:
			value = true;
			break;
		default:
			value = null;
		}
		return value;
	}

	public FalseOrTrue valueOf(Boolean bool){

		return Optional
						.ofNullable(bool)
						.map(b->b ? TRUE : FALSE)
						.orElse(null);
	}
}
