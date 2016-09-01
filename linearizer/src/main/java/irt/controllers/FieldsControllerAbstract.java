package irt.controllers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controllers.serial_port.SerialPortController;
import irt.data.LinkedPacketsQueue;
import irt.data.MyThreadFactory;
import irt.data.packets.interfaces.FieldController;
import irt.data.packets.interfaces.LinkedPacket;

public abstract class FieldsControllerAbstract extends Observable implements Observer, FieldController  {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	private static boolean error;

	protected abstract void 	updateFields(LinkedPacket packet) throws Exception;
	protected abstract Duration getPeriod();

	private 		String 			name; 			public String getName() { return name; } 		public void setName(String name) { this.name = name; }

	protected 		Observer 		observer 		= this;
	private final 	PacketSender 	packetSender 	= new PacketSender();
	protected ScheduledFuture<?> 	scheduleAtFixedRate;

	protected final Executor executor = Executors.newFixedThreadPool(10, new MyThreadFactory());

	protected FutureTask<Boolean> task;

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

		task = new FutureTask<>(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				if(observable instanceof LinkedPacket) 
					update((LinkedPacket) observable);

				return error;
			}

			public void update(LinkedPacket p) {
				if( p.getAnswer()!=null)
					try {

						updateFields(p);

						error = false;

					} catch (Exception e) {
						if(!error){	//not to repeat the same error message
							error = true;
							logger.catching(e);
						}
					}
				else if(!error){	//not to repeat the same error message
					error = true;
					logger.warn("No Answer: {}", p);
				}
			}
		});
		executor.execute(task);
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
			logger.traceEntry();

			packetsToSend
			.stream()
			.forEach(packet->SerialPortController.getQueue().add(packet, true));
		}
	}
}
