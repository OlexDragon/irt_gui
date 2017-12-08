package irt.gui.data.packet.observable.device_debug;

import irt.gui.IrtGuiProperties;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.observable.PacketAbstract5;
import irt.gui.errors.PacketParsingException;

public class DACPacket extends RegisterPacket{

	public static final String BUC_PROPERTIES	= "gui.converter.DAC.%d";
	public static final String FCM_PROPERTIES 	= "gui.converter.DAC.%d.FCM";

	public enum UnitType{
		FCM,
		BUC
	}

	public enum DACs{
		DAC1,
		DAC2,
		DAC3,
		DAC4,
		DAC5;

		public int getIndex(UnitType type) {
			final String format = String.format(type==UnitType.BUC ? BUC_PROPERTIES : FCM_PROPERTIES, ordinal()+1) + ".index";
				return IrtGuiProperties.getLong(format).intValue();
		}

		public int getAddress(UnitType type) {
			final String format = String.format(type==UnitType.BUC ? BUC_PROPERTIES : FCM_PROPERTIES, ordinal()+1) + ".address";
			return IrtGuiProperties.getLong(format).intValue();
		}

		public int getPeriod() {
			final String format = String.format(BUC_PROPERTIES, ordinal()+1) + ".period";
			return IrtGuiProperties.getLong(format).intValue();
		}
	}

	private DACs dac;

	public DACPacket(byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(answer, hasAcknowledgment);
	}

	public DACPacket(DACs dac, Integer value) throws PacketParsingException {
		super(new RegisterValue(dac.getIndex(UnitType.BUC), dac.getAddress(UnitType.BUC), value));
		this.dac = dac;
	}

	@Override
	public synchronized void setLinkHeaderAddr(byte addr) {

		if(addr == getLinkHeader().getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		final Payload payload = payloads.get(0);
		final int length = payload.getBuffer().length;

		UnitType ut = addr == PacketAbstract5.CONVERTER_ADDR ? UnitType.FCM : UnitType.BUC;
		
		if(length==12)
			payload.setBuffer(dac.getIndex(ut), dac.getAddress(ut), payload.getInt(2));
		else
			payload.setBuffer(dac.getIndex(ut), dac.getAddress(ut));

	}

	public void setValue(int intValue) {
		final Payload payload = payloads.get(0);
		payload.setBuffer(payload.getInt(0), payload.getInt(1), intValue);
	}

}
