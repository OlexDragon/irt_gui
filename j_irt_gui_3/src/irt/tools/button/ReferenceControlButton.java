
package irt.tools.button;

import javax.swing.JButton;

public class ReferenceControlButton extends JButton {
//	private static final Logger logger = LogManager.getLogger();
//
	private static final long serialVersionUID = 4505706702151924563L;
//
//	private final ReferenceControlPacket getPacket 				= new ReferenceControlPacket();
//	private final ReferenceControlPacket setPacket 				= new ReferenceControlPacket(ReferenceStatus.OFF);
//	private final ComPortThreadQueue 	cptq 					= GuiControllerAbstract.getComPortThreadQueue();
//	private final ScheduledExecutorService	scheduledThreadPool = Executors.newScheduledThreadPool(1, new MyThreadFactory());
//	private final ScheduledFuture<?> 		scheduleAtFixedRate;
//	private final Updater			 		updater 			= new Updater();
//
//	private PacketListener packetListener = new PacketListener() {
//		
//		@Override
//		public void packetRecived(Packet packet) {
//			updater.setPacket(packet);
//			scheduledThreadPool.execute(updater);
//		}
//	};
//
//	private AncestorListener ancestorListener = new AncestorListener() {
//		
//		@Override public void ancestorRemoved(AncestorEvent event) {
//			scheduleAtFixedRate.cancel(true);
//			cptq.removePacketListener(packetListener);
//		}
//		@Override public void ancestorMoved(AncestorEvent event) { }
//		@Override public void ancestorAdded(AncestorEvent event) { }
//	};
//
//	public ReferenceControlButton(String text){
//		super(text);
//		setCursor(new Cursor(Cursor.HAND_CURSOR));
//		addAncestorListener(ancestorListener);
//		addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try{
//					ReferenceStatus valueOf = ReferenceStatus.valueOf(getText());
//					setPacket.setValue(valueOf==ReferenceStatus.ON ? ReferenceStatus.OFF : ReferenceStatus.ON);
//					GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
//				}catch(Exception ex){
//					logger.catching(ex);
//				}
//			}
//		});
//		setMargin(new Insets(1,1,1,1));
//		
//
//		scheduleAtFixedRate = scheduledThreadPool.scheduleAtFixedRate(new Sender(), 1, 5000, TimeUnit.MILLISECONDS);
//		cptq.addPacketListener(packetListener);
//	}
//
//	//****************************** class Sender **************************************
//	private class Sender implements Runnable{
//
//		@Override
//		public void run() {
//			GuiControllerAbstract.getComPortThreadQueue().add(getPacket);
//		}
//		
//	}
//
//	//****************************** class Updater **************************************
//	public class Updater implements Runnable {
//
//		private Packet packet;
//
//		@Override
//		public void run() {
//			PacketHeader header = packet.getHeader();
//			if(header.getOption()==PacketImp.NO_ERROR){
//				if(packet.equals(getPacket)){
//					ReferenceStatus referenceStatus = ReferenceStatus.values()[packet.getPayload(0).getByte()];
//					if(referenceStatus==ReferenceStatus.UNKNOWN){
//						setVisible(false);
//						scheduleAtFixedRate.cancel(true);
//						return;
//					}
//					String text = referenceStatus.name();
//					if(!getText().equals(text))
//						setText(text);
//				}
//			}
//		}
//
//		public void setPacket(Packet packet) {
//			this.packet = packet;
//		}
//		
//	}
}
