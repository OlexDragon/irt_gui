
package irt.services;

import irt.services.interfaces.CastValue;

public abstract class ObjectToAbstract<T> implements CastValue<T> {

	private T value;
	public T getValue() {
		return value;
	}

	/**	replace existing value by new and return replaced value
	 * @return old value
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T setValue(Object value) {

		T oldValue = this.value;

		this.value = (T) value;

		return oldValue;
	}
}
