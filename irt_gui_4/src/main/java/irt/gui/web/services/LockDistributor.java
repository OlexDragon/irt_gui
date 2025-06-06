package irt.gui.web.services;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.stereotype.Service;

import irt.gui.web.beans.Packet;
import irt.gui.web.beans.PacketType;
import irt.gui.web.beans.RequestPacket;
import irt.gui.web.exceptions.IrtSerialPortIOException;
import irt.gui.web.exceptions.IrtSerialPortRTException;
import irt.gui.web.exceptions.IrtSerialPortTOException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

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

	private boolean buisy;
	@Override
	public void run() {
		if(buisy)
			return;
		buisy = true;

		PacketTask packetTask = null;
		try {

			packetTask = queue.take();
			sendFromQueue(packetTask.packet);

		} catch (IrtSerialPortRTException | IrtSerialPortIOException | IrtSerialPortTOException e) {
			logger.catching(Level.DEBUG, e);
			packetTask.getPacket().setError(e.getLocalizedMessage());

		} catch (InterruptedException e) {
			logger.catching(Level.TRACE, e);

		} catch (Exception e) {
			logger.catching(e);
			packetTask.getPacket().setError(e.getLocalizedMessage());
		}
		packetTask.tasks.forEach(ThreadWorker::runThread);
		buisy = false;
	}

	@Override
	public FutureTask<RequestPacket> send(RequestPacket requestPacket) {
//		logger.traceEntry("Put in queue -> {}", requestPacket);

		final Callable<RequestPacket> callable = ()->requestPacket;
		final FutureTask<RequestPacket> task = new FutureTask<>(callable );

		final PacketTask packetTask = new PacketTask(requestPacket, requestPacket.isCommand());

		final Optional<PacketTask> any = queue.parallelStream().filter(packetTask::equals).findAny();

		if(any.isPresent()) {
			any.get().tasks.add(task);
		}else {
			packetTask.tasks.add(task);
			queue.add(packetTask);
		}

		return task;
	}

	private void sendFromQueue(RequestPacket requestPacket) throws IrtSerialPortIOException {
		logger.traceEntry("{}", requestPacket);

		final String portName = requestPacket.getSerialPort();

		final int timeout = Optional.ofNullable(requestPacket.getTimeout()).orElse(100);
		final byte[] bytes = requestPacket.getBytes();

		if (bytes == null || bytes.length == 0) {
			requestPacket.setError("There is no data to send.");
			return;
		}

		final Integer baudrate = requestPacket.getBaudrate();
		byte[] received = serialPort.send(portName, timeout, bytes, baudrate);
		if(received == null)
			return;

		Packet packet = new Packet(received);

		logger.debug("\n\t{}\n\t{} : {}", packet, received.length, received);

		final int lastIndex = packet.getLastIndex() + 1;
		if(lastIndex==received.length && packet.getPacketType()== PacketType.ACKNOWLEDGEMENT)
			received = serialPort.read(portName, timeout, baudrate);
		else
			received = Arrays.copyOfRange(received, lastIndex, received.length);

		logger.debug("\n\t{}\n\t{} : {}", packet, received.length, received);

		requestPacket.setAnswer(received);

		Optional.ofNullable(packet.getAcknowledgement()).filter(a->a.length>0)
		.ifPresent(
				a->{
					try {
						serialPort.send(portName, null, a, baudrate);
					} catch (IrtSerialPortIOException e) {
						throw new IrtSerialPortRTException(e.getLocalizedMessage(), e);
					}
				});
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

	@RequiredArgsConstructor @Getter @EqualsAndHashCode(exclude = {"tasks", "command"}) @ToString
	private static class PacketTask{
		private final RequestPacket packet;
		private final List<FutureTask<RequestPacket>> tasks = new ArrayList<>();
		private final boolean command;
	}

	@Override
	public boolean closePort(String spName) {
		return serialPort.close(spName);
	}
}
