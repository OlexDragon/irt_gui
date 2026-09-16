package irt.gui.web.services.distributor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import irt.gui.web.beans.Packet;
import irt.gui.web.beans.RequestPacket;
import irt.gui.web.controllers.UpgradeRestController;
import irt.gui.web.exceptions.IrtSerialPortIOException;
import irt.gui.web.exceptions.IrtSerialPortRTException;
import irt.gui.web.exceptions.IrtSerialPortTOException;
import irt.gui.web.services.ThreadWorker;
import irt.gui.web.services.serialPort.IrtSerialPort;

@Service
public class LockDistributor implements SerialPortDistributor, Runnable, ThreadFactory {
	private final static Logger logger = LogManager.getLogger();

	Comparator<PacketTask> comparator = (a, b) -> {

		// 1. If packets are equal → return 0
		if (a.equals(b))
			return 0;

		// 2. Commands first
		if (a.isCommand() != b.isCommand())
			return a.isCommand() ? -1 : 1;

		// 3. FIFO fallback
		return Long.compare(a.getSeq(), b.getSeq());
	};

	private final BlockingQueue<PacketTask> queue = new PriorityBlockingQueue<>(100, comparator);
	private final IrtSerialPort serialPort;
	private final SerialDispatcher dispatcher;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(this);

	// constructor injection for dispatcher
	public LockDistributor(@Qualifier("jSerialComm") IrtSerialPort serialPort, SerialDispatcher dispatcher) {

		this.serialPort = serialPort;

		this.dispatcher = dispatcher;

		executor.submit(this);
	}

	@Override
	public void run() {

		try {

			while (!Thread.currentThread().isInterrupted()) {

				final PacketTask packetTask = queue.take();
				final RequestPacket packet = packetTask.getPacket();

				try {

					process(packetTask);

				} finally {

					if (!packetTask.isCommand()) {
						activePackets.remove(PacketKey.of(packet), packetTask);
					}

					packetTask.runTasks();
				}
			}

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
		}
	}

	private void process(PacketTask packetTask) {

		try {

			logger.traceEntry("packetTask={}", packetTask);
			dispatcher.dispatch(packetTask.getPacket());

		} catch (IrtSerialPortRTException | IrtSerialPortIOException | IrtSerialPortTOException e) {

			logger.catching(Level.DEBUG, e);
			packetTask.getPacket().setError(e.getLocalizedMessage());

		} catch (Exception e) {

			logger.catching(e);
			packetTask.getPacket().setError(e.getLocalizedMessage());
		}
	}

	private final Map<PacketKey, PacketTask> activePackets = new ConcurrentHashMap<>();

	@Override
	public synchronized FutureTask<RequestPacket> send(RequestPacket requestPacket) {
		FutureTask<RequestPacket> future = new FutureTask<>(() -> requestPacket);

		// Check if the port is locked
		if (isLocked(requestPacket)) {
			requestPacket.setError("The port is locked.");
			future.run();
			return future;
		}

		if (Boolean.TRUE.equals(requestPacket.getCommand())) {

			PacketTask packetTask = newPacketTask(requestPacket, future);
			queue.add(packetTask);

			return future;
		}

		PacketKey key = PacketKey.of(requestPacket);
		activePackets.compute(key, (k, existing) -> {

			if (existing != null && existing.add(requestPacket, future))
				return existing;

			PacketTask created = newPacketTask(requestPacket, future);

			queue.add(created);

			return created;
		});

		return future;
	}

	private PacketTask newPacketTask(RequestPacket requestPacket, FutureTask<RequestPacket> future) {

		final PacketTask packetTask = new PacketTask(requestPacket);
		packetTask.add(requestPacket, future);
		return packetTask;
	}

	public boolean isLocked(RequestPacket requestPacket) {
		return !requestPacket.getId().equals(UpgradeRestController.PACKET_ID)
				&& lockedPorts.contains(requestPacket.getSerialPort());
	}

	@Override
	public boolean isOpen(String serialPort) {
		return this.serialPort.isOpen(serialPort);
	}

	byte[] read(byte[] initial, String portName, Integer timeout, Integer baudrate, byte flagSequence)
			throws IrtSerialPortIOException, InterruptedException {

		byte[] buffer = initial;

		final int maxIterations = 200; // 200 × 10ms = 2 seconds max
		int iterations = 0;

		while (iterations++ < maxIterations) {

			// --- 1. Termination check (no lambdas) ---
			if (flagSequence == Packet.FLAG_SEQUENCE) {
				int count = 0;
				for (byte b : buffer) {
					if (b == flagSequence) {
						if (++count >= 2)
							return buffer;
					}
				}
			} else {
				for (byte b : buffer) {
					if (b == flagSequence)
						return buffer;
				}
			}

			// --- 2. Read more bytes ---
			byte[] more = serialPort.read(portName, timeout, baudrate);
			if (more == null || more.length == 0)
				return buffer;

			// --- 3. Append ---
			byte[] combined = Arrays.copyOf(buffer, buffer.length + more.length);
			System.arraycopy(more, 0, combined, buffer.length, more.length);
			buffer = combined;

			// --- 4. Small pacing delay ---
			TimeUnit.MILLISECONDS.sleep(10);
		}

		// --- 5. Timeout fallback ---
		return buffer;
	}

	@Override
	public Thread newThread(Runnable r) {
		return ThreadWorker.createThread(r);
	}

	@Override
	public void shutdown() {
		executor.shutdownNow();
	}

	@Override
	public boolean closePort(String spName) {
		queue.removeIf(task -> spName.equals(task.getPacket().getSerialPort()));

		return serialPort.close(spName);
	}

	private final Set<String> lockedPorts = ConcurrentHashMap.newKeySet();

	@Override
	public void lockPort(String spName) {
		if (lockedPorts.add(spName))
			logger.info("Locking port: {}", spName);
	}

	@Override
	public void unlockPort(String spName) {
		if (lockedPorts.remove(spName))
			logger.info("Unlocking port: {}", spName);
	}
}
