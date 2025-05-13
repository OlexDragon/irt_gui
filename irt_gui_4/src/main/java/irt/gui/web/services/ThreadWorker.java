
package irt.gui.web.services;

import java.util.concurrent.ThreadFactory;

public class ThreadWorker implements ThreadFactory {

	@Override
	public Thread newThread(Runnable r) {

		Thread t = new Thread(r);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(--priority);
		t.setDaemon(true);
		return t;
	}

	public static Thread runThread(Runnable r) {
		final Thread t = new ThreadWorker().newThread(r);
		t.start();
		return t;
	}

	public static ThreadFactory getThreadFactory() {
		return new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				return new ThreadWorker().newThread(r);
			}
		};
	}

}
