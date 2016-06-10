package irt.gui.data.packet.observable.device_debug;

import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

public class DAC2Packet extends RegisterPacket{

	private static final int BUC_ADDR = 2;
	private static final int BUC_INDEX = 100;
	private static final int FCM_ADDR = 0;
	private static final int FCM_INDEX = 2;

	public DAC2Packet(byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(answer, hasAcknowledgment);
	}

	public DAC2Packet(Integer value) throws PacketParsingException {
		super(new RegisterValue(BUC_INDEX, BUC_ADDR, value));
	}

	@Override
	public synchronized void setLinkHeaderAddr(byte addr) {

		if(addr == getLinkHeader().getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		final Payload payload = payloads.get(0);
		final int length = payload.getBuffer().length;
		if(addr == -1)	// -1 -> converter
			if(length==12)
				payload.setBuffer(FCM_INDEX, FCM_ADDR, payload.getInt(2));
			else
				payload.setBuffer(FCM_INDEX, FCM_ADDR);
		else
			if(length==12)
				payload.setBuffer(BUC_INDEX, BUC_ADDR, payload.getInt(2));
			else
				payload.setBuffer(BUC_INDEX, BUC_ADDR);
	}

}
