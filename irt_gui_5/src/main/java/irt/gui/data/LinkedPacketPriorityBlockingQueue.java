package irt.gui.data;
import java.util.concurrent.PriorityBlockingQueue;

import irt.gui.data.packet.interfaces.LinkedPacket;

public class LinkedPacketPriorityBlockingQueue extends PriorityBlockingQueue<LinkedPacket> {
	private static final long serialVersionUID = 3467203083495384001L;

	@Override
	/** remove duplicates packet and add new */
	public boolean add(LinkedPacket linkedPacket) {

		if(contains(linkedPacket))
			remove(linkedPacket);

		return super.add(linkedPacket);
	}

}
