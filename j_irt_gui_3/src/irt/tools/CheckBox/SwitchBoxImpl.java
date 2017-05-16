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
import irt.data.listener.PacketListener;
import irt.data.packet.Packet;
import irt.data.packet.interfaces.PacketWork;

import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;

public abstract class SwitchBoxImpl extends SwitchBox implements Runnable {
	private static final long serialVersionUID = 151272991200793236L;

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	protected final 	ComPortThreadQueue 			cptq 				= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService	service 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private final	PacketWork 					packetToSend;
	private 	 	ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	Updater			 			updater = new Updater();
	private final PacketListener packetListener = new PacketListener() {
		@Override
		public void onPacketRecived(Packet packet) {
			updater.setPacket(packet);
			if(!service.isShutdown())
				service.execute(updater);
		}
	};
	final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override public void ancestorRemoved(AncestorEvent event) {

			scheduleAtFixedRate.cancel(true);
			cptq.removePacketListener(packetListener);
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
		@Override public void ancestorAdded(AncestorEvent event) {
			scheduleAtFixedRate = service.scheduleAtFixedRate(SwitchBoxImpl.this, 1, 5000, TimeUnit.MILLISECONDS);
		}
	};

	public SwitchBoxImpl(Image offImage, Image onImage, PacketWork packetToSEnd) {
		super(offImage, onImage);
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null)
					service.shutdownNow();
			}
		});
		logger.entry(packetToSEnd);

		this.packetToSend = packetToSEnd;
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
