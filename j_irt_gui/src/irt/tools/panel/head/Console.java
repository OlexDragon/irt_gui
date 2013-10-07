package irt.tools.panel.head;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

@SuppressWarnings("serial")
public class Console extends JDialog {

	public static final JTextArea TEXT_AREA = new JTextArea();
	private static final int MAX_LINE_COUNT = 5000;

	public Console(JFrame gui, String string) {
		super(gui, string);

		setSize(1800, 200);
		JScrollPane contentPane = new JScrollPane();
		setContentPane(contentPane);
		contentPane.setViewportView(TEXT_AREA);
//		DefaultCaret caret = (DefaultCaret)TEXT_AREA.getCaret();
//		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}

	/**
	 * Start from new line
	 */
	public static void appendLn(Object localizedMessage, String prefix) {
		append(localizedMessage, "\n"+prefix);
	}


	/**
	 * Start from new line
	 */
	public static void appendLn(String prefix) {
		appendLn("", prefix);
	}

	public static void append(Object localizedMessage, String prefix) {
		int lineCount = TEXT_AREA.getLineCount();
		if(lineCount>MAX_LINE_COUNT){
			try {
				TEXT_AREA.replaceRange(null, TEXT_AREA.getLineStartOffset(0), TEXT_AREA.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		TEXT_AREA.append(prefix+" : "+localizedMessage);
		TEXT_AREA.setCaretPosition(TEXT_AREA.getDocument().getLength());
//			GuiController.TEXT_AREA.setCaretPosition(GuiController.TEXT_AREA.getDocument().getLength());
	}

	public static String getText() {
		return TEXT_AREA.getText();
	}
}
