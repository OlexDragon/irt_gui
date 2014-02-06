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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import jssc.SerialPortException;

public class ComPortThreadQueue extends Thread {

	private final Logger logger = (Logger) LogManager.getLogger();

	private PriorityBlockingQueue<PacketWork> comPortQueue = new PriorityBlockingQueue<>(300);
	private static ComPort serialPort;

	public ComPortThreadQueue(){
		super("ComPortThreadQueue");

		setPriority(Thread.currentThread().getPriority()-1);
		start();
	}

	@Override
	public void run() {

		while(true){
			try {

				PacketWork packetWork = comPortQueue.take();

				if(serialPort!=null){
					PacketThread packetThread = packetWork.getPacketThread();

					if(packetThread!=null){
						packetThread.join();

						if(serialPort.getPortName().startsWith("COM") && packetThread.isReadyToSend()) {

							Packet send = serialPort.send(packetWork);
							fireValueChangeListener(send);

							if(send==null || send.getHeader()==null)
								StaticComponents.getLedRx().setLedColor(Color.WHITE);
							else if(send.getHeader().getType()!=Packet.IRT_SLCP_PACKET_TYPE_RESPONSE)
								StaticComponents.getLedRx().setLedColor(Color.RED);
							else
								StaticComponents.getLedRx().setLedColor(Color.GREEN);

							StaticComponents.getLedRx().blink();
						}
					}
//					if(comPortQueue.isEmpty()){
//						serialPort.closePort();
//					}
				}
			} catch (Exception e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortThreadQueue:run");
			}
		}
	}

	public synchronized void add(PacketWork packetWork){
			
		try {

			if (!comPortQueue.contains(packetWork)) {

				PacketThread pt = packetWork.getPacketThread();
				pt.setDaemon(true);
				pt.start();

				comPortQueue.add(packetWork);
				// System.out.println("<<< is added - "+packetWork);
			}
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
			oldSerialPort.setRun(false);
			try {
				synchronized (serialPort) {
					oldSerialPort.closePort();
				}
			} catch (SerialPortException e) {
				logger.catching(e);
				Console.appendLn(e.getLocalizedMessage(), "ComPortQueue:setSerialPort");
			}
		}

		ComPortThreadQueue.serialPort = serialPort;
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private EventListenerList packetListeners = new EventListenerList();

	public void addPacketListener(PacketListener packetListener) {
		packetListeners.add(PacketListener.class, packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		packetListeners.remove(PacketListener.class, packetListener);
	}

	public void fireValueChangeListener(Packet packet) {
		Object[] listeners = packetListeners.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			Object l = listeners[i];
			if (l == PacketListener.class)
				((PacketListener) listeners[i + 1]).packetRecived(packet);
			;
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
