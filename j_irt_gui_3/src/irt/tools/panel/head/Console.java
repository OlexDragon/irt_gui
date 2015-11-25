package irt.tools.panel.head;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.RundomNumber;

@SuppressWarnings("serial")
public class Console extends JDialog {

	private final static Logger logger = (Logger) LogManager.getLogger();

	private static final int MAX_QUEUE_SIZE = 50;

	public static final JTextArea TEXT_AREA = new JTextArea();
	private static final int MAX_LINE_COUNT = 500;
	private static ThreadsWorker threadsWorker = new ThreadsWorker();

	public Console(JFrame gui, String string) {
		super(gui, string);
		threadsWorker.setOwner(this);

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

		private BlockingQueue<String> stringQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
		private Console console;
		private boolean queueIsFull;

		public ThreadsWorker() {
			super("Console.ThreadsWorker-"+new RundomNumber().toString()+"-"+new RundomNumber());
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (true)
				try {
					String s = stringQueue.take();

					int lineCount = TEXT_AREA.getLineCount();
					if (lineCount > MAX_LINE_COUNT) {
						try {
							TEXT_AREA.replaceRange(null, TEXT_AREA.getLineStartOffset(0), TEXT_AREA.getLineEndOffset(10));
						} catch (Exception e) {
							logger.catching(e);
						}
					}

					TEXT_AREA.append(s);
					TEXT_AREA.setCaretPosition(TEXT_AREA.getDocument().getLength());

				} catch (Exception e) {
					logger.catching(e);
				}
		}

		public void append(String string) {
			try {
				if (console!=null && console.isVisible()) {
					boolean isFull = stringQueue.size() >= MAX_QUEUE_SIZE-1;

					if (isFull){
						if(!queueIsFull){
							queueIsFull = true;
							stringQueue.put("\n*** computer is very slow... Information has been lost... ***\n");
						}
					}else{
						queueIsFull = false;
						stringQueue.put(string);
					}

				} else {
					if(TEXT_AREA.getText()!=null){
						stringQueue.clear();
						TEXT_AREA.setText(null);
					}
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}

		public void setOwner(Console console) {
			this.console = console;
		}
	}
}
