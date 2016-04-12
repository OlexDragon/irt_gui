
package irt.controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.packet.AlarmStatusPacket;

public class AlarmPacketSender{

	private final Logger logger = LogManager.getLogger();

	private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private final ScheduledFuture<?> processHandle;

	private final AlarmStatusPacket packet; 			public AlarmStatusPacket getPacket() { return packet; }

	public AlarmPacketSender(byte linkAddr, short alarmId) {

		packet = new AlarmStatusPacket(linkAddr, alarmId); 

		final ComPortThreadQueue comPortThreadQueue = GuiControllerAbstract.getComPortThreadQueue();

		final Runnable process = new Runnable() {
			@Override
			public void run() {
				try{

				comPortThreadQueue.add(packet);

				}catch(Exception e){
					logger.catching(e);
				}
			}
		};
		processHandle = scheduler.scheduleAtFixedRate(process, 1, 3, TimeUnit.SECONDS);

	}

	public void destroy(){
		processHandle.cancel(true);
		scheduler.shutdownNow();
	}
}
