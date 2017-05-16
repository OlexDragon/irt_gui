
package irt.data.packet.interfaces;

import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public interface LinkedPacket extends Packet {

	LinkHeader getLinkHeader();

}
