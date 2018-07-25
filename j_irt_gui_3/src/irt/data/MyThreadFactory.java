
package irt.data;

import java.util.concurrent.ThreadFactory;

public class MyThreadFactory implements ThreadFactory {

	private final String name;

	public MyThreadFactory(String name) {
		this.name = name;
	}

	public MyThreadFactory(Runnable runnable, String name) {
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

}
