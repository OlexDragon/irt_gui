package irt.data;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LoggerWorker {

	public static void setLoggerLevel(String loggerName, Level level) {

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		{
			Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
			loggerConfig.setLevel(level);
		}
		ctx.updateLoggers();
	}

}
