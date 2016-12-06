package irt.fx.control.prologix.enums;

/** GPIB termination characters */
public enum Eos{
	CR_LF	("\r\n"	),
	RC		("\r"	),
	LF		("\n"	),
	NONE	(null	);

	private String eos;

	private Eos(String eos){
		this.eos = eos;
	}

	@Override
	public String toString() {
		return eos;
	}
}
