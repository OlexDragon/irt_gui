package irt.gui.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import irt.gui.web.beans.Packet;

@Service
@Qualifier("jSerialComm")
public class JSerialComm extends JSerialCommAbstr {

	@Value("${irt.packet.termination.byte}")
	private Byte termination;

	@Override
	protected void read(final InputStream is, ByteBuffer bb) throws IOException {

		while(true) {

			final int read =  is.read();

			if(read<0)
				break;

			final byte readByte = (byte)read;
			bb.put(readByte);

			if(bb.position()>0) {
				final Byte t = Optional.ofNullable(termination).orElse(Packet.FLAG_SEQUENCE);
				if(t.equals(readByte)) {
					final int available = is.available();
					if(available == 0) {
						break;
					}else {
						read(is, bb);
						break;
					}
				}
			}
		}
	}
}
