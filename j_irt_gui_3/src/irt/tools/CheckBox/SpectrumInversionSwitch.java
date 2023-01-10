package irt.tools.CheckBox;

import java.awt.Image;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketID;
import irt.data.packet.Payload;
import irt.data.packet.configuration.SpectrumInversionPacket;
import irt.data.packet.configuration.SpectrumInversionPacket.Spectrum;
import irt.data.packet.interfaces.Packet;

public class SpectrumInversionSwitch extends SwitchBoxImpl {
	private static final long serialVersionUID = 312509249334409413L;

	public SpectrumInversionSwitch(Image offImage, Image onImage) {
		super(offImage, onImage, new SpectrumInversionPacket());
	}

	@Override
	protected void action() {
		PacketSuper pw = new SpectrumInversionPacket(isSelected() ? Spectrum.INVERTED : Spectrum.NOT_INVERTED);
		cptq.add(pw);
	}

	@Override
	protected void update(Packet packet) {
		final PacketHeader h = packet.getHeader();
		final short pID = h.getPacketId();
		if(!PacketID.CONFIGURATION_SPECTRUM_INVERSION.match(pID))
			return;

		logger.trace(packet);

		final Payload pl = packet.getPayload(0);
		if(h.getError()!=0 || pl.getByte()==0){
			setVisible(false);
			return;
		}

		if(!isVisible())
			setVisible(true);

		final boolean b = pl.getByte()==1;
		if(isSelected() != b)
			setSelected(b);
	}
}
