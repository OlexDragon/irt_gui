
package irt.irt_gui;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.DumpControllerFull;
import irt.controller.serial_port.ComPortJSerialComm;
import irt.data.packet.denice_debag.DeviceDebugInfoPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.protocol.PacketTranceverMode;

public class Test {

	private static final LoggerContext ctx = DumpControllerFull.setSysSerialNumber(null);	//need for log file name setting

	private final static Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		logger.trace(ctx);
		final ComPortJSerialComm cp = new ComPortJSerialComm("COM3");
		cp.openPort();
		final byte linkAddr = 0;
		DeviceDebugInfoPacket packet = new DeviceDebugInfoPacket(linkAddr, (byte) 2);
		packet.setValue(1);
		logger.error(packet);
		final byte[] bytes = packet.toBytes();
		logger.error("bytes: {}; {}", bytes.length, bytes);
		final Packet send = cp.send(packet);
		logger.error(send);
		final byte[] ttoBytes = send.toBytes();
		if(ttoBytes==null) {
			logger.warn("No answer from the unit.");
			return;
		}
		logger.error("bytes: {}; {}", ttoBytes.length, ttoBytes);
		cp.closePort();
		final Optional<Object> o = PacketTranceverMode.parseValueFunction.apply(send);
		logger.error(o);
		final byte[] acknowledg = packet.getAcknowledg();
		logger.error("bytes: {}; {}", acknowledg.length, acknowledg);
	}
}
