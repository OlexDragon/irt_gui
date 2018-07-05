package irt.data.event;

import java.awt.AWTEvent;

import irt.data.packet.PacketWork.PacketIDs;

@SuppressWarnings("serial")
public class ValueChangeEvent  extends AWTEvent {

	public ValueChangeEvent(Object source, PacketIDs packetID) {
		super(source, packetID.getId());
	}

	@Override
	public String toString() {
		return "ValueChangeEvent [id="+id+", source="+source+"("+source.getClass().getSimpleName()+")]";
	}
}
