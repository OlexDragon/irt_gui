package irt.data;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controllers.serial_port.LinkedPacketSender;
import irt.data.packets.core.LinkHeader;
import irt.data.packets.interfaces.PacketToSend;

public class LinkedPacketsQueue implements Runnable {

	public static final ScheduledExecutorService SERVICES = Executors.newScheduledThreadPool(10, new MyThreadFactory());

	private final Logger logger = LogManager.getLogger();

	private LinkedPacketPriorityBlockingQueue blockingQueue = new LinkedPacketPriorityBlockingQueue();
	private volatile LinkedPacketSender comPort;

	private byte unitAddress = (byte) 254;
	private boolean runServer = true; public boolean isRunServer() { return runServer; } public void setRunServer(boolean runServer) { this.runServer = runServer; }

	public LinkedPacketsQueue(){
		SERVICES.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
	}

	private boolean warnReported;

	private boolean error;

	@Override
	public void run() {
		logger.traceEntry();

		try {

			PacketToSend packet = blockingQueue.take();

//			logger.error(packet);

			if (comPort != null && comPort.isOpened()) {

				comPort.send(packet);
				warnReported = false;

			} else{
				if(!warnReported){
					logger.warn("serialPort==null");
					warnReported = true;
				}

			}

			error = false;
		} catch (Exception e) {
			if(!error){	// not to repeat the same error message
				error = true;
				logger.catching(e);
			}
		}

		logger.traceExit();;
	}

	public synchronized void add(PacketToSend packet, boolean checkUnitAddress){
		logger.entry(packet);

		if(packet==null){
			logger.warn("packetWork!=null");
			return;
		}

		if(checkUnitAddress)
			checkUnitAddress(packet);

		blockingQueue.add(packet);

		//		logger.error("{}:{}", unitAddress, packet);
//		logger.error(blockingQueue.size());
	}

	public void clear(){
		blockingQueue.clear();
	}

	public int size() {
		return blockingQueue.size();
	}

	public void setComPort(LinkedPacketSender serialPort) {

		this.comPort = serialPort;
	}

	public void setUnitAddress(byte address) {
		unitAddress = address;
	}

	private void checkUnitAddress(PacketToSend packet) {
		final LinkHeader linkHeader = packet.getLinkHeader();
		final byte addr = linkHeader.getAddr();
		if(addr!=unitAddress)
			packet.setLinkHeaderAddr(unitAddress);
	}
}
