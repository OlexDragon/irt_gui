package irt.tools.CheckBox;

import java.awt.Image;

import irt.data.PacketWork;
import irt.data.packet.LnbReferencePacket;
import irt.data.packet.LnbReferencePacket.ReferenceStatus;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

public class LnbReferenceSwitch extends SwitchBoxImpl {
	private static final long serialVersionUID = 312509249334409413L;

	public LnbReferenceSwitch(Image offImage, Image onImage) {
		super(offImage, onImage, new LnbReferencePacket());
	}

	@Override
	protected void action() {
		logger.entry();
		PacketWork pw = new LnbReferencePacket(isSelected() ? ReferenceStatus.ON : ReferenceStatus.OFF);
		cptq.add(pw);
	}

	@Override protected void update(Packet packet) {
		final PacketHeader h = packet.getHeader();
		final short pID = h.getPacketId();

		if(pID!=PacketWork.PACKET_ID_CONFIGURATION_FCM_LNB_REFERENCE)
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
