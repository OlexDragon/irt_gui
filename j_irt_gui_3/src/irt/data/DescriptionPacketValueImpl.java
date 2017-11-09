package irt.data;

import irt.controller.interfaces.DescriptionPacketValue;
import irt.controller.translation.Translation;
import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.interfaces.RangePacket;
import irt.data.value.Value;

public abstract class DescriptionPacketValueImpl implements Comparable<DescriptionPacketValueImpl>, DescriptionPacketValue{

	protected DescriptionPacketValueImpl( String description, RangePacket rangePacket, PacketWork packet) {
		this.description = description;
		this.rangePacket = rangePacket;
		this.packet = packet;
	}

	public abstract Value getValue(long min, long max);

	private final String 		description; 	public String 		getDescription	() { return description; }
	private final RangePacket 	rangePacket; 	public RangePacket 	getRangePacket	() { return rangePacket; }
	private final PacketWork	packet; 		public PacketWork	getPacketWork	() { return packet; 	 }

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return description.hashCode();
	}

	@Override
	public int compareTo(DescriptionPacketValueImpl o) {
		return description.compareTo(o.description);
	}

	@Override
	public String toString() {
		return Translation.getValue(String.class, description, description);
	}
}
