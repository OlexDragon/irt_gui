package irt.data.tools.interfaces;

import javax.activation.UnsupportedDataTypeException;

public interface ToolCommands {

	byte[] getCommand();
	void setValue(Object value) throws UnsupportedDataTypeException;
	Object getValue();
}
