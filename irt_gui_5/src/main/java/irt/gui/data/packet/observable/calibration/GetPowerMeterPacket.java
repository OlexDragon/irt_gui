
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.HP437B_Commands;

public class GetPowerMeterPacket extends ToolsPacket {

	public GetPowerMeterPacket() {
		super(HP437B_Commands.TRIGGER_IMMEDIATE);
	}
}
