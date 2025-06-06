package irt.gui.web.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConnectionCounter {

	@Value("${irt.serial.port.close.delay}") private Integer delay;
	private final static Map<String, IdRemover> sessions = new HashMap<>();

	public void add(String sessionId){
		ThreadWorker.runThread(
				()->{
					synchronized (sessions) {

						Optional.ofNullable(sessions.get(sessionId)).ifPresent(IdRemover::stopThis);

						IdRemover idRemover = new IdRemover(sessionId);
						sessions.put(sessionId, idRemover);
					}
				});
	}

	public int getConnectionCount() {
		synchronized (sessions) {
			return sessions.size();
		}
	}

	public static void stop() {
		sessions.entrySet().stream().map(Map.Entry::getValue).forEach(IdRemover::stopThis);
	}

	class IdRemover extends Thread{

		private final String sessionId;
		
		private boolean run = true;

		private Thread thisThread;

		public IdRemover(String sessionId) {
			this.sessionId =  sessionId;
			thisThread = ThreadWorker.runThread(this);
		}

		@Override
		public void run() {

			try {

				TimeUnit.SECONDS.sleep(5);

			} catch (InterruptedException e) { }

			if(run)
				synchronized (sessions) {
					sessions.remove(sessionId);
				}
		}

		public void stopThis() {
			run = false;
			thisThread.interrupt();
		}
	}
}
