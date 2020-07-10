package irt.gui.data.packet.observable.device_debug;

import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Payload;
import irt.gui.errors.PacketParsingException;

public class DAC3Packet extends RegisterPacket{

	private static final int BUC_ADDR = 3;
	private static final int BUC_INDEX = 100;
	private static final int FCM_ADDR = 0;
	private static final int FCM_INDEX = 3;

	public DAC3Packet(byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(answer, hasAcknowledgment);
	}

	public DAC3Packet(Integer value) throws PacketParsingException {
		super("DAC3Packet", new RegisterValue(BUC_INDEX, BUC_ADDR, value));
	}

	@Override
	public synchronized boolean setLinkHeaderAddr(byte addr) {

		if(!super.setLinkHeaderAddr(addr))
			return false;

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
		return true;
	}

}
