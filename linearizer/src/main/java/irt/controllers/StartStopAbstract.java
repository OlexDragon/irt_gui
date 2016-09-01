package irt.controllers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controllers.serial_port.SerialPortController;
import irt.data.LinkedPacketsQueue;
import irt.data.packets.interfaces.FieldController;
import irt.data.packets.interfaces.LinkedPacket;

public abstract class StartStopAbstract implements Runnable, Observer, FieldController{

	protected final Logger logger = LogManager.getLogger(getClass().getName());
	protected final			Set<LinkedPacket>	packets					= new HashSet<>();	public Set<LinkedPacket> getPackets() { return packets; }

	protected 				ScheduledFuture<?> 	scheduleAtFixedRate;
	//time between requests
	private long period = 10000;			public long getPeriod(){ return period; }	public void setPeriod(long period){ this.period = period; }	

	@Override public void run() {
//		logger.error(packets);
		try{

			send();

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	/**  @return true if status have been changed  */
	public synchronized boolean start() {
		boolean start = false;
		if(!packets.isEmpty() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())){
			scheduleAtFixedRate = LinkedPacketsQueue.SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
			start = true;
		}
		return start;
	}

	public synchronized void stop(boolean mayInterruptIfRunning) {
//			logger.error(this.getPackets());
		Optional
		.ofNullable(scheduleAtFixedRate)
		.filter(sh->!sh.isCancelled())
		.ifPresent(schedule -> schedule.cancel(mayInterruptIfRunning));
	}

	protected void addPacket(LinkedPacket packet){

		packets.add(Objects.requireNonNull(packet));
		packet.addObserver(this);
	}

	protected void removeAllPackets(){
		logger.traceEntry();

		packets
		.parallelStream()
		.forEach(p->p.deleteObservers());

		packets.clear();
	}

	@Override
	public void doUpdate(boolean doUpdate) {
		logger.entry(doUpdate);

		if(doUpdate)
			start();
		else
			stop(true);
	}

	public void send() {

		packets
		.stream()
		.forEach(packet->SerialPortController.getQueue().add(packet, true));
	}
}
