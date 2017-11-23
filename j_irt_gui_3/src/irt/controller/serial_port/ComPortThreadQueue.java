package irt.controller.serial_port;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.panel.head.Console;
import jssc.SerialPortException;

public class ComPortThreadQueue implements Runnable {

	private final Logger logger = (Logger) LogManager.getLogger();

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300, Collections.reverseOrder());
	private final ScheduledExecutorService services = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private static ComPort serialPort;

	public ComPortThreadQueue(){
		services.scheduleAtFixedRate(this, 10, 10, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		boolean sent = false;
		PacketWork packetWork = null;
		try {

			packetWork = comPortQueue.take();
			logger.trace(packetWork);

			if (serialPort != null) {
				logger.debug("serial port present");
				PacketThreadWorker packetThread = packetWork.getPacketThread();

				if (packetThread != null) {
					logger.debug("packetThread present");
					packetThread.join();

					logger.debug("packetThread.join() done");
					Packet packet = packetThread.getPacket();
					if (packet == null) {
						logger.warn(packetWork);
						return;
					}

					if (serialPort.isOpened()) {
						logger.debug("serialPort.isOpened() = true");
						sent = false;

						Packet send = serialPort.send(packetWork);
						firePacketListener(send);

					} else if (!sent) {
						sent = true;
						logger.warn("Serial port or Packet is not ready:\n{}", packetWork);
					}
				} else{
					logger.warn("packetThread==null; ({})", packetWork);
				}
			} else
				logger.warn("serialPort==null");

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortThreadQueue:run");
		}
	}

	public synchronized void add(PacketWork packetWork){
//		if(packetWork instanceof AlarmDescriptionPacket)
//			logger.error(packetWork);//catching(new Throwable());

		try {

			if(packetWork!=null){
				if (comPortQueue.size() < 300){
					if (!comPortQueue.contains(packetWork)) {

						packetWork.getPacketThread().start();

						comPortQueue.add(packetWork);
						logger.trace("<<< is added - {}", packetWork);
					} else
						logger.warn("Tis packet already in the queue. {}", packetWork);
				}else
					logger.warn("comPortQueue is FULL");
			}else
				logger.warn("packetWork!=null");
		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:add");
		}
	}

	public void clear(){
		logger.entry();
		comPortQueue.clear();
	}

	public int size() {
		return comPortQueue.size();
	}

	public static ComPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(ComPort serialPort) {

		ComPort oldSerialPort = ComPortThreadQueue.serialPort;

		if(oldSerialPort!=null){
			logger.warn("oldSerialPort={}, serialPort={}", oldSerialPort, serialPort);
			clear();
			oldSerialPort.setRun(false, "Reset Serial Port");

			oldSerialPort.closePort();
		}

		ComPortThreadQueue.serialPort = serialPort;

		if(serialPort.getPortName().startsWith("COM") || serialPort.getPortName().startsWith("/dev"))
		try {

			serialPort.openPort();

		} catch (Exception e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort 2");
		}
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

	public void close() throws SerialPortException {
		if(serialPort.isOpened())
			serialPort.closePort();
		serialPort = null;

		comPortQueue.clear();
		comPortQueue = null;
	}
}
