package irt.tools.CheckBox;

import java.awt.Image;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.configuration.LnbReferencePacket;
import irt.data.packet.configuration.LnbReferencePacket.ReferenceStatus;
import irt.data.packet.interfaces.Packet;

public class LnbReferenceSwitch extends SwitchBoxImpl {
	private static final long serialVersionUID = 312509249334409413L;

	public LnbReferenceSwitch(Image offImage, Image onImage) {
		super(offImage, onImage, new LnbReferencePacket());
	}

	@Override
	protected void action() {
		PacketSuper pw = new LnbReferencePacket(isSelected() ? ReferenceStatus.ON : ReferenceStatus.OFF);
		cptq.add(pw);
	}

	@Override protected void update(Packet packet) {
		final PacketHeader h = packet.getHeader();
		final short pID = h.getPacketId();

		if(!PacketIDs.CONFIGURATION_FCM_LNB_REFERENCE.match(pID))
			return;

		logger.debug(packet);

		final Payload pl = packet.getPayload(0);
		if(h.getOption()!=0 || pl.getByte()==0){
			setVisible(false);
			return;
		}

		setVisible(true);

		final boolean isOn = pl.getByte()==ReferenceStatus.ON.ordinal();
		if(isSelected() != isOn)
			setSelected(isOn);
	}
}
