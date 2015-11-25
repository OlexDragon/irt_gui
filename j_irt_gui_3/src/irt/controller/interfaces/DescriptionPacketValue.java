package irt.controller.interfaces;

import irt.data.PacketWork;
import irt.data.packet.RangePacket;
import irt.data.value.Value;

public interface DescriptionPacketValue {

	Class<?> 		getParameterType();
	String 			getDescription	();
	String			getDeaultStep();
	RangePacket 	getRangePacket	();
	PacketWork		getPacketWork	();
	Value 			getValue		(long min, long max);
}
