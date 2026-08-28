package irt.gui.web.services.distributor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import irt.gui.web.beans.RequestPacket;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class PacketTask {

	private static final AtomicLong SEQ = new AtomicLong();
	private final long seq = SEQ.getAndIncrement();
	private final RequestPacket packet;
	private final Map<RequestPacket, FutureTask<RequestPacket>> tasks = new IdentityHashMap<>();
	private boolean completed;

	public @NonNull Boolean isCommand() {
		return packet.getCommand();
	}

	public synchronized boolean add(RequestPacket requestPacket, FutureTask<RequestPacket> future) {

		if (completed)
			return false;

		tasks.put(requestPacket, future);
		return true;
	}

	public void runTasks() {

		final List<FutureTask<RequestPacket>> futures;

		synchronized (this) {

			completed = true;

			final byte[] answer = packet.getAnswer();
			final String error = packet.getError();

			for (RequestPacket request : tasks.keySet()) {

				if (request != packet) {
					request.setAnswer(answer);
					request.setError(error);
				}
			}

			futures = new ArrayList<>(tasks.values());
		}

		futures.forEach(FutureTask::run);
	}

	public synchronized void copyResult() {

		final byte[] answer = packet.getAnswer();
		final String error = packet.getError();

		for (RequestPacket request : tasks.keySet()) {

			if (request != packet) {
				request.setAnswer(answer);
				request.setError(error);
			}
		}
	}
}