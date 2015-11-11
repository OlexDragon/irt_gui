package irt.gui.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.errors.PacketParsingException;

public abstract class FieldsControllerAbstract implements Observer {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	private 			Observer 		observer 	= this;
	protected final 	PacketSender 	packetSender 	= new PacketSender();

	protected abstract 	void 			updateFields(LinkedPacket packet) throws PacketParsingException;

	public void receive(boolean receive) {
		packetSender.setSend(receive);
	}

	@Override
	public void update(Observable observable, Object object) {
		logger.entry(observable, object);
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				if(observable instanceof LinkedPacket) {
					LinkedPacket p = (LinkedPacket) observable;

					if( p.getAnswer()!=null)
						new Thread(	new FieldsUpdater(p)).run();
					else
						logger.warn("No Answer");
				}
			}
		});
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(--priority);
		t.setDaemon(true);
		t.start();
	}

	//*********************************************   InfoPacketSender   ****************************************************************
	protected class PacketSender implements Runnable{

		private List<LinkedPacket> 	packetsToSend = new ArrayList<>();
		private boolean 			send;			public boolean isSend() { return send; }
		public void setSend(boolean send) {
			this.send = send;
		}		

		public void addPacketToSend(LinkedPacket linkedPacket){
			packetsToSend.add(linkedPacket);
			linkedPacket.addObserver(observer);
		}

		@Override
		public void run(){

			if(send)
				for(LinkedPacket packet:packetsToSend)
					SerialPortController.QUEUE.add(packet);
		}
	}

	//*********************************************   FieldsUpdater   ****************************************************************

	public class FieldsUpdater implements Runnable {

		private LinkedPacket packet;

		public FieldsUpdater(LinkedPacket packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			try {

				updateFields(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}

	}
}
