package irt.controller.text.document;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntDocument extends PlainDocument {
	private static final long serialVersionUID = -9062008454473998394L;

	public static final String NUMERIC = "0123456789";

	private int maxVal = Integer.MAX_VALUE;
	private int maxLength = Integer.MAX_VALUE;


	public void setMaxLength(int maxLength) {
	    if (maxLength < 0)
	        throw new IllegalArgumentException("maxLength="+maxLength+". It should be >0");
	    this.maxLength = maxLength;
	}

	public void setMaxVal(int maxVal) {
	    this.maxVal = maxVal;
	}

	@Override
	public void insertString(int offset, String str, AttributeSet attr)throws BadLocationException {

		if (str != null){
			str=str.replaceAll("\\D", "");

			String text = getText(0, offset) + str + getText(offset, getLength() - offset);

			if (!str.isEmpty() && validateLength(text) && isValidForMaxVal(text))

				super.insertString(offset, str, attr);
			else
				beep();
		}else
			beep();
	}

	public boolean isValidForMaxVal(String text) {
	    return Integer.parseInt(text)<=maxVal;
	}

	private boolean validateLength(String toAdd) {
	    return toAdd.length()<=maxLength;

	}

	private void beep() {
	    java.awt.Toolkit.getDefaultToolkit().beep();
	}


}
