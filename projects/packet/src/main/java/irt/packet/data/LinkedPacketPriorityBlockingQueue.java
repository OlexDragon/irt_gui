package irt.packet.data;

import java.util.Observer;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.packet.interfaces.PacketToSend;

public class LinkedPacketPriorityBlockingQueue extends PriorityBlockingQueue<PacketToSend> {
	private static final long serialVersionUID = 3467203083495384001L;

	private final Logger logger = LogManager.getLogger();

	@Override
	/** remove duplicates packet and add new */
	public boolean add(PacketToSend packet) {

		final Predicate<? super PacketToSend> filter = (p) -> {

			try {
				if (packet.equals(p)) {

					for (Observer o : p.getObservers())
						packet.addObserver(o);

					return true;
				}

			} catch (Exception e) {
				LogManager.getLogger().catching(e);
			}
			return false;
		};

		if (removeIf(filter))
			logger.debug("Paket removed:{}", packet);

		return super.add(packet);
	}
}
