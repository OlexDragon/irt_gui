package irt.data.event;

import java.awt.AWTEvent;

@SuppressWarnings("serial")
public class ValueChangeEvent  extends AWTEvent {

	public ValueChangeEvent(Object source, int id) {
		super(source, id);
	}

	@Override
	public String toString() {
		return "ValueChangeEvent [id="+id+", source="+source+"("+source.getClass().getSimpleName()+")]";
	}
}
