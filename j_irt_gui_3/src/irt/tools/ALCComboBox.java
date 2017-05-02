package irt.tools;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.MyThreadFactory;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.ALCEnablePacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;

public class ALCComboBox extends JCheckBox implements Runnable, PacketListener{
	private static final long serialVersionUID = 5927791917430153433L;

	protected final Logger logger = LogManager.getLogger();

	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	private	final 	ScheduledExecutorService 	scheduledThreadPool 	= Executors.newScheduledThreadPool(1, new MyThreadFactory());
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;

	private final ALCEnablePacket packet;

	private final Byte linkAddr; 

		public ALCComboBox(String text, final Byte linkAddr) {
		super(text);

		this.linkAddr = linkAddr;
		packet = new ALCEnablePacket(linkAddr, null); 

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				cptq.addPacketListener(ALCComboBox.this);
				scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(ALCComboBox.this, 1, 3000, TimeUnit.MILLISECONDS);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				scheduleAtFixedRate.cancel(true);
				removeAncestorListener(this);
				cptq.removePacketListener(ALCComboBox.this);
			}
			@Override public void ancestorMoved(AncestorEvent event) { }
		});
		addItemListener(checkBoxItemListener);
	}

	@Override
	public void run() {
		try{

			logger.trace(packet);
			cptq.add(packet);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {
		try{
			if(packet.equals(this.packet) && packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE && packet.getHeader().getOption()==PacketImp.ERROR_NO_ERROR){
				logger.trace(packet);

				final Payload payload = packet.getPayload(0);
				setSelected((payload.getByte()==1));
			}
		}catch(Exception e){
			logger.catching(e);
		}
	}

	final ItemListener checkBoxItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {

			PacketWork packet = new ALCEnablePacket(linkAddr, (byte) (e.getStateChange() == ItemEvent.DESELECTED ? 0 : 1));
			cptq.add(packet);
		}
	};
}
