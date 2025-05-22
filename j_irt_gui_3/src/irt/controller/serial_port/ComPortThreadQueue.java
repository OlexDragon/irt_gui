package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketWork;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import jssc.SerialPortException;

public class ComPortThreadQueue implements Runnable {

	public static final String GUI_CLOSED_CORRECTLY = "gui is closed correctly";

	private final static Logger logger = LogManager.getLogger();

	public final static int QUEUE_SIZE_TO_DELAY = 20;
	public final static int QUEUE_SIZE_TO_RESUME = 5;
	public final static int DELAY_TIMES = 5;
	

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300, Collections.reverseOrder());
	private ScheduledExecutorService service;
	private ScheduledFuture<?> scheduledFuture;

	private static SerialPortInterface serialPort;

	public ComPortThreadQueue(){
		start();
	}

	public void start() {
		logger.traceEntry();

		//Show Stack Trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		clear();

		// do nothing if the job is not finished.
		if(!Optional.ofNullable(service).filter(s->!s.isShutdown() && !s.isTerminated()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("ComPortThreadQueue"));

		if(!Optional.ofNullable(scheduledFuture).filter(s->!s.isCancelled() && !s.isDone()).isPresent())
			scheduledFuture = service.scheduleAtFixedRate(this, 1, 1, TimeUnit.MILLISECONDS);

		Optional.ofNullable(serialPort).ifPresent(openPort());
	}

	@Override
	public void run() {
		logger.traceEntry("{} : {}; serialPort: {}; port is opend: {} ", ()->getClass().getSimpleName(), ()->Thread.currentThread().getName(), ()->serialPort, ()->Optional.ofNullable(serialPort).map(SerialPortInterface::isOpened));

		try {

			PacketWork packetWork = comPortQueue.take();
			logger.debug("Packet to send: {}", packetWork);

			Optional
			.ofNullable(serialPort)
			.ifPresent(sp->{

				Optional
				.ofNullable(packetWork)
				.map(sp::send)
				.ifPresent(this::firePacketListener);
			});

		} catch (InterruptedException e) {
			logger.catching(Level.TRACE, e);
//			Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort());
		} catch (Exception e) {
			logger.catching(e);
		}

		logger.traceExit();
	}

	public synchronized void add(PacketWork packetWork){
		logger.traceEntry("{}", packetWork);
//		Optional.of(packetWork).map(PacketWork::getPacketThread).map(PacketThreadWorker::getPacket).filter(p->PacketID.DEVICE_DEBUG_CPU_INFO.match(p.getHeader().getPacketId())).ifPresent(p->logger.catching(new Throwable()));

		if(serialPort==null || packetWork==null)
			return;

		try {

			if(
					Optional
					.of(packetWork)
					.map(PacketWork::getPacketThread)
					.map(PacketThreadWorker::getPacket)
					.map(Packet::getHeader)
					.map(PacketHeader::getPacketType)
					.filter(t->t==PacketImp.PACKET_TYPE_COMMAND)
					.isPresent()){

				comPortQueue.add(packetWork);
				return;
			}

			if (comPortQueue.size() < 100){
				if (!comPortQueue.contains(packetWork)) {

					packetWork.getPacketThread().start();

					// set packet time stamp
					Optional.of(packetWork).filter(PacketSuper.class::isInstance).map(PacketSuper.class::cast).ifPresent(p->p.setTimestamp(System.nanoTime()));

					comPortQueue.add(packetWork);

					logger.trace("<<< is added - {}", packetWork);
				} else
					logger.warn("Tis packet already in the queue. {}", packetWork);
			}else
				logger.error("comPortQueue is FULL");

		} catch (Exception e) {
			logger.catching(e);
		}
		logger.traceExit();
	}

	public synchronized void clear(){
		comPortQueue.clear();
	}

	public int size() {
		return comPortQueue.size();
	}

	public static SerialPortInterface getSerialPort() {
		return serialPort;
	}

	public synchronized void setSerialPort(SerialPortInterface serialPort) {
		logger.traceEntry("{}", serialPort);
		
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		clear();

		if(serialPort == ComPortThreadQueue.serialPort) {
			Optional.ofNullable(serialPort).ifPresent(openPort());
			return;
		}

		// Close old serial port
		Optional.ofNullable(ComPortThreadQueue.serialPort).ifPresent(SerialPortInterface::closePort);

		ComPortThreadQueue.serialPort = serialPort;

		Optional
		.ofNullable(serialPort)
		.filter(sp->sp.getPortName().startsWith("COM") || sp.getPortName().startsWith("/dev"))
		.ifPresent(openPort());

		logger.traceExit();
	}

	private Consumer<? super SerialPortInterface> openPort() {
		return sp -> {

			try {

				if(!sp.isOpened())
					sp.openPort();

			} catch (SerialPortException e) {

				logger.catching(Level.DEBUG, e);

				final String errorMessage = String.format("Serial port %s is in use.", sp);
				SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(null, errorMessage));

			} catch (Exception e) {
				logger.catching(e);
			}
		};
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList packetListeners = new EventListenerList();

	public void addPacketListener(PacketListener packetListener) {

		// Add if not exists
		if(Arrays.stream(packetListeners.getListenerList()).parallel().filter(l->l.equals(packetListener)).findAny().isPresent())
			return;

		packetListeners.add(PacketListener.class, packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		packetListeners.remove(PacketListener.class, packetListener);
	}

	public void firePacketListener(Packet packet) {
		logger.debug("Recived Packet: {}", packet);

		Arrays
		.stream(packetListeners.getListenerList())
		.parallel()
		.filter(PacketListener.class::isInstance)
		.map(PacketListener.class::cast)
		.forEach(l->{
			try{

				l.onPacketReceived(packet);

			}catch (Exception e) {
				logger.catching(e);
			}
		});
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public synchronized void close() {

		// Show stack trace
//		logger.error(Arrays.stream(Thread.currentThread().getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1 + "\n" + s2).get());

		Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort());
		comPortQueue.clear();
	}

	public void stop() {

		close();
		Optional.of(scheduledFuture).filter(shf->!shf.isCancelled()).ifPresent(serv->serv.cancel(true));
		Optional.of(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
	}
}
