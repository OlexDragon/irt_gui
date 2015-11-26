
package irt.data;

import irt.controller.translation.Translation;

public class LOIdValue extends IdValue {

	public LOIdValue(short id, Object value) {
		super(id, value);
	}

	@Override
	public String toString() {
		return Translation.getValue(String.class, "lo", "LO") + ":" + getID() + " - " + getValue();
	}

}
