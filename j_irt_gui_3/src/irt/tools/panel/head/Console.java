package irt.tools.panel.head;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class Console extends JDialog {

	private final static Logger logger = (Logger) LogManager.getLogger();

	public static final JTextArea TEXT_AREA = new JTextArea();
	private static final int MAX_LINE_COUNT = 5000;
	private static ThreadsWorker threadsWorker = new ThreadsWorker();

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
		threadsWorker.append(prefix+" : "+localizedMessage);
	}

	public static String getText() {
		return TEXT_AREA.getText();
	}

	//*************************** class ThreadsWorker *************************************************
	private static class ThreadsWorker extends Thread{

		public ThreadsWorker() {
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		private BlockingQueue<String> stringQueue = new ArrayBlockingQueue<>(1024);

		@Override
		public void run() {
			while (true)
				try {
					String s = stringQueue.take();

					int lineCount = TEXT_AREA.getLineCount();
					if (lineCount > MAX_LINE_COUNT) {
						try {
							TEXT_AREA.replaceRange(null, TEXT_AREA.getLineStartOffset(0), TEXT_AREA.getLineEndOffset(0));
						} catch (BadLocationException e) {
							logger.catching(e);
						}
					}

					TEXT_AREA.append(s);
					TEXT_AREA.setCaretPosition(TEXT_AREA.getDocument().getLength());

				} catch (InterruptedException e) {
					logger.catching(e);
				}
		}

		public void append(String string) {
			stringQueue.add(string);
		}
	}
}
