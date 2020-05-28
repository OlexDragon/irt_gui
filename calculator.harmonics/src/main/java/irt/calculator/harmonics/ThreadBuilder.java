package irt.calculator.harmonics;

public class ThreadBuilder {

	public static Thread newThread(Runnable runnable) {
		final Thread thread = new Thread(runnable);
		int priority = thread.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(--priority);
		return thread;
	}

	public static Thread startThread(Runnable runnable) {
		final Thread thread = newThread(runnable);
		thread.start();
		return thread;
	}
}
