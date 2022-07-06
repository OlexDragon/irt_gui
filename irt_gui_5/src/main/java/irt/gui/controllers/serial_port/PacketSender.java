package irt.gui.controllers.serial_port;

import irt.gui.data.packet.interfaces.PacketToSend;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketSender {

	private IrtSerialPort serialPort;

	public void send(PacketToSend packet) throws Exception {
		clear();
	}

	private void clear() throws Exception {
		serialPort.getAvailableBytes();
	}
}
