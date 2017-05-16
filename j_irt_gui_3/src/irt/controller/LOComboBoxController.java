
package irt.controller;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.interfaces.IrtController;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.IdValue;
import irt.data.LOIdValue;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LOFrequenciesPacket;
import irt.data.packet.LOPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.ValueFrequency;

public class LOComboBoxController extends Observable implements IrtController, Runnable, PacketListener {

	private final Logger logger = LogManager.getLogger();

	private final 	PacketListener 			pl 		= this;
	private final 	ComPortThreadQueue 		cptq 	= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService scheduledThreadPool 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private final 	ScheduledFuture<?> 		scheduleAtFixedRate;
	private final 	LOPacket 				loPacket;

	private 		PacketWork 				packetToSend;

	private final 	JComboBox<IdValue> 		comboBox;
	private final 	byte 					linkAddr;

	public LOComboBoxController(final JComboBox<IdValue> comboBox, byte linkAddr) {

		this.linkAddr = linkAddr;
		this.comboBox = comboBox;
		comboBox.addAncestorListener(ancestorListener);

		packetToSend = new LOFrequenciesPacket(linkAddr);
		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(this, 1, 5000, TimeUnit.MILLISECONDS);

		loPacket = new LOPacket(linkAddr);
	}

	@Override
	public void run() {
		try{

			cptq.add(packetToSend);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	private byte id;
	int times = 5;
	@Override
	public void onPacketRecived(final Packet packet) {
		scheduledThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				final PacketHeader header = packet.getHeader();
				if(packetToSend.equals(packet) && header.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE) {

					final short packetId = header.getPacketId();
					final Payload pl = packet.getPayload(0);

					if(packetId==PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES){

						final int size = pl.getParameterHeader().getSize();


						List<IdValue> list = new ArrayList<>();
						for(byte i=0; i<size; i+=8) {
							byte id = pl.getByte(i++);
							IdValue iv = new LOIdValue(id, new ValueFrequency(pl.getLong(i), 0, Long.MAX_VALUE));
							list.add(iv);
						}

						Collections.sort(list);
						DefaultComboBoxModel<IdValue> dcbm = new DefaultComboBoxModel<>(list.toArray(new IdValue[0]));
						comboBox.setModel(dcbm);
						packetToSend = loPacket;

					}else if(packetId==PacketWork.PACKET_ID_CONFIGURATION_LO){
						final byte b = pl.getByte();
						if(id!=b){

							id = b;
							final IdValue anObject = new IdValue(id, null);

							comboBox.removeItemListener(itemListener);
							comboBox.setSelectedItem(anObject);
							comboBox.addItemListener(itemListener);

							setChanged();
							notifyObservers();
						}
					}

				}else
					logger.info("No answer:{}", packet);
			}
		});
	}

	private final ItemListener itemListener  = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			if(itemEvent.getStateChange()==ItemEvent.SELECTED){
				final IdValue selectedItem = (IdValue)comboBox.getSelectedItem();
				byte id = (byte) selectedItem.getID();

				final LOPacket packet = new LOPacket(linkAddr, id);
				cptq.add(packet);
			}
		}
	};

	private final AncestorListener ancestorListener = new AncestorListener() {
		public void ancestorAdded(AncestorEvent event) {
			cptq.addPacketListener(pl);
			comboBox.addItemListener(itemListener);
		}
		public void ancestorRemoved(AncestorEvent event) {
			cptq.removePacketListener(pl);
			scheduleAtFixedRate.cancel(true);
			comboBox.removeItemListener(itemListener);
			comboBox.removeAncestorListener(this);
			scheduleAtFixedRate.cancel(true);
			scheduledThreadPool.shutdown();
		}
		public void ancestorMoved(AncestorEvent event) { }
	};
}
