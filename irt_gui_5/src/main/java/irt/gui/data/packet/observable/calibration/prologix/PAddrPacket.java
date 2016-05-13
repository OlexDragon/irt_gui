package irt.gui.data.packet.observable.calibration.prologix;

import irt.gui.controllers.calibration.tools.prologix.enums.PrologixCommands;

/**<b>8.1. addr</b>
<p>The addr command is used to configure, or query the GPIB address. Meaning of the GPIB address depends on the operating mode of the controller. In CONTROLLER mode, it refers to the GPIB address of the instrument being controlled. In DEVICE mode, it is the address of the GPIB peripheral that Prologix GPIB-USB controller is emulating.</p>
<p>An optional secondary address may also be specified. Secondary address must be separated from the primary address by a space character. Valid secondary address values are 96 to 126 (decimal). Secondary address value of 96 corresponds to secondary GPIB address of 0, 97 corresponds to 1, and so on. Specifying secondary address has no effect in DEVICE mode.</p>
<p>If the command is issued with no parameters, the currently configured address (primary, and secondary, if specified) is returned.</p>
<b>SYNTAX</b>: ++addr [<PAD> [<SAD>]]
<p>PAD (Primary Address) is a decimal value between 0 and 30.<br>
SAD (Secondary Address) is a decimal value between 96 and 126. SAD is optional.</p>
<p>MODES AVAILABLE: CONTROLLER, DEVICE</p>
<p><b>EXAMPLES</b>:</p>
++addr 5 – Set primary address to 5<br>
++addr – Query current address<br>
++addr 9 96 – Set primary address to 9 and secondary address to 0<br>
<p>NOTE:
Default GPIB address of many HP-GL/2 plotters is 5.</p>
 */
public class PAddrPacket extends PrologixPacket {

	public PAddrPacket() {
		super(PrologixCommands.ADDR);
	}

}
