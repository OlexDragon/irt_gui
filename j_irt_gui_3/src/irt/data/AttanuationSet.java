
package irt.data;

import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.AttenuationRangePacket;
import irt.data.value.Value;
import irt.data.value.ValueDouble;

public class AttanuationSet extends DescriptionPacketValueImpl {

	public AttanuationSet( byte linkAddr) {
		super("attenuation", new AttenuationRangePacket(linkAddr), new AttenuationPacket(linkAddr, null));
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
