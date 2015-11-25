
package irt.data;

import irt.data.packet.FrequencyPacket;
import irt.data.packet.FrequencyRangePacket;
import irt.data.value.Value;
import irt.data.value.ValueFrequency;

public class FrequencySet extends DescriptionPacketValueImpl {

	public FrequencySet( byte linkAddr) {
		super("Frequency", new FrequencyRangePacket(linkAddr), new FrequencyPacket(linkAddr, null));
	}

	@Override
	public Value getValue(long min, long max) {
		return new ValueFrequency(min, min, max);
	}

	@Override
	public Class<?> getParameterType() {
		return Long.class;
	}

	@Override
	public String getDeaultStep() {
		return "100MHz";
	}
}
