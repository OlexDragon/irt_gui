package irt.calculator.harmonics;

public class ThreadRunner {

	public static Thread newThread(Runnable runnable) {
		final Thread thread = new Thread(runnable);
		int priority = thread.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(--priority);
		return thread;
	}

	public static Thread runThread(Runnable runnable) {
		final Thread thread = newThread(runnable);
		thread.start();
		return thread;
	}
}
