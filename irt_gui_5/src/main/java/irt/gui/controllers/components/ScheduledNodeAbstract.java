package irt.gui.controllers.components;

import java.util.HashSet;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.interfaces.UnitAddress;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

public abstract class ScheduledNodeAbstract implements Runnable, Observer, UnitAddress{
	protected final Logger logger = LogManager.getLogger(getClass().getName());

	public static final String NAME = "name";
	public static final String PERIOD = "period";

	protected static final 	ScheduledExecutorService SERVICES 			= ScheduledServices.services;
	protected 				ScheduledFuture<?> 	scheduleAtFixedRate;
	protected 				String 				propertyName; 			public String getPropertyName() { return propertyName; }
	//time between requests
	private 				long 				period = 10000;			public long getPeriod(){ return period; }	public void setPeriod(long period){ this.period = period; }	

	private final			Set<LinkedPacket>	packets					= new HashSet<>();						//to get set value

	public abstract void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException;

	@Override public void run() {
		logger.entry(packets);
		try{

			packets
			.stream()
			.forEach(packet->SerialPortController.QUEUE.add(packet));

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public synchronized void start() {
		if(!packets.isEmpty() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = SERVICES.scheduleAtFixedRate(this, 1, period, TimeUnit.MILLISECONDS);
	}

	public synchronized void stop(boolean mayInterruptIfRunning) {
		Optional
		.ofNullable(scheduleAtFixedRate)
		.ifPresent(schedule -> schedule.cancel(mayInterruptIfRunning));
	}

	protected void addPacket(LinkedPacket packet){
		logger.entry(packet);

		Optional.of(packet)
		.ifPresent(p->{

			p.addObserver(this);
			packets.add(p);
		});
	}

	protected void removeAllPackets(){
		logger.entry();

		packets
		.parallelStream()
		.forEach(p->p.deleteObservers());

		packets.clear();
	}

	@Override public void setUnitAddress(Byte address) {

		packets
		.parallelStream()
		.map(LinkedPacket::getLinkHeader)
		.forEach(lh->lh.setAddr(address));
	}

	//******************************** class TooltipWorker   ********************************************
	class TooltipWorker implements Runnable{

		private Control node;
		private String message;
		private volatile boolean isRunning;

		public TooltipWorker(Control node) {
			super();
			this.node = node;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			if(isRunning)
				return;

			isRunning = true;

			final Tooltip tooltip = node.getTooltip();
			Platform.runLater(()->node.setTooltip(new Tooltip(message)));

			try { Thread.sleep(10000); } catch (Exception e) { }

			Platform.runLater(()->node.setTooltip(tooltip));

			isRunning = false;
		}
		
	}
}
