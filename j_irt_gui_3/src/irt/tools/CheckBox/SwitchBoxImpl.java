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
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;

public abstract class SwitchBoxImpl extends SwitchBox implements Runnable {
	private static final long serialVersionUID = 151272991200793236L;

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	protected final 	ComPortThreadQueue 			cptq 				= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService	scheduledThreadPool 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private final	PacketWork 					packetToSend;
	private final 	ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	Updater			 			updater = new Updater();
	private final PacketListener packetListener = new PacketListener() {
		@Override
		public void onPacketRecived(Packet packet) {
			updater.setPacket(packet);
			scheduledThreadPool.execute(updater);
		}
	};
	final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override public void ancestorRemoved(AncestorEvent event) {

			scheduleAtFixedRate.cancel(true);
			cptq.removePacketListener(packetListener);
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
		@Override public void ancestorAdded(AncestorEvent event) { }
	};

	public SwitchBoxImpl(Image offImage, Image onImage, PacketWork packetToSEnd) {
		super(offImage, onImage);
		logger.entry(packetToSEnd);

		this.packetToSend = packetToSEnd;
		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(this, 1, 5000, TimeUnit.MILLISECONDS);
		cptq.addPacketListener(packetListener);

		addAncestorListener(ancestorListener);
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				action();
			}
		});
	}

	/**
	 * Action to status change
	 */
	protected abstract void action();
	/**
	 * @param packet - received packet
	 */
	protected abstract void update(Packet packet);

	@Override public void run() {
		logger.entry(packetToSend);
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
			update(packet);
		}
	}
}
