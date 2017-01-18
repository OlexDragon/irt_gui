
package irt.packet.prologix;

import irt.data.prologix.PrologixCommands;

/**
 * <b>8.12. mode</b>
<p>This command configures the Prologix GPIB-USB controller to be a CONTROLLER or DEVICE.</p>
<p>If the command is issued without any arguments, the current mode is returned.</p>
<p><b>SYNTAX</b>:</p> ++mode [0|1] where 1 – CONTROLLER, 0 – DEVICE
<p>MODES AVAILABLE: CONTROLLER, DEVICE</p>
<p><b>EXAMPLES</b>:</p>
++mode 1 Switch to CONTROLLER mode<br>
++mode 0 Switch to DEVICE mode<br>
++mode Query current mode
 */
public class PrologixModePacket extends PrologixPacket {

	public PrologixModePacket() {
		super(PrologixCommands.MODE);
	}
}
