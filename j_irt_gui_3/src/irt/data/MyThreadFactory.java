
package irt.data;

import java.util.concurrent.ThreadFactory;

public class MyThreadFactory implements ThreadFactory {

	public MyThreadFactory() {
	}

	public MyThreadFactory(Runnable runnable) {
		 newThread(runnable).start();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(--priority);
		t.setDaemon(true);
		return t;
	}

}
