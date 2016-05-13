
package irt.gui.controllers.calibration.tools.prologix.enums;

public enum PrologixCommands {

	ADDR		("++addr"		,PrologixDeviceType.FOR_BOTH	),
	EOI			("++eoi"		,PrologixDeviceType.FOR_BOTH	),
	EOS			("++eos"		,PrologixDeviceType.FOR_BOTH	),
	EOT_ENABLE	("++eot_enable"	,PrologixDeviceType.FOR_BOTH	),
	EOT_CHAR	("++eot_char"	,PrologixDeviceType.FOR_BOTH	),
	MODE		("++mode"		,PrologixDeviceType.FOR_BOTH	),
	RST			("++rst"		,PrologixDeviceType.FOR_BOTH	),
	SAVECFG		("++savecfg"	,PrologixDeviceType.FOR_BOTH	),
	VER			("++ver"		,PrologixDeviceType.FOR_BOTH	),
	HELP		("++help"		,PrologixDeviceType.FOR_BOTH	),

	READ_AFTER_WRITE("++auto"	,PrologixDeviceType.CONTROLLER	),
	CLR			("++clr"		,PrologixDeviceType.CONTROLLER	),
	IFC			("++ifc"		,PrologixDeviceType.CONTROLLER	),
	LOC			("++loc"		,PrologixDeviceType.CONTROLLER	),
	READ		("++read"		,PrologixDeviceType.CONTROLLER	),
	READ_TO_EOI	("++read eoi"	,PrologixDeviceType.CONTROLLER	),
	READ_TMO_MS	("++read_tmo_ms",PrologixDeviceType.CONTROLLER	),
	SPOLL		("++spoll"		,PrologixDeviceType.CONTROLLER	),
	SRQ			("++srq"		,PrologixDeviceType.CONTROLLER	),
	TRG			("++trg"		,PrologixDeviceType.CONTROLLER	),

	LON			("++lon"		,PrologixDeviceType.DEVICE		),
	STATUS		("++status"		,PrologixDeviceType.DEVICE		);

	private String command;
	private Object value;
	private PrologixDeviceType deviceType;

	private PrologixCommands(String command, PrologixDeviceType deviceType){
		this.command = command;
		this.deviceType = deviceType;
	}

	public Object getValue() {
		return value;
	}

	public PrologixCommands setValue(Object value) {
		if(value instanceof Boolean)
			value = (Boolean)value ? "1" : "0";
		else if(value instanceof PrologixDeviceType)
			value = (PrologixDeviceType)value==PrologixDeviceType.CONTROLLER ? "1" : "0";
		this.value = value;
		return this;
	}

	public byte[] getCommand(){
		String str;
		if(value!=null){
			str = command+" "+value;
			value = null;
		}else
			str = command;

		return (str+Eos.LF).getBytes();
	}

	public PrologixDeviceType getDeviceType() {
		return deviceType;
	}

	@Override
	public String toString() {
		return "command="+command+"; value="+value;
	}


}
