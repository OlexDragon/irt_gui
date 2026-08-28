package irt.gui.web.services;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConnectionCounter {
	private final static Logger logger = LogManager.getLogger();

	final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(ThreadWorker.getThreadFactory());

	@Value("${irt.connection.counter.delay}")
	private Integer delayMinutes;

	private final Map<String, ScheduledFuture<?>> sessions = new ConcurrentHashMap<>();

    public void add(String sessionId) {
    	if(sessionId == null) {
    		logger.catching(new Throwable("Missing session ID."));
    		return;
    	}

        // Cancel previous timeout if session is refreshed
        Optional.ofNullable(sessions.remove(sessionId))
                .ifPresent(f -> f.cancel(false));

        // Schedule removal after delay
        ScheduledFuture<?> future = scheduler.schedule(
                () -> sessions.remove(sessionId),
                delayMinutes,
                TimeUnit.MINUTES
        );

        sessions.put(sessionId, future);
    }

    public void remove(String sessionId) {
        Optional.ofNullable(sessions.remove(sessionId))
                .ifPresent(f -> f.cancel(false));
    }

    public int getConnectionCount() {
        return sessions.size();
    }

    public void stop() {
        sessions.values().forEach(f -> f.cancel(false));
        sessions.clear();
        scheduler.shutdownNow();
    }
}
