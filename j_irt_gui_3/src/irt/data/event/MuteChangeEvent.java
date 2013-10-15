package irt.data.event;

import java.awt.AWTEvent;

@SuppressWarnings("serial")
public class MuteChangeEvent extends AWTEvent{

	public MuteChangeEvent(Object source, int id) {
		super(source, 2);
	}

}
