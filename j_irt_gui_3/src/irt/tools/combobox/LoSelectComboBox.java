
package irt.tools.combobox;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.IdValueFreq;
import irt.data.Listeners;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.LOFrequenciesPacket;
import irt.data.packet.LOPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.value.ValueFrequency;

public class LoSelectComboBox extends JComboBox<IdValueFreq> implements Runnable{
	private static final long serialVersionUID = -419940816764892955L;

	private final Logger logger = LogManager.getLogger();

	private final 	DefaultComboBoxModel<IdValueFreq> model = new DefaultComboBoxModel<>();
	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private final 	ScheduledExecutorService	scheduledThreadPool 	= Executors.newScheduledThreadPool(1);
	private 		PacketWork 					packetToSend;
	private final 	ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	Updater			 			updater = new Updater();
	private final 	PacketListener packetListener = new PacketListener() {
		@Override
		public void packetRecived(Packet packet) {
			updater.setPacket(packet);
			scheduledThreadPool.execute(updater);
		}
	};
	private final AncestorListener ancestorListener = new AncestorListener() {
		
		@Override
		public void ancestorRemoved(AncestorEvent event) {
			scheduleAtFixedRate.cancel(true);
			cptq.removePacketListener(packetListener);
		}
		
		@Override
		public void ancestorMoved(AncestorEvent event) { 
		}
		
		@Override
		public void ancestorAdded(AncestorEvent event) {
		}
	};

	private final ItemListener aListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			if(itemEvent.getStateChange()==ItemEvent.SELECTED){
				logger.entry();
				cptq.add(new LOPacket(linkAddr, ((IdValueFreq)model.getSelectedItem()).getId()));
			}
		}
	};

	private final byte linkAddr;

	public LoSelectComboBox(final byte linkAddr) {

		this.linkAddr = linkAddr;
		setModel(model);

		setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ private static final long serialVersionUID = 1L; @Override public int getWidth() { return 0; }};}});
		addPopupMenuListener(Listeners.popupMenuListener);
		((JLabel)getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		packetToSend = new LOFrequenciesPacket(linkAddr);

		addAncestorListener(ancestorListener);

		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(this, 1, 5000, TimeUnit.MILLISECONDS);
		cptq.addPacketListener(packetListener);
	}

	@Override
	public void run() {
		try{

			cptq.add(packetToSend);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	public class Updater implements Runnable {

		private Packet packet;

		@Override public void run() {

			final PacketHeader h = packet.getHeader();
			final short pID = h.getPacketId();
			if(!(pID==PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES || pID==PacketWork.PACKET_ID_CONFIGURATION_LO))
				return;

			if(h.getPacketType()==PacketImp.PACKET_TYPE_REQUEST)
				logger.warn("Packet is wrong or no connection: {}", packet);

			logger.trace(packet);
			final Payload payload = packet.getPayloads().get(0);

			switch(pID){
			case PacketWork.PACKET_ID_CONFIGURATION_LO_FREQUENCIES:
				fillComboBox(payload);
				break;
			case PacketWork.PACKET_ID_CONFIGURATION_LO:
				update(payload);
			}
		}

		private void update(Payload payload) {

			final byte loID = payload.getByte();
			final IdValueFreq anObject = new IdValueFreq(loID, null);
			if(!anObject.equals(model.getSelectedItem())){
				removeItemListener(aListener);
				setSelectedItem(anObject);
				addItemListener(aListener);
			}
		}

		public void fillComboBox(Payload payload) {

			final int size = payload.getParameterHeader().getSize();
			switch(size){
			case 0:
				final LoSelectComboBox cb = LoSelectComboBox.this;
				cb.getParent().remove(cb);
				break;
			default:
				final byte[] frs = payload.getBuffer();
				for(int i=0; i< frs.length; i+=8){

					byte id = frs[i];
					long v = payload.getLong((byte)++i);
					IdValueFreq ivf = new IdValueFreq(id, new ValueFrequency(v, v, v));
					if(model.getIndexOf(ivf)<0)
						addItem(ivf);
				}
				addItemListener(aListener);
				packetToSend = new LOPacket(linkAddr);
				LoSelectComboBox.this.run();
			}
		}

		public void setPacket(Packet packet) {
			this.packet = packet;
		}

	}
}
