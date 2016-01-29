
package irt.data;

import irt.data.value.ValueFrequency;

public class IdValueFreq {

	private final byte id; public byte getId() { return id; }

	private final ValueFrequency valueFrequency;

	public IdValueFreq(byte id, ValueFrequency valueFrequency) {
		this.id = id;
		this.valueFrequency = valueFrequency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdValueFreq other = (IdValueFreq) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return valueFrequency.toString();
	}
}
