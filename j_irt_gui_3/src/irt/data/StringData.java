package irt.data;

public class StringData {

	private String str;

	public StringData(byte[] buffer) {
		if(buffer == null)
			str = "N/A";
		else
			str = new String(buffer).trim();
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		return str.equals(obj);
	}

	@Override
	public int hashCode() {
		return str.hashCode();
	}
}
