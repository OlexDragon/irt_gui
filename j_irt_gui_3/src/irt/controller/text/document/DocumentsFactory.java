package irt.controller.text.document;

import javax.swing.text.Document;

public class DocumentsFactory {

	public static Document createIntDocument(int maxValue) {
		IntDocument intDocument = new IntDocument();
	    intDocument.setMaxVal(maxValue);
	    intDocument.setMaxLength((""+maxValue).length());
	    return intDocument;
	}
}
