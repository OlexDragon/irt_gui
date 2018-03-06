
package irt.controller.log4J;

import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rolling.RollingRandomAccessFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

//@Plugin(name = "MyFileManager", category = "Core", elementType = "appender", printObject = true)
public class MyFileManager extends RollingRandomAccessFileManager {

	public MyFileManager(
			LoggerContext loggerContext,
			RandomAccessFile raf,
			String fileName,
			String pattern,
			OutputStream os,
			boolean append,
			boolean immediateFlush,
			int bufferSize,
			long size,
			long time,
			TriggeringPolicy policy,
			RolloverStrategy strategy,
			String advertiseURI,
			Layout<? extends Serializable> layout,
			boolean writeHeader) {
		super(loggerContext, raf, fileName, pattern, os, append, immediateFlush, bufferSize, size, time, policy, strategy, advertiseURI, layout, writeHeader);
		System.out.println("************ -> " +  fileName);
	}
//
//	@PluginFactory
//	public static MyFileManager createAppender(	@PluginElement("Layout") Layout layout,
//												@PluginAttribute("name") String name,
//	                                            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
//	                                            @PluginElement("Layout") Layout layout,
//	                                            @PluginElement("Filters") Filter filter) {
//
//		return new MyFileManager(loggerContext, raf, fileName, pattern, os, append, immediateFlush, bufferSize, size, time, policy, strategy, advertiseURI, layout, writeHeader);	
//	}
}
