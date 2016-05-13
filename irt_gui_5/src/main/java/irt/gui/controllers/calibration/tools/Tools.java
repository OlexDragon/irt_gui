package irt.gui.controllers.calibration.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public abstract class Tools {

	protected static final Logger LOGGER = (Logger) LogManager.getLogger();
	protected final Logger logger = (Logger) LogManager.getLogger(getClass().getName());

	private String id;


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public abstract byte getAddr();
}
