
package irt.data;

import java.util.concurrent.ThreadFactory;

public class ThreadWorker implements ThreadFactory {

	private final String name;

	public ThreadWorker(String name) {
		this.name = name;
	}

	public ThreadWorker(Runnable runnable, String name) {
		this(name);
		 newThread(runnable).start();
	}

	@Override
	public Thread newThread(Runnable r) {

		Thread t = new Thread(r);
		t.setName(name + "-" + t.getId());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(--priority);
		t.setDaemon(true);
		return t;
	}

	public static Thread runThread(Runnable r, String threadName) {
		final Thread t = new ThreadWorker(threadName).newThread(r);
		t.start();
		return t;
	}

}
