package irt.services;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.serial.port.controllers.PacketsQueue;

public class GlobalPacketsQueues {
	private static final Logger logger = LogManager.getLogger();

	public static PacketsQueue get(String serviceName){
		logger.entry(serviceName);

		return Optional	.ofNullable((PacketsQueue)GlobalServices.get(serviceName))
						.orElseGet( ()->{
							PacketsQueue pq = new PacketsQueue(serviceName);
							GlobalServices.add(serviceName, pq);
							return pq;
						});
	}

	public static void remove(String serviceName) {
		logger.entry(serviceName);

		Optional.ofNullable((PacketsQueue)GlobalServices.get(serviceName)).ifPresent(queue->{
			queue.stop();
			GlobalServices.remove(serviceName);
		});
	}

}
