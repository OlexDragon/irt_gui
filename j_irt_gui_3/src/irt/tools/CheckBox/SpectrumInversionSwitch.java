package irt.tools.CheckBox;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.data.packet.SpectrumInversionPacket;
import irt.data.packet.SpectrumInversionPacket.Spectrum;

public class SpectrumInversionSwitch extends SwitchBox implements Runnable {
	private static final long serialVersionUID = 151272991200793236L;

	private final Logger logger = LogManager.getLogger();

	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService	scheduledThreadPool 	= Executors.newScheduledThreadPool(1);
	private final	PacketWork 					packetToSend;
	private final 	ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	Updater			 			updater = new Updater();
	private final PacketListener packetListener = new PacketListener() {
		@Override
		public void packetRecived(Packet packet) {
			updater.setPacket(packet);
			scheduledThreadPool.execute(updater);
		}
	};
	final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override
		public void ancestorRemoved(AncestorEvent event) {
			scheduleAtFixedRate.cancel(true);
			cptq.removePacketListener(packetListener);
		}
		
		@Override
		public void ancestorMoved(AncestorEvent event) { }
		
		@Override
		public void ancestorAdded(AncestorEvent event) { }
	};

	public SpectrumInversionSwitch(Image offImage, Image onImage) {
		super(offImage, onImage);

		packetToSend = new SpectrumInversionPacket();
		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(this, 1, 5000, TimeUnit.MILLISECONDS);
		cptq.addPacketListener(packetListener);

		addAncestorListener(ancestorListener);
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				PacketWork pw = new SpectrumInversionPacket(isSelected() ? Spectrum.INVERTED : Spectrum.NOT_INVERTED);
				cptq.add(pw);
			}
		});
	}

	@Override
	public void run() {
		try{

			cptq.add(packetToSend);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	//****************************   class Updater   *******************
	private class Updater implements Runnable{

		private Packet packet;

		private void setPacket(Packet packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			final PacketHeader h = packet.getHeader();
			final short pID = h.getPacketId();
			if(!(pID==PacketWork.PACKET_ID_CONFIGURATION_SPECTRUM_INVERSION || pID==PacketWork.PACKET_ID_CONFIGURATION_SET_SPECTRUM_INVERSION))
				return;

			final Payload pl = packet.getPayload(0);
			if(h.getOption()!=0 || pl.getByte()==0){
				setVisible(false);
				return;
			}

			if(!isVisible())
				setVisible(true);

			final boolean b = pl.getByte()==1;
			if(isSelected() != b)
				setSelected(b);
		}
		
	}
}
