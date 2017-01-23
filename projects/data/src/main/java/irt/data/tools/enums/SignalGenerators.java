package irt.data.tools.enums;

import irt.data.tools.interfaces.ToolCommands;

public enum SignalGenerators {

	HP_83640B	("10 MHz", 	"40 GHz", 	"-20", 	"25", 	Commands83640B.class),
	HP_83752BA	("10 MHz", 	"20 GHz", 	"-15", 	"20", 	Commands83640B.class),
	HP_8648C	("9 kHz", 	"3.2 GHz",	"-135", "14",	CommandsSCPI.class),
	HP_8648D	("9 kHz", 	"4 GHz",	"-127", "13",	CommandsSCPI.class);

	public final String minFreq;
	public final String maxFreq;

	public final String minPower;
	public final String maxPower;

	public final Class<? extends ToolCommands> commandEnumClass;

	private SignalGenerators(	String minFreq,
								String maxFreq,
								String minPower,
								String maxPower,

								Class<? extends ToolCommands>  commandEnumClass){

		this.minFreq = minFreq;
		this.maxFreq = maxFreq;

		this.minPower = minPower;
		this.maxPower = maxPower;

		this.commandEnumClass 	= commandEnumClass;
	}
}
