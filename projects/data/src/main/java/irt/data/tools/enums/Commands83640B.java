package irt.data.tools.enums;

import java.util.Optional;

import javax.activation.UnsupportedDataTypeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.prologix.Eos;
import irt.data.tools.ToolsFrequency;
import irt.data.tools.interfaces.ToolCommands;

/**
 * @category Hewlett Packard 83640N
 * @since 10MHz-40MHz
 * @category 8363B series swept signal generator
 */
public enum Commands83640B implements ToolCommands{

	/** Identification */
	ID		(	"*IDN"		, null),
	OUTPUT	(	"OUTP:STAT"	, ToolsState		.class),
	POWER	(	"POW:LEV"	, ToolsPower		.class),
	FREQUENCY(	"FREQ:CW"	, ToolsFrequency	.class);
	//IP- instrument preset; ST - sweep time; AT - attenuation

	private Logger logger = LogManager.getLogger();

	private String command;
	private Class<?> clazz;
	private Object value;

	private Commands83640B(String command, Class<?> clazz){
		this.command = command;
		this.clazz = clazz;
	}

	@Override public Object getValue() {
		return value;
	}

	@Override public void setValue(Object value) throws UnsupportedDataTypeException {
		this.value = Optional
						.of(value)
						.filter(v->v.getClass()==clazz)
						.orElseThrow(()->new UnsupportedDataTypeException());
	}

	@Override public byte[] getCommand() {
		final StringBuilder sb = new StringBuilder(command);

		if(value==null)
			sb.append('?');
		else{
			sb.append(' ');
			sb.append(value);
		}

		value = null;

		logger.trace("{}", sb);

		sb.append(Eos.LF);

		return sb.toString().getBytes();
	}

	@Override public String toString(){
		return name() + "[command=" + command + "; value=" + value + "]";
	}
}
