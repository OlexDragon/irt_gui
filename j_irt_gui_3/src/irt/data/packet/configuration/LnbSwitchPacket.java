
package irt.data.packet.configuration;

import java.util.Optional;

import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;

public class LnbSwitchPacket extends ConfifurationPacket {

	public LnbSwitchPacket(byte linkAddr, LnbPosition redundancyEnable) {
		super(
				linkAddr,
						PacketWork.PACKET_ID_CONFIGURATION_DLRS_WGS_SWITCHOVER,
						PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER,
						redundancyEnable!=null ? redundancyEnable.toBytes() : null);
	}

	public LnbSwitchPacket() {
		this((byte)0, null);
	}

	@Override
	public Object getValue() {
		final LnbPosition[] values = LnbPosition.values();
		return Optional
				.ofNullable(getPayloads())
				.map(pls->pls.parallelStream())
				.flatMap(stream->stream.findAny())
				.map(Payload::getBuffer)
				.map(d->d[0])
				.filter(index->index<values.length)
				.map(index->values[index]);
	}

	public enum LnbPosition{
		UNKNOWN,
		LNB1,
		LNB2;

		public byte[] toBytes(){
			return new byte[]{(byte) ordinal()};
		}
	}
}
