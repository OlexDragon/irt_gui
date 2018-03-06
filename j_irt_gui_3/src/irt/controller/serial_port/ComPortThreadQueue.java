package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.panel.head.Console;
import purejavacomm.PortInUseException;

public class ComPortThreadQueue implements Runnable {

	private final static Logger logger = LogManager.getLogger();

	public final static int QUEUE_SIZE_TO_DELAY = 5;
	public final static int DELAY_TIMES = 10;
	

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300, Collections.reverseOrder());
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private ScheduledFuture<?> scheduledFuture;

	private static SerialPortInterface serialPort;

	public ComPortThreadQueue(){
		scheduledFuture = service.scheduleAtFixedRate(this, 10, 10, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		boolean sent = false;
		PacketWork packetWork = null;
		try {

			packetWork = comPortQueue.take();
			logger.trace(packetWork);

			if (serialPort != null) {
				PacketThreadWorker packetThread = packetWork.getPacketThread();

					Packet packet = packetThread.getPacket();
					if (packet == null) {
						logger.warn(packetWork);
						return;
					}

					if (serialPort.isOpened()) {
						sent = false;

						Packet send = serialPort.send(packetWork);
						firePacketListener(send);

					} else if (!sent) {
						sent = true;
						String message = String.format("The serial port %s is not ready.", serialPort.getPortName());
						JOptionPane.showMessageDialog(null, message);
						logger.warn("The Serial port is not ready:\n{}", packetWork);
					}
			} else
				logger.warn("serialPort==null");

		} catch (InterruptedException e) {
			Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort());
		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortThreadQueue:run");
		}
	}

	public synchronized void add(PacketWork packetWork){
		logger.entry(packetWork);
//		if(packetWork instanceof DeviceDebugInfoPacket)
//			logger.debug(packetWork.getClass().getSimpleName());//catching(new Throwable());

		try {

			if (comPortQueue.size() < 300){
				if (!comPortQueue.contains(packetWork)) {

					packetWork.getPacketThread().start();

					comPortQueue.add(packetWork);
					logger.trace("<<< is added - {}", packetWork);
				} else
					logger.warn("Tis packet already in the queue. {}", packetWork);
			}else
				logger.warn("comPortQueue is FULL");

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:add");
		}
		logger.debug("queue size: {}\n{}", comPortQueue.size(), comPortQueue);
	}

	public void clear(){
		logger.traceEntry();
		comPortQueue.clear();
	}

	public int size() {
		return comPortQueue.size();
	}

	public static SerialPortInterface getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPortInterface serialPort) {

		SerialPortInterface oldSerialPort = ComPortThreadQueue.serialPort;

		if(oldSerialPort!=null){
			logger.warn("oldSerialPort={}, serialPort={}", oldSerialPort, serialPort);
			clear();
			oldSerialPort.setRun(false, "Reset Serial Port");

			oldSerialPort.closePort();
		}

		ComPortThreadQueue.serialPort = serialPort;

		Optional
		.ofNullable(ComPortThreadQueue.serialPort)
		.filter(sp->sp.getPortName().startsWith("COM") || sp.getPortName().startsWith("/dev"))
		.ifPresent(sp->{
			
			try {

				sp.openPort();

			} catch (PortInUseException e) {

				final String errorMessage = String.format("Seriap port %s is in use.", sp);

				JOptionPane.showMessageDialog(null, errorMessage);

			} catch (Exception e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort 2");
			}
		});
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList packetListeners = new EventListenerList();

	public void addPacketListener(PacketListener packetListener) {

		// Add if not exists
		if(Arrays.stream(packetListeners.getListenerList()).parallel().filter(l->l.equals(packetListener)).map(l->false).findAny().orElse(true))
			packetListeners.add(PacketListener.class, packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		packetListeners.remove(PacketListener.class, packetListener);
	}

	public void firePacketListener(Packet packet) {

		Arrays
		.stream(packetListeners.getListenerList())
		.parallel()
		.filter(PacketListener.class::isInstance)
		.map(PacketListener.class::cast)
		.forEach(l->{
			try{

				l.onPacketRecived(packet);

			}catch (Exception e) {
				logger.catching(e);
			}
		});
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void close() {

		new Thread(()->Optional.ofNullable(serialPort).filter(sp->sp.isOpened()).ifPresent(sp->sp.closePort())).start();

		serialPort = null;

		comPortQueue.clear();
		comPortQueue = null;
	}

	public void stop() {

		close();
		Optional.of(scheduledFuture).filter(shf->!shf.isCancelled()).ifPresent(serv->serv.cancel(true));
		Optional.of(service).filter(serv->!serv.isShutdown()).ifPresent(serv->serv.shutdownNow());
	}
}
