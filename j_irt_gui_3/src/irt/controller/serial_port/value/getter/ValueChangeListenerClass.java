package irt.controller.serial_port.value.getter;

import java.util.Arrays;

import irt.data.FireValue;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import javax.swing.event.EventListenerList;


public class ValueChangeListenerClass {


	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList valueChangeListeners = new EventListenerList();

	public void addVlueChangeListener(ValueChangeListener valueChangeListener) {
		valueChangeListeners.add(ValueChangeListener.class, valueChangeListener);
	}

	public void removeVlueChangeListener(ValueChangeListener valueChangeListener) {
		valueChangeListeners.remove(ValueChangeListener.class, valueChangeListener);

	}

	public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {
		Thread t = new Thread(new FireValue(valueChangeListeners, valueChangeEvent), "fireValueChangeListener for "+getClass().getSimpleName());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.start();

//		System.out.println("Class-"+getClass().getSimpleName()+" Fire valueChangeEvent: "+valueChangeEvent);
	}

	public <T> void removeVlueChangeListeners() {

		Object[] listenerList = valueChangeListeners.getListenerList();
		
		if(listenerList!=null){
			listenerList = Arrays.copyOf(listenerList, listenerList.length);

			for(Object ll:listenerList){
				if(ll instanceof ValueChangeListener)
					valueChangeListeners.remove(ValueChangeListener.class, (ValueChangeListener)ll);
			}
		}
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String toString() {
		return "ValueChangeListenerClass [getListenerCount()="
				+ valueChangeListeners.getListenerCount() + ", getClass()=" + getClass() + "]";
	}
}