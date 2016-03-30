
package irt.gui.data;

import java.util.Observer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;

import irt.gui.data.packet.interfaces.LinkedPacket;

public class LinkedPacketFilter implements Predicate<LinkedPacket> {

	private LinkedPacket linkedPacket;

	public void setLincedPacket(LinkedPacket packet) {
		this.linkedPacket = packet;
	}

	@Override
	public boolean test(LinkedPacket packet) {

		try {
			if (linkedPacket.equals(packet)) {

				for (Observer o : packet.getObservers())
					linkedPacket.addObserver(o);

				return true;
			}

		} catch (Exception e) {
			LogManager.getLogger().catching(e);
		}
		return false;
	}

}
