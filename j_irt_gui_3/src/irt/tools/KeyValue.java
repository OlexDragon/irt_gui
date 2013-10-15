package irt.tools;

public class KeyValue<K, V> {

	private K key;
	private V value;

	public KeyValue(K key, V value){
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return key==null ? super.hashCode() : key.hashCode();
	}

	@Override
	public String toString() {
		return value!=null ? value.toString() : null;
	}
}
