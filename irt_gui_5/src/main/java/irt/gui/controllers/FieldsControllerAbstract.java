package irt.gui.controllers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.interfaces.LinkedPacket;

public abstract class FieldsControllerAbstract extends Observable implements Observer, FieldController  {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	private static boolean wasShown;

	protected abstract void 	updateFields(LinkedPacket packet) throws Exception;
	protected abstract Duration getPeriod();

	private 		String 			name; 			public String getName() { return name; } 		public void setName(String name) { this.name = name; }

	protected 		Observer 		observer 		= this;
	private final 	PacketSender 	packetSender 	= new PacketSender();
	private ScheduledFuture<?> 		scheduleAtFixedRate;

	public void addLinkedPacket(LinkedPacket linkedPacket){
		packetSender.addPacketToSend(linkedPacket);
	}

	public void removeLinkedPacket(LinkedPacket linkedPacket){
		packetSender.removePacketToSend(linkedPacket);
	}

	/** update = true - start sending the packages to the device, false - stop*/
	public void doUpdate(boolean update) {
		if(update) {
			if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
				scheduleAtFixedRate = LinkedPacketsQueue.SERVICES.scheduleAtFixedRate(packetSender, 1, getPeriod().toMillis(), TimeUnit.MILLISECONDS);

		}else if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(false);
	}

	@Override
	public void update(Observable observable, Object object) {
		logger.entry(observable, object);
		startThread(new Thread(new Runnable() {

			@Override
			public void run() {
				if(observable instanceof LinkedPacket) 
					update((LinkedPacket) observable);

				if(object instanceof LinkedPacket)
					update((LinkedPacket) object);
			}

			public void update(LinkedPacket p) {
				if( p.getAnswer()!=null)
					try {
						wasShown = false;

						updateFields(p);

					} catch (Exception e) {
						logger.catching(e);
					}
				else if(!wasShown){
					wasShown = true;
					logger.warn("No Answer: {}", p);
				}
			}
		}));
	}

	private void startThread(final Thread thread) {
		int priority = thread.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(--priority);
		thread.setDaemon(true);
		thread.start();
	}

	//*********************************************   InfoPacketSender   ****************************************************************
	protected class PacketSender implements Runnable{

		private final List<LinkedPacket> 	packetsToSend = new ArrayList<>(); 

		private boolean 			send;			public boolean isSend() { return send; }
		public void setSend(boolean send) {
			this.send = send;
		}		

		public void addPacketToSend(LinkedPacket linkedPacket){

			if(!packetsToSend.contains(linkedPacket)){

				packetsToSend.add(linkedPacket);
				linkedPacket.addObserver(observer);
			}
		}
		public void removePacketToSend(LinkedPacket linkedPacket) {
			packetsToSend.remove(linkedPacket);
			linkedPacket.deleteObserver(observer);
		}

		@Override
		public void run(){
			logger.entry();

			packetsToSend
			.stream()
			.forEach(packet->SerialPortController.QUEUE.add(packet, true));
		}
	}
}
