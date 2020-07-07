package irt.gui.data.packet.observable.device_debug;

import java.util.Optional;

import irt.gui.IrtGuiProperties;
import irt.gui.data.RegisterValue;
import irt.gui.errors.PacketParsingException;

public class InputPowerDetectorPacket extends RegisterPacket{

	private static final int indexBUC = IrtGuiProperties.getLong("gui.label.register.p_det.1.index").byteValue();
	private static final int addrBUC  = IrtGuiProperties.getLong("gui.label.register.p_det.1.addr").byteValue();

	private static final int indexFCM = IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.index").byteValue();
	private static final int addrFCM  = IrtGuiProperties.getLong("gui.label.register.fcm.p_det.input.addr").byteValue();

	public InputPowerDetectorPacket() throws PacketParsingException {
		super("InputPowerDetectorPacket", new RegisterValue(indexBUC, addrBUC));
	}

	public InputPowerDetectorPacket(byte[] answer, Boolean hasAcknowledgment) throws PacketParsingException {
		super(answer, Optional.ofNullable(hasAcknowledgment).orElse(false));
	}

	@Override
	public synchronized void setLinkHeaderAddr(byte addr) {

		if(addr==linkHeader.getAddr())
			return;

		super.setLinkHeaderAddr(addr);

		if(addr == -1)	// -1 -> converter
			payloads.get(0).setBuffer(addrFCM, indexFCM);
		else
			payloads.get(0).setBuffer(indexBUC, addrBUC);
	}
}
