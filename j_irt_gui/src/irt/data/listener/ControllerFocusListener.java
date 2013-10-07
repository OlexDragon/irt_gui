package irt.data.listener;

import irt.data.event.ValueChangeEvent;

import java.util.EventListener;

public interface ControllerFocusListener extends EventListener{

	public void focusGained(ValueChangeEvent valueChangeEvent);
}
