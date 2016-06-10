package irt.gui.controllers.calibration.process;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.data.MyThreadFactory;

public abstract class Average<T> implements Callable<T>{
	protected final Logger logger = LogManager.getLogger(getClass());

	protected final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, new MyThreadFactory());

	protected final Tool tool;
	protected final Commands command;

	public Average(Tool tool, Commands command) {
		this.tool = tool;
		this.command = command;
	}
}
