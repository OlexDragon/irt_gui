package irt.controller;

import java.awt.Component;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import irt.data.listener.PacketListener;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.control.SaveConfigPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;

public class StoreConfigController implements PacketListener{

	private final Component owner;
	private final SaveConfigPacket packet;
	private final Timer timer = new Timer((int) TimeUnit.SECONDS.toMillis(5), e->{
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(StoreConfigController.this);
		JOptionPane.showMessageDialog( null, "Could not store the configuration.");
	});

	public StoreConfigController(byte linkAddr, Component owner) {
		packet = new SaveConfigPacket(linkAddr);

		this.owner = owner;

		if(JOptionPane.showConfirmDialog( null, "Do you want to store the configuration?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
			GuiControllerAbstract.getComPortThreadQueue().add(packet);
			timer.setRepeats(false);
			timer.start();
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {
		if(packet.getHeader().getPacketId()!=PacketWork.PACKET_ID_STORE_CONFIG)
			return;

		Packets
		.cast(packet)
		.ifPresent(p->{

			timer.stop();

			final byte packetType = p.getHeader().getPacketType();
			final byte option = p.getHeader().getOption();

			if(packetType!= PacketImp.PACKET_TYPE_RESPONSE || option!=PacketImp.ERROR_NO_ERROR)
				if(JOptionPane.showConfirmDialog( owner, "Could not store the configuration. Try one more time?", "Store Config", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
					timer.restart();
					GuiControllerAbstract.getComPortThreadQueue().add(this.packet);
					return;
			}

			GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
			JOptionPane.showMessageDialog( owner, "The Configuration has been stored.");
		});
	}

	@Override
	protected void finalize() throws Throwable {
		timer.stop();
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
	}

}
