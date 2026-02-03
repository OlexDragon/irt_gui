package irt.gui.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.Level;

import com.fazecast.jSerialComm.SerialPort;

import lombok.Getter;

@Getter
public class JSerialCommConsole extends JSerialCommAbstr {

	private byte[] expectedEnd = new byte[]{'>'};

	@Override
	protected void read(final InputStream is, ByteBuffer bb) {
		logger.traceEntry("JSerialCommFlash.read()");

		try {
			while(true) {

				final byte readByte = (byte)is.read();
				logger.trace("readByte: {}", readByte);
				bb.put(readByte);
			}
		} catch (IOException e) {
			logger.catching(Level.TRACE, e);
		}
	}

	public void setExpectedEnd(byte[] expectedEnd) {
		if(expectedEnd!=null && expectedEnd.length>0)
			this.expectedEnd = expectedEnd;
	}

	@Override
	protected void setSpTimeout(SerialPort sp, Integer timeout) {
		super.setSpTimeout(sp, 100);
	}

	
}
