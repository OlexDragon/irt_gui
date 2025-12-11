package irt.gui.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
@Qualifier("jSerialCommFlash")
@Getter
public class JSerialCommFlash extends JSerialCommAbstr {

	private int expectedLength = 1;

	@Override
	protected void read(final InputStream is, ByteBuffer bb) throws IOException {
		logger.error("JSerialCommFlash.read()");

		try {
			while(true) {

				final int read =  is.read();

				if(read<0)
					break;

				final byte readByte = (byte)read;
				logger.error("Read byte: 0x{}", readByte);
				bb.put(readByte);

				if(bb.position()>=expectedLength) 
					break;
				else {
					read(is, bb);
					break;
				}
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	public void setExpectedLength(int expectedLength) {
		this.expectedLength = expectedLength<=0 ? 1 : expectedLength;
	}

	
}
