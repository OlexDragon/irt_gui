package irt.data.event;

import java.awt.AWTEvent;

import irt.data.packet.PacketID;

@SuppressWarnings("serial")
public class ValueChangeEvent  extends AWTEvent {

	public ValueChangeEvent(Object source, PacketID packetID) {
		super(source, packetID.getId());
	}

	@Override
	public String toString() {
		return "ValueChangeEvent [id="+id+", source="+source+"("+source.getClass().getSimpleName()+")]";
	}
}
