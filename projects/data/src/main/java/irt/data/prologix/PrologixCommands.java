
package irt.data.prologix;

import org.apache.logging.log4j.LogManager;

import irt.service.ObjectToEos;
import irt.service.ObjectToNoValue;
import irt.service.ObjectToPrologixDeviceType;
import irt.services.ObjectToBoolean;
import irt.services.ObjectToInteger;
import irt.services.interfaces.CastValue;

public enum PrologixCommands{

	ADDR		("++addr"		,PrologixDeviceType.FOR_BOTH	, new ObjectToInteger(0, 30)),
	EOI			("++eoi"		,PrologixDeviceType.FOR_BOTH	, new ObjectToBoolean()),
	EOS			("++eos"		,PrologixDeviceType.FOR_BOTH	, new ObjectToEos()),
	EOT_ENABLE	("++eot_enable"	,PrologixDeviceType.FOR_BOTH	, new ObjectToBoolean()),
	EOT_CHAR	("++eot_char"	,PrologixDeviceType.FOR_BOTH	, new ObjectToNoValue()),
	MODE		("++mode"		,PrologixDeviceType.FOR_BOTH	, new ObjectToPrologixDeviceType()),
	RST			("++rst"		,PrologixDeviceType.FOR_BOTH	, new ObjectToNoValue()),
	SAVECFG		("++savecfg"	,PrologixDeviceType.FOR_BOTH	, new ObjectToBoolean()),
	VER			("++ver"		,PrologixDeviceType.FOR_BOTH	, new ObjectToNoValue()),
	HELP		("++help"		,PrologixDeviceType.FOR_BOTH	, new ObjectToNoValue()),

	READ_AFTER_WRITE("++auto"	,PrologixDeviceType.CONTROLLER	, new ObjectToBoolean()),
	CLR			("++clr"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),
	IFC			("++ifc"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),
	LOC			("++loc"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),
	READ		("++read"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),//TODO 
	READ_TO_EOI	("++read eoi"	,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),
	READ_TMO_MS	("++read_tmo_ms",PrologixDeviceType.CONTROLLER	, new ObjectToInteger(1, 3000)),
	SPOLL		("++spoll"		,PrologixDeviceType.CONTROLLER	, new ObjectToInteger(0, 30)),
	SRQ			("++srq"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),
	TRG			("++trg"		,PrologixDeviceType.CONTROLLER	, new ObjectToNoValue()),

	LON			("++lon"		,PrologixDeviceType.DEVICE		, new ObjectToBoolean()),
	STATUS		("++status"		,PrologixDeviceType.DEVICE		, new ObjectToInteger(0, 255));

	private String command;
	private Object oldValue;
	private PrologixDeviceType deviceType;
	private CastValue<?> castValue;

	private PrologixCommands(String command, PrologixDeviceType deviceType, CastValue<?> castValue){
		this.command = command;
		this.deviceType = deviceType;
		this.castValue = castValue;
	}

	public Object getValue() {
		return castValue.getValue();
	}

	public Object getOldValue() {
		return oldValue;
	}

	public PrologixCommands setValue(Object value) {

		castValue.setValue(value);
		this.oldValue = null;
		return this;
	}

	public byte[] getCommand(){
		LogManager.getLogger().error("{}: byte[] getCommand()", name());

		final String str = command + castValue.toPrologixCode();
		oldValue = castValue.setValue(null);

		return (str + Eos.LF).getBytes();
	}

	public PrologixDeviceType getDeviceType() {
		return deviceType;
	}

	@Override
	public String toString() {
		return "command="+ command + "; value=" + castValue.getValue() + "; oldValue=" + oldValue;
	}
}