package irt.gui.controllers.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.ScheduledServices;
import irt.gui.data.packet.interfaces.LinkedPacket;

public abstract class ScheduledNode implements Runnable, Observer {
	protected final Logger logger = LogManager.getLogger(getClass().getName());

	protected static final 	ScheduledExecutorService SERVICES 			= ScheduledServices.services;
	protected 				ScheduledFuture<?> 	scheduleAtFixedRate;
	protected 				String 				name; 					public String getName() { return name; }
	protected 				long 				period;						//time between requests

	private final			List<LinkedPacket>	packets					= new ArrayList<>();						//to get set value

	public abstract void setName(String name);

	public void start() {
		if(!packets.isEmpty() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try{

			packets
			.stream()
			.forEach(packet->SerialPortController.QUEUE.add(packet));

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void stop(boolean leaveEditable) {
		Optional
		.ofNullable(scheduleAtFixedRate)
		.ifPresent(schedule -> schedule.cancel(true));
	}

	protected void addPacket(LinkedPacket packet){
		Optional.of(packet)
		.ifPresent(p->{

			p.addObserver(this);
			packets.add(p);
		});
	}

	protected void removePacket(LinkedPacket packet){
		Optional.ofNullable(packet)
		.ifPresent(p->{

			p.deleteObservers();
			packets.remove(p);
		});
	}
}
