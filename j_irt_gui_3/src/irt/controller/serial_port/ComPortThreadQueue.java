package irt.controller.serial_port;

import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;
import irt.data.value.StaticComponents;
import irt.tools.panel.head.Console;

import java.awt.Color;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.event.EventListenerList;

import jssc.SerialPortException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class ComPortThreadQueue extends Thread {

	private final Logger logger = (Logger) LogManager.getLogger();

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300);
	private static ComPort serialPort;

	public ComPortThreadQueue(){
		super("ComPortThreadQueue");

		int priority = getPriority();
		if(priority>Thread.MIN_PRIORITY)
			setPriority(priority-1);
		setDaemon(true);
		start();
	}

	@Override
	public void run() {

		boolean sent = false;
		while(true){
			try {

				PacketWork packetWork = comPortQueue.take();

				if(serialPort!=null){
					PacketThread packetThread = packetWork.getPacketThread();

					if(packetThread!=null){
						packetThread.join();

						if(serialPort.isOpened() && packetThread.isReadyToSend()) {
							sent = false;

							logger.trace("Data to send - {}", packetWork);
							Packet send = serialPort.send(packetWork);
							firePacketListener(send);
							logger.trace("Received data - {}", send);

							if(send==null || send.getHeader()==null)
								StaticComponents.getLedRx().setLedColor(Color.WHITE);
							else if(send.getHeader().getPacketType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE)
								StaticComponents.getLedRx().setLedColor(Color.RED);
							else
								StaticComponents.getLedRx().setLedColor(Color.GREEN);

							StaticComponents.getLedRx().blink();
						}else if(!sent){
							sent = true;
							logger.warn("Serial port or Packet is not ready:\n{}", packetWork);
						}
					}else
						logger.warn("packetThread==null; ({})", packetWork);
//					if(comPortQueue.isEmpty()){
//						serialPort.closePort();
//					}
				}else
					logger.warn("serialPort==null");
			} catch (Exception e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortThreadQueue:run");
			}
		}
	}

	public synchronized void add(PacketWork packetWork){
			
		try {
			if(packetWork!=null)
				if (comPortQueue.size() < 300)
					if (!comPortQueue.contains(packetWork)) {

						packetWork.getPacketThread().start();

						comPortQueue.add(packetWork);
						logger.trace("<<< is added - {}", packetWork);
					} else{
						comPortQueue.remove(packetWork);
						comPortQueue.add(packetWork);
						logger.warn("Already contains. Is Replaced" + packetWork);
					}
				else
					logger.warn("comPortQueue is FULL");
			else
				logger.warn("packetWork!=null");
		} catch (IllegalStateException e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:add");
		}
	}

	public void clear(){
		comPortQueue.clear();
	}

	public int size() {
		return comPortQueue.size();
	}

	public ComPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(ComPort serialPort) {

		ComPort oldSerialPort = ComPortThreadQueue.serialPort;

		if(oldSerialPort!=null){
			logger.warn("oldSerialPort={}, serialPort={}", oldSerialPort, serialPort);
			clear();
			oldSerialPort.setRun(false, "Reset Serial Port");
			try {
				synchronized (ComPortThreadQueue.serialPort) {
					oldSerialPort.closePort();
				}
			} catch (SerialPortException e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort 1");
			}
		}

		ComPortThreadQueue.serialPort = serialPort;

		if(serialPort.getPortName().startsWith("COM"))
		try {

			serialPort.openPort();

		} catch (SerialPortException e) {
			logger.catching(e);
			Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort 2");
		}
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList packetListeners = new EventListenerList();

	public void addPacketListener(PacketListener packetListener) {
		packetListeners.add(PacketListener.class, packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		packetListeners.remove(PacketListener.class, packetListener);
	}

	public void firePacketListener(Packet packet) {
		Object[] listeners = packetListeners.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			Object l = listeners[i];
			if (l == PacketListener.class)
				((PacketListener) listeners[i + 1]).packetRecived(packet);
		}
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
