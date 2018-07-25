package irt.controller.serial_port.value.getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.FireValue;
import irt.data.MyThreadFactory;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;


public class ValueChangeListenerClass {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	protected 	final 	ScheduledExecutorService 	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory("ValueChangeListenerClass"));
	private 	final 	EventListenerList 			valueChangeListeners 	= new EventListenerList();

	public void addVlueChangeListener(ValueChangeListener valueChangeListener) {

		valueChangeListeners.add(ValueChangeListener.class, valueChangeListener);
	}

	public void removeVlueChangeListener(ValueChangeListener valueChangeListener) {
		valueChangeListeners.remove(ValueChangeListener.class, valueChangeListener);

	}

	public void fireValueChangeListener(ValueChangeEvent valueChangeEvent) {

		new MyThreadFactory(new FireValue(valueChangeListeners, valueChangeEvent), "fireValueChangeListener");
	}

	public void removeVlueChangeListeners() {

		Object[] listenerList = valueChangeListeners.getListenerList();
		
		if(listenerList!=null){

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

	public void stop() {
		removeVlueChangeListeners();
	}
}