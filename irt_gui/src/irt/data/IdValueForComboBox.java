package irt.data;

public class IdValueForComboBox extends IdValue {

	public IdValueForComboBox(short id, Object value) {
		super(id, value);
	}

	@Override
	public String toString() {
		Object value = getValue();
		return value!=null ? value.toString() : "";
	}
}
