
package irt.irt_gui;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.DumpControllerFull;
import irt.controller.serial_port.ComPortJSerialComm;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.protocol.PacketTranceverMode;

public class Test {

	private static final LoggerContext ctx = DumpControllerFull.setSysSerialNumber(null);	//need for log file name setting

	private final static Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		logger.trace(ctx);
		final ComPortJSerialComm cp = new ComPortJSerialComm("COM3");
		cp.openPort();
		final byte linkAddr = (byte)254;
		PacketTranceverMode packet = new PacketTranceverMode(linkAddr, null);
		final Packet send = cp.send(packet);
		logger.error(send);
		cp.closePort();
		final Optional<Object> o = PacketTranceverMode.parseValueFunction.apply(send);
		logger.error(o);
	}
}
