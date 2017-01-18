package irt.data.prologix;

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

	public byte[] toBytes() {

		if(this == NONE)
			return null;

		return eos.getBytes();
	}
}
