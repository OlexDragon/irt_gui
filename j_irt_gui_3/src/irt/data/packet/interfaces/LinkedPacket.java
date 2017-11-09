
package irt.data.packet.interfaces;

import irt.data.packet.LinkHeader;

public interface LinkedPacket extends Packet {

	LinkHeader getLinkHeader();

}
