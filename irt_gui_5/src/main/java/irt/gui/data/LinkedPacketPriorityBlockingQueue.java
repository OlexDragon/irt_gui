package irt.gui.data;
import java.util.Observer;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.packet.interfaces.LinkedPacket;

public class LinkedPacketPriorityBlockingQueue extends PriorityBlockingQueue<LinkedPacket> {
	private static final long serialVersionUID = 3467203083495384001L;

	private final Logger logger = LogManager.getLogger();

	@Override
	/** remove duplicates packet and add new */
	public boolean add(LinkedPacket linkedPacket) {

		final Predicate<LinkedPacket> filter = new Predicate<LinkedPacket>() {

			@Override
			public boolean test(LinkedPacket packet) {
				if(linkedPacket.equals(packet)){
					try {

						final Observer[] observers = packet.getObservers();

						for(Observer o:observers)
							linkedPacket.addObserver(o);

						packet.deleteObservers();

					} catch (Exception e) {
						logger.catching(e);
					}

					return true;
				}
				return false;
			}
		};

		if(removeIf(filter))
			logger.info("Paket removed:{}", linkedPacket);

		return super.add(linkedPacket);
	}
}
