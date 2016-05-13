package irt.gui.controllers.calibration.tools.prologix.enums;

public enum Eos{
	CR_LF	("\r\n"	),
	RC		("\r"	),
	LF		("\n"	);

	private String eos;

	private Eos(String eos){
		this.eos = eos;
	}

	@Override
	public String toString() {
		return eos;
	}
}
