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
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class StartStopAbstract implements Runnable, Observer, FieldController{
	protected final Logger logger = LogManager.getLogger(getClass().getName());

	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
	protected 				ScheduledFuture<?> 	scheduleAtFixedRate;

	protected final			Set<LinkedPacket>	packets	= new HashSet<>();

	//time between requests
	private long period = 10000;

	public StartStopAbstract() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(true)));
	}

	@Override public void run() {
//		logger.error(packets);
		try{

			send();

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	/***/
	public synchronized void start() {
		if(!packets.isEmpty() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())){
			scheduleAtFixedRate = LinkedPacketsQueue.SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
		}
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
		if(doUpdate)
			start();
		else
			stop(true);
	}

	public void send() {
		logger.trace(packets);

		packets
		.stream()
		.forEach(packet->SerialPortController.getQueue().add(packet, true));
	}
}
