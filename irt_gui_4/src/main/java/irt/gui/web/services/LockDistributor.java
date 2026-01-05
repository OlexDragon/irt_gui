package irt.gui.web.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import irt.gui.web.beans.Packet;
import irt.gui.web.beans.RequestPacket;
import irt.gui.web.controllers.ConsoleRestController;
import irt.gui.web.controllers.UpgradeRestController;
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
	@Autowired @Qualifier("jSerialCommFlash") JSerialCommFlash serialPortFlash;

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
			logger.traceEntry("packetTask={}", packetTask);
			final boolean isFlash = Optional.ofNullable(packetTask.getPacket().getName()).map(n->n.equals("Flash")).orElse(false);
			if (isFlash)
				sendFromQueueFlash(packetTask.packet);
			else
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
	public synchronized FutureTask<RequestPacket> send(RequestPacket requestPacket) {
		logger.traceEntry("Put in queue -> {}", requestPacket);

		// Check if the port is locked
		if(!requestPacket.getId().equals(UpgradeRestController.PACKET_ID) && lockedPorts.contains(requestPacket.getSerialPort())) {
			requestPacket.setError("The port is locked.");
			final Callable<RequestPacket> callable = ()->requestPacket;
			final FutureTask<RequestPacket> task = new FutureTask<>(callable);
			task.run();
			return task;
		}

		final Callable<RequestPacket> callable = ()->requestPacket;
		final FutureTask<RequestPacket> task = new FutureTask<>(callable);

		final PacketTask packetTask = new PacketTask(requestPacket, requestPacket.getCommand());

		final Optional<PacketTask> any = queue.parallelStream().filter(packetTask::equals).findAny();

		if(any.isPresent() && !requestPacket.getCommand()) {
			any.get().tasks.add(task);
		}else {
			packetTask.tasks.add(task);
			queue.add(packetTask);
		}

		return task;
	}

	@Override
	public boolean isOpen(String serialPort) {
		return this.serialPort.isOpen(serialPort);
	}

	private void sendFromQueueFlash(RequestPacket requestPacket) throws IrtSerialPortIOException {
		logger.traceEntry("{}", requestPacket);

		final String portName = requestPacket.getSerialPort();

		final int timeout = Optional.ofNullable(requestPacket.getTimeout()).orElse(100);
		final byte[] bytes = requestPacket.getBytes();

		if (bytes == null || bytes.length == 0) {
			requestPacket.setError("There is no data to send.");
			return;
		}

		final Integer baudrate = requestPacket.getBaudrate();
		serialPortFlash.setExpectedLength(requestPacket.getExpectedLength());
		byte[] received = serialPortFlash.send(portName, timeout, bytes, baudrate);
		if (received == null)
			return;

		logger.error("{} : {}", received.length, received);

		requestPacket.setAnswer(received);
		logger.debug(requestPacket);
		
	}

	private int oldPacketId = Integer.MIN_VALUE;
	private void sendFromQueue(RequestPacket requestPacket) throws IrtSerialPortIOException, InterruptedException {
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

//		logger.error("{} : {}", received.length, received);

		final byte flagSequence = (byte) (requestPacket.getId() == ConsoleRestController.PACKET_ID ? '\n' : Packet.FLAG_SEQUENCE);
		received = read(received, portName, timeout, baudrate, flagSequence);

		Packet packet = new Packet(received, requestPacket.getUnitAddr()==0);
		if(oldPacketId==Integer.MIN_VALUE && packet.getPacketId()!=requestPacket.getId()){
			oldPacketId = packet.getPacketId();
			sendAcknowlegement(packet, portName, baudrate);
			sendFromQueue(requestPacket);
			return;
		}
		oldPacketId = Integer.MIN_VALUE;

		final int lastIndex = packet.getLastIndex() + 1;
		received = read(Arrays.copyOfRange(received, lastIndex, received.length), portName, timeout, baudrate, flagSequence);


		requestPacket.setAnswer(received);
		logger.debug(requestPacket);

		sendAcknowlegement(packet, portName, baudrate);
	}

	private void sendAcknowlegement(Packet packet, final String portName, final Integer baudrate) {

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

	byte[] read(final byte[] bytes, String portName, Integer timeout, Integer baudrate, byte flagSequence) throws IrtSerialPortIOException, InterruptedException{
		logger.traceEntry("bytes={}, portName={}, timeout={}, baudrate={}", bytes, portName, timeout, baudrate);

		if(flagSequence==Packet.FLAG_SEQUENCE) {
			if(IntStream.range(0, bytes.length).parallel().filter(i->bytes[i]==flagSequence).count()>=2)
				return bytes;
		}else if(IntStream.range(0, bytes.length).parallel().filter(i->bytes[i]==flagSequence).findAny().isPresent())
			return bytes;

		final byte[] bs = serialPort.read(portName, timeout, baudrate);
		if(bs==null)
			return bytes;

		if(bytes.length==0){
			TimeUnit.MILLISECONDS.sleep(10);
			return read(bs, portName, timeout, baudrate, flagSequence);
		}

		TimeUnit.MILLISECONDS.sleep(10);
		final byte[] copyOf = Arrays.copyOf(bytes, bytes.length + bs.length);
		System.arraycopy(bs, 0, copyOf, bytes.length, bs.length);
		return read(copyOf, portName, baudrate, baudrate, flagSequence);
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

	private Set<String> lockedPorts = new HashSet<>();
	@Override
	public void lockPort(String spName) {
		if(!lockedPorts.contains(spName)) {
			logger.info("Locking port: {}", spName);
			lockedPorts.add(spName);
		}
	}
	@Override
	public void unlockPort(String spName) {
		if(lockedPorts.remove(spName))
			logger.info("Unlocking port: {}", spName);
	}
}
