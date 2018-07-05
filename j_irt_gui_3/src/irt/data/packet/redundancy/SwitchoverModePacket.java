package irt.data.packet.redundancy;

import java.util.Optional;

import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;
import irt.tools.fx.ControlPanelIrPcFx.SwitchoverModes;

public class SwitchoverModePacket extends PacketSuper{

	public static final PacketIDs PACKET_ID = PacketIDs.CONFIGURATION_REDUNDANCY_MODE;

	public SwitchoverModePacket(Byte linkAddr, SwitchoverModes switchoverModes) {
		super(
				linkAddr,
				Optional.ofNullable(switchoverModes).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PACKET_ID,
				PacketImp.GROUP_ID_REDUNDANCY_CONTROLLER,
				PacketImp.REDUNDANCY_CONTROLLER_SWITCHOVER_MODE,
				Optional.ofNullable(switchoverModes).map(SwitchoverModes::ordinal).map(Integer::byteValue).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(switchoverModes).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
