
package irt.data;

import java.util.Observer;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;

import irt.data.packets.interfaces.PacketToSend;

public class PacketFilter implements Predicate<PacketToSend> {

	private PacketToSend linkedPacket;

	public void setLincedPacket(PacketToSend packet) {
		this.linkedPacket = packet;
	}

	@Override
	public boolean test(PacketToSend packet) {

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
