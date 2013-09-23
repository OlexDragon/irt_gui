package irt.data;

public class IdValue {

	private short id;
	private Object value;

	public IdValue(short id, Object value) {
		this.id = id;
		this.value = value;
	}

	public short getID() {
		return id;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "IdValue [id=" + id + ", value=" + value + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
