package irt.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.MyThreadFactory;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.RegisterPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketWork;
import irt.data.value.Value;

public class RegisterTextField extends JTextField implements PacketListener {
	private static final long serialVersionUID = 517630309792962880L;
	private final Logger logger = LogManager.getLogger();

	public final int MIN;
	public final int MAX;

	private PacketWork getPacket;
	private PacketWork setPacket;

	private final RegisterValue valueToSend;
	private final RegisterValue valueSaveRegister;

	private final 	ScheduledExecutorService	service = Executors.newScheduledThreadPool(5, new MyThreadFactory());
	private ScheduledFuture<?> scheduleAtFixedRate;

	private final TextFieldUpdater updater 		= new TextFieldUpdater();
	private Byte unitAddress;
	private short packetId;

	public RegisterTextField(Byte linkAddr, RegisterValue registerValue, short packetId, int min, int max) {
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null)
					service.shutdownNow();
			}
		});

		MIN = min;
		MAX = max;
		unitAddress = linkAddr;
		this.packetId = packetId;

		registerValue.setValue(null); // if value is null packet type is REQIEST
		getPacket = new RegisterPacket(linkAddr, registerValue, packetId);
//		if(Optional
//				.of(getPacket)
//				.map(pw->pw.getPacketThread().getPacket())
//				.filter(p->p.getHeader().getGroupId()==PacketImp.GROUP_ID_DEVICE_DEBAG)
//				.map(p->p.getPayloads())
//				.map(pls->pls.get(0))
//				.map(Payload::getRegisterValue)
//				.filter(pv->pv.getAddr()==4 && pv.getIndex()==100)
//				.isPresent())
//			throw new RuntimeException();

		valueToSend = new RegisterValue(registerValue);
		valueToSend.setValue( new Value(896, MIN, MAX, 0)); // if value not null packet type is COMMAND
		setPacket = new RegisterPacket(linkAddr, valueToSend, packetId);

		valueSaveRegister = new RegisterValue(registerValue.getIndex(), registerValue.getAddr()+3, new Value(0, 0, 0, 0));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				send();
			}
		});
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if(!(Character.isDigit(c) || (c==KeyEvent.VK_BACK_SPACE || c==KeyEvent.VK_DELETE) || c==KeyEvent.VK_ENTER)){
				    getToolkit().beep();
				    e.consume();
				}
			}
			
			@Override public void keyReleased(KeyEvent e) { }
			@Override public void keyPressed(KeyEvent e) { }
		});
	}

	@Override
	public void onPacketRecived(Packet packet) {
		updater.setPacket(packet);
		service.execute(updater);
	}

	public void start(){
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate = service.scheduleAtFixedRate(new Sender(), 1, 3, TimeUnit.SECONDS);
	}

	public void stop(){
		if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(true);
	}

	public void saveRegister(){

		((RegisterPacket)setPacket).setValue(valueSaveRegister);
		GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
	}

	public void send() {
		final String text = getText();
		if(text.isEmpty()){
		    getToolkit().beep();
		    return;
		}

		final int value = Integer.parseInt(text);
		valueToSend.getValue().setValue(value);
		((RegisterPacket)setPacket).setValue(valueToSend);

		GuiControllerAbstract.getComPortThreadQueue().add(setPacket);
	}

	//************************ class TextFieldUpdater *********************************
	private class TextFieldUpdater implements Runnable{

		private Packet packet;

		public void setPacket(Packet packet) {
			this.packet = packet;
		}

		@Override
		public void run() {

			try{

				final Optional<Packet> o = Optional.ofNullable(packet);

				if(!o.isPresent())
					return;

				byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

				if(addr!=unitAddress)
					return;

				final Optional<PacketHeader> sameGroupId = o.map(Packet::getHeader)
															.filter(h->h.getPacketId()==packetId)
															.filter(h->h.getGroupId()==PacketImp.GROUP_ID_DEVICE_DEBAG);

				if(!sameGroupId.isPresent())
					return;

				Optional<PacketHeader> hasResponse = sameGroupId.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

				if(!hasResponse.isPresent()){
					logger.warn("Unit is not connected {}", packet);
					return;
				}

				final Optional<PacketHeader> noError = hasResponse
														.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

				if(!noError.isPresent()){
					logger.warn("Packet has error {}", packet);
					return;
				}
				
				noError
				.map(h->packet.getPayloads())
				.map(pls->pls.parallelStream())
				.ifPresent(
						stream->{
							stream
							.filter(pl->pl.getParameterHeader().getCode()==PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE)
							.map(Payload::getRegisterValue)
							.filter(rv->rv.getIndex()==valueToSend.getIndex())
							.forEach(rv->{

								final int aR = rv.getAddr();
								final int aS = valueSaveRegister.getAddr();
								final int aV = valueToSend.getAddr();

								if(aR==aS)
									showSaved();

								else if(aR==aV)
									setValue(rv);
							});
						});
			}catch(Exception e){
				logger.catching(e);
			}
		}

		private void setValue(final RegisterValue registerValue) {
			final String str = registerValue.getValue().toString();

			SwingUtilities.invokeLater(()-> {
				if(!str.equals(getText()))
					setText(str);
			});
		}

		private void showSaved() {

			try {
				stop();
				Thread.sleep(100);
				setText("SAVED");
				Thread.sleep(2000);
				start();
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}

	//************************ class TextFieldUpdater *********************************
	private class Sender implements Runnable{
		@Override
		public void run() {

			try{

				GuiControllerAbstract.getComPortThreadQueue().add(getPacket);

			}catch (Exception e) {
				logger.catching(e);
			}
		}
	}
}
