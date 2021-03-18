package irt.tools.CheckBox;

import java.awt.Image;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.configuration.LnbPowerPacket;
import irt.data.packet.configuration.LnbPowerPacket.PowerStatus;
import irt.data.packet.interfaces.Packet;

public class LnbPowerSwitch extends SwitchBoxImpl {
	private static final long serialVersionUID = 312509249334409413L;

	public LnbPowerSwitch(Image offImage, Image onImage) {
		super(offImage, onImage, new LnbPowerPacket());
	}

	@Override
	protected void action() {
		final boolean selected = isSelected();
		PacketSuper pw = new LnbPowerPacket(selected ? PowerStatus.ON : PowerStatus.OFF);
		logger.debug("selected={}; {}", selected, pw);
		cptq.add(pw);
	}

	@Override protected void update(Packet packet) {

		final PacketHeader h = packet.getHeader();
		final short pID = h.getPacketId();

		if(!PacketIDs.CONFIGURATION_FCM_LNB_POWER.match(pID))
			return;

		logger.debug(packet);

		final Payload pl = packet.getPayload(0);
		if(h.getError()!=0 || pl.getByte()==0){
			setVisible(false);
			return;
		}

		if(!isVisible())
			setVisible(true);

		final boolean isOn = pl.getByte()==PowerStatus.ON.ordinal();
		if(isSelected() != isOn)
			setSelected(isOn);
	}
}
