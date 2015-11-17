package irt.gui.controllers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.LinkedPacketPriorityBlockingQueue;
import irt.gui.data.packet.interfaces.LinkedPacket;

public class LinkedPacketsQueue implements Runnable {

	private final Logger logger = LogManager.getLogger();

	private LinkedPacketPriorityBlockingQueue blockingQueue = new LinkedPacketPriorityBlockingQueue();
	private volatile LinkedPacketSender comPort;

	public LinkedPacketsQueue(){
		ScheduledServices.services.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
	}

	private boolean warnReported;
	@Override
	public void run() {
		logger.entry();

		try {

			LinkedPacket packet = blockingQueue.take();

			if (comPort != null && comPort.isOpened()) {

				comPort.send(packet);
				logger.trace("\n\tResived data - {}", packet);
				warnReported = false;

			} else if(!warnReported){
				logger.warn("serialPort==null");
				warnReported = true;
			}

		} catch (Exception e) {
			logger.catching(e);
		}

		logger.exit();
	}

	public synchronized void add(LinkedPacket packet){
		logger.entry(packet);

		if(packet!=null)
			blockingQueue.add(packet);
		else
			logger.warn("packetWork!=null");
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
}
