package irt.gui.data;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.packet.interfaces.LinkedPacket;

public class LinkedPacketPriorityBlockingQueue extends PriorityBlockingQueue<LinkedPacket> {
	private static final long serialVersionUID = 3467203083495384001L;

	private final Logger logger = LogManager.getLogger();
	private final LinkedPacketFilter filter = new LinkedPacketFilter();

	@Override
	/** remove duplicates packet and add new */
	public boolean add(LinkedPacket linkedPacket) {

		filter.setLincedPacket(linkedPacket);
		if(removeIf(filter))
			logger.info("Paket removed:{}", linkedPacket);

		return super.add(linkedPacket);
	}
}
