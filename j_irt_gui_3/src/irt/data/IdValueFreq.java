
package irt.data;

import java.util.Optional;

import irt.data.value.ValueFrequency;

public class IdValueFreq {

	private final byte id; 							public byte 			getId() 			{ return id; }
	private final ValueFrequency valueFrequency; 	public ValueFrequency 	getValueFrequency() { return valueFrequency; }

	public IdValueFreq(byte id, ValueFrequency valueFrequency) {
		this.id = id;
		this.valueFrequency = valueFrequency;
	}

	@Override
	public int hashCode() {
		return 31  + id;
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
		return Optional.ofNullable(valueFrequency).map(Object::toString).orElse("N/A");
	}
}
