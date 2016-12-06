package irt.fx.control.prologix.packets;

import irt.fx.control.prologix.enums.PrologixCommands;

/**
 * <b>8.16. savecfg</b>
<p>This command enables, or disables, automatic saving of configuration parameters in EPROM. If enabled, the following configuration parameters are saved whenever they are updated – mode, addr, auto, eoi, eos, eot_enable, eot_char and read_tmo_ms.</p>
<p>However, frequent updates may eventually wear out the EPROM. This command may be used to temporarily disable automatic saving of configuration parameters to reduce EEPROM wear.</p>
<p>The savecfg setting itself is not saved in EPROM. It is always enabled on startup (after power up, or reset).</p>
<b>SYNTAX</b>: ++savecfg [0|1]
<p>MODES AVAILABLE: CONTROLLER, DEVICE</p>
<b>EXAMPLE</B>:
<p>++savecfg 1 Enable saving of configuration parameters in EPROM<br>
++savecfg 0 Disable saving of configuration parameters in EPROM<br>
++savecfg Query current setting</p>
<b>NOTE</b>:
<p>“++savecfg 1” command will immediately save the current values of all configuration parameters, in addition to enabling the automatic saving of parameters.</p>
 */
public class PrologixSaveCfgPacket extends PrologixPacket {

	public PrologixSaveCfgPacket() {
		super(PrologixCommands.SAVECFG);
	}

}
