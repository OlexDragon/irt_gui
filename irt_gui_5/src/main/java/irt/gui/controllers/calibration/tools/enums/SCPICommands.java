package irt.gui.controllers.calibration.tools.enums;

import java.util.Optional;

import irt.gui.controllers.calibration.tools.data.ToolsFrequency;
import irt.gui.controllers.calibration.tools.data.ToolsPower;
import irt.gui.controllers.calibration.tools.prologix.enums.Eos;

public enum SCPICommands implements ToolCommands{

	OUTPUT("OUTP:STAT", ToolsState.class),
	POWER("POW:AMPL", ToolsPower.class),
	FREQUENCY("FREQ:CW", ToolsFrequency.class);

	private String command;
	private Class<?> clazz;
	private Object value;

	private SCPICommands(String command, Class<?> clazz){
		this.command = command;
		this.clazz = clazz;
	}

	@Override public Object getValue() {
		return value;
	}

	@Override public void setValue(Object value) {
		this.value = Optional
						.of(value)
						.filter(v->v.getClass()==clazz)
						.get();
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

		sb.append(Eos.LF);

		return sb.toString().getBytes();
	}

}
