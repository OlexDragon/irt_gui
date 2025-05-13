package irt.gui.web.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import irt.gui.web.beans.RequestPacket;
import irt.gui.web.exceptions.IrtSerialPortException;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Service
public class LockDistributor implements SerialPortDistributor, Runnable, ThreadFactory {
	private final static Logger logger = LogManager.getLogger();

	BlockingQueue<PacketTask> queue = new PriorityBlockingQueue<>(100, (a,b)->{ if(a.command==b.command) return 0; else if(a.command) return 1; else return -1; });
	
	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;

	@Value("${irt.packet.acknowledgement.size}")
	private int acknowledgementSize;

	private final ScheduledExecutorService service;
	private final ScheduledFuture<?> scheduledFuture;

	public LockDistributor() {
		service = Executors.newSingleThreadScheduledExecutor(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 0, 100, TimeUnit.MILLISECONDS);
	}

	@Override
	public FutureTask<RequestPacket> send(RequestPacket requestPacket) {
		logger.traceEntry("{}", requestPacket);

		final Callable<RequestPacket> callable = ()->requestPacket;
		final FutureTask<RequestPacket> task = new FutureTask<>(callable );

		final PacketTask packetTask = new PacketTask(requestPacket, requestPacket.isCommand());

		final Optional<PacketTask> any = queue.parallelStream().filter(packetTask::equals).findAny();
		if(any.isPresent()) {
			logger.error("any.isPresent()");
			any.get().tasks.add(task);
		}else {
			packetTask.tasks.add(task);
			queue.add(packetTask);
		}

		return task;
	}

	private void sendFromQueue(RequestPacket requestPacket) {

		final String portName = requestPacket.getSerialPort();

		try {
				
			Optional.ofNullable(requestPacket.getBaudrate()).ifPresent(br -> serialPort.open(portName, br));
			serialPort.open(portName, requestPacket.getBaudrate());
			final int timeout = Optional.ofNullable(requestPacket.getTimeout()).orElse(100);
			final byte[] bytes = requestPacket.getBytes();

			if (bytes == null || bytes.length == 0)
				throw new IrtSerialPortException("There is no data to send.", new Throwable());

			byte[] received = serialPort.send(portName, timeout, bytes);

			byte[] acknowledgement = null;
			if (acknowledgementSize == received.length) {

				acknowledgement = received;
				received = serialPort.read(portName, null);

			}else 
				logger.warn("No acknowledgement. {} : {}", received.length, received);

			requestPacket.setAnswer(received);

			if(acknowledgement!=null)
				serialPort.send(portName, null, acknowledgement);

		} catch (IrtSerialPortException e) {
			logger.catching(Level.DEBUG, e);
			throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, e.getLocalizedMessage());
		}
	}

	private boolean buisy;
	@Override
	public void run() {
		if(buisy)
			return;
		buisy = true;
		try {

			final PacketTask packetTask = queue.take();
			sendFromQueue(packetTask.packet);
			packetTask.tasks.forEach(ThreadWorker::runThread);

		} catch (Exception e) {
			logger.catching(e);
		}
		buisy = false;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new ThreadWorker().newThread(r);
	}

	@Override
	public void shutdown() {
		scheduledFuture.cancel(true);
		service.shutdownNow();
	}

	@RequiredArgsConstructor @EqualsAndHashCode(exclude = {"tasks", "command"})
	private static class PacketTask{
		private final RequestPacket packet;
		private final List<FutureTask<RequestPacket>> tasks = new ArrayList<>();
		private final boolean command;
	}
}
