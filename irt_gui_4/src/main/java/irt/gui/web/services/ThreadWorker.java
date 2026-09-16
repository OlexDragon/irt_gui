package irt.gui.web.services;

import java.util.concurrent.ThreadFactory;

public final class ThreadWorker {

    public static Thread runThread(Runnable runnable) {

        Thread thread = createThread(runnable);
        thread.start();

        return thread;
    }

    public static ThreadFactory getThreadFactory() {
        return ThreadWorker::createThread;
    }

    public static Thread createThread(Runnable runnable) {

        Thread thread = new Thread(runnable);

        if (thread.getPriority() > Thread.MIN_PRIORITY)
            thread.setPriority(thread.getPriority() - 1);

        thread.setDaemon(true);

        return thread;
    }
}