
package irt.data;

import irt.data.packet.ALCPacket;
import irt.data.packet.ALCRangePacket;
import irt.data.value.Value;
import irt.data.value.ValueDouble;

public class ALCSet extends DescriptionPacketValueImpl {

	public ALCSet( byte linkAddr) {
		super("ALC", new ALCRangePacket(linkAddr), new ALCPacket(linkAddr, null));
	}

	@Override
	public Class<?> getParameterType(){
		return Short.class;
	}

	@Override
	public Value getValue(long min, long max) {
		final ValueDouble value = new ValueDouble(min, min, max, 1);
		value.setPrefix("dB");
		return value;
	}

	@Override
	public String getDeaultStep() {
		return "1dB";
	}
}
