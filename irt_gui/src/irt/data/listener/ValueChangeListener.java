package irt.data.listener;

import irt.data.event.ValueChangeEvent;

import java.util.EventListener;



public interface ValueChangeListener extends EventListener{
	
	public void valueChanged(ValueChangeEvent valueChangeEvent);
}
