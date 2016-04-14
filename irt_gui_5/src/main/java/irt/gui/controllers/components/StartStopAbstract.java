package irt.gui.controllers.components;

import java.util.HashSet;
import java.util.Objects;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.data.packet.interfaces.LinkedPacket;

public abstract class StartStopAbstract implements Runnable, Observer{

	protected final Logger logger = LogManager.getLogger(getClass().getName());
	protected final			Set<LinkedPacket>	packets					= new HashSet<>();						//to get set value 
	protected 				ScheduledFuture<?> 	scheduleAtFixedRate;
	//time between requests
	private 				long 				period = 10000;			public long getPeriod(){ return period; }	public void setPeriod(long period){ this.period = period; }	

	@Override public void run() {
		logger.entry(packets);
		try{

			packets
			.stream()
			.forEach(packet->SerialPortController.QUEUE.add(packet, true));

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public synchronized void start() {
		if(!packets.isEmpty() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = LinkedPacketsQueue.SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
	}

	public synchronized void stop(boolean mayInterruptIfRunning) {
		Optional
		.ofNullable(scheduleAtFixedRate)
		.ifPresent(schedule -> schedule.cancel(mayInterruptIfRunning));
	}

	protected void addPacket(LinkedPacket packet){

		packets.add(Objects.requireNonNull(packet));
		packet.addObserver(this);
	}

	protected void removeAllPackets(){
		logger.entry();

		packets
		.parallelStream()
		.forEach(p->p.deleteObservers());

		packets.clear();
	}
}
