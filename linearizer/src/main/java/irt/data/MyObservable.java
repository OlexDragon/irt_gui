
package irt.data;

import java.lang.reflect.Field;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class MyObservable extends Observable {


	@SuppressWarnings("unchecked")
	public Observer[] getObservers() throws Exception{

		final Field obs = Observable.class.getDeclaredField("obs");
		obs.setAccessible(true);
		final Vector<Observer> vector = (Vector<Observer>) obs.get(this);
		return vector.toArray(new Observer[vector.size()]);
	}
}
