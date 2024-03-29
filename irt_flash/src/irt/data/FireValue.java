package irt.data;

import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;

import javax.swing.event.EventListenerList;

	
	public class FireValue implements Runnable{

		private EventListenerList valueChangeListeners;
		private ValueChangeEvent valueChangeEvent;

		public FireValue(EventListenerList valueChangeListeners, ValueChangeEvent valueChangeEvent){
			this.valueChangeListeners = valueChangeListeners;
			this.valueChangeEvent = valueChangeEvent;
		}

		@Override
		public void run() {

			Object[] listeners = valueChangeListeners.getListenerList();
			for (int i = 0; i < listeners.length; i++) {
				Object l = listeners[i];
				if (l == ValueChangeListener.class)
					((ValueChangeListener) listeners[++i]).valueChanged(valueChangeEvent);
			}
		}
	}
