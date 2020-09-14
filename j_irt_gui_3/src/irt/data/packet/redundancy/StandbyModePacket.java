package irt.data.packet.redundancy;

import java.util.Optional;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.tools.fx.module.ControlPanelIrPcFx.StandbyModes;

public class StandbyModePacket extends PacketSuper{

	public static final PacketIDs PACKET_ID = PacketIDs.REDUNDANCY_MODE;

	public StandbyModePacket(Byte linkAddr, StandbyModes standbyModes) {
		super(
				linkAddr,
				Optional.ofNullable(standbyModes).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				PACKET_ID,
				PacketGroupIDs.REDUNDANCY,
				PacketImp.PARAMETER_ID_REDUNDANCY_CONTROLLER_STANDBY_MODE,
				Optional.ofNullable(standbyModes).map(StandbyModes::ordinal).map(Integer::byteValue).map(v->new byte[]{v}).orElse(null),
				Optional.ofNullable(standbyModes).map(v->Priority.COMMAND).orElse(Priority.REQUEST));
	}

}
