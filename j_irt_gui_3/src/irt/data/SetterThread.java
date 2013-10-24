package irt.data;

import irt.data.packet.Packet;
import irt.data.packet.Payload;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class SetterThread extends PacketThread {

	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final int BYTE = 1;
	public static final int LONG = 2;

	private long valueToSend;
	private int valueType;

	public SetterThread(byte[] packetSetting, int valueToSend, int valueType) {
		super(packetSetting);
		this.valueToSend = valueToSend;
		this.valueType = valueType;
	}

	@Override
	protected Packet newPacket() {
		Packet newPacket = super.newPacket();
		logger.trace("newPacket() = {}", newPacket.getClass().getSimpleName());
		List<Payload> pls = newPacket.getPayloads();
		if(pls!=null)
			switch(valueType){
			case BYTE:
				pls.get(0).setBuffer((byte)valueToSend);
				break;
			case LONG:
				pls.get(0).setBuffer(valueToSend);
			}
		return newPacket;
	}

	public long getValueToSend() {
		return valueToSend;
	}

	public void setValueToSend(long valueToSend) {
		this.valueToSend = valueToSend;
	}

}
