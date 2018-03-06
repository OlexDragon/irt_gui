package irt.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringData {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

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
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return str.hashCode();
	}
}
