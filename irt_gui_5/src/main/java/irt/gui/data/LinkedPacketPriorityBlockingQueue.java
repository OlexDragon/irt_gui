package irt.gui.data;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.packet.interfaces.PacketToSend;

public class LinkedPacketPriorityBlockingQueue extends PriorityBlockingQueue<PacketToSend> {
	private static final long serialVersionUID = 3467203083495384001L;

	private final Logger logger = LogManager.getLogger();
	private final PacketFilter filter = new PacketFilter();

	@Override
	/** remove duplicates packet and add new */
	public boolean add(PacketToSend packet) {

		filter.setLincedPacket(packet);
		if(removeIf(filter))
			logger.info("Paket removed:{}", packet);

		return super.add(packet);
	}
}
