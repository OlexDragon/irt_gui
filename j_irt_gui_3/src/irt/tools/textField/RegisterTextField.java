package irt.tools.textField;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

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
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RegisterTextField extends JTextField implements PacketListener, Runnable {
	private static final long serialVersionUID = 517630309792962880L;
	private final Logger logger = LogManager.getLogger();

	public final int MIN;
	public final int MAX;

	private PacketWork getPacket;
	private PacketWork setPacket;

	private final RegisterValue valueToSend;
	private final RegisterValue valueSaveRegister;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final 	ScheduledExecutorService	service = Executors.newScheduledThreadPool(5, new MyThreadFactory());

	private Byte unitAddress;
	private short packetId;
	private Timer timer;

	public RegisterTextField(Byte linkAddr, RegisterValue registerValue, short packetId, int min, int max) {
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				stop();
			}
		});

		MIN = min;
		MAX = max;
		unitAddress = linkAddr;
		this.packetId = packetId;

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(RegisterTextField.this);
							service.shutdownNow();
						}));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
				start();
			}
			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}
			public void ancestorMoved(AncestorEvent event) { }
		});

		registerValue.setValue(null); // if value is null packet type is REQIEST
		getPacket = new RegisterPacket(linkAddr, registerValue, packetId);

		valueToSend = new RegisterValue(registerValue);
		valueToSend.setValue( new Value(896, MIN, MAX, 0)); // if value not null packet type is COMMAND
		setPacket = new RegisterPacket(linkAddr, valueToSend, packetId);

		valueSaveRegister = new RegisterValue(registerValue.getIndex(), registerValue.getAddr()+3, new Value(0, 0, 0, 0));

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

				 if(!e.isShiftDown() && c==KeyEvent.VK_ESCAPE){
					 start();

				 }else if(e.isShiftDown() && c==KeyEvent.VK_ESCAPE){
					 stop();

//				 }else  if(e.isControlDown() && c==KeyEvent.VK_SHIFT){// VK_SHIFT equals CTRL 'P'
//					 logger.error("***");
//						setToolTipText("Data copied to the clipboard.");

				 }else if(!(Character.isDigit(c) || c==KeyEvent.VK_BACK_SPACE || c==KeyEvent.VK_DELETE) || c==KeyEvent.VK_ENTER || c==0){
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
				showAction(Color.PINK);
				logger.warn("Unit is not connected {}", packet);
				return;
			}

			final Optional<PacketHeader> noError = hasResponse.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR);

			if(!noError.isPresent()){
				showAction(Color.RED);
				logger.warn("Packet has error {}", packet);

				setToolTipText(hasResponse.get().getOptionStr());
				return;
			}

			showAction(Color.YELLOW);
			noError
			.map(h->packet.getPayloads())
			.map(pls->pls.parallelStream())
			.orElse(Stream.empty())
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
		}catch(Exception e){
			logger.catching(e);
		}
	}

	public void showAction(Color bg) {

		if(timer!=null && timer.isRunning())
			return;

		Border border = getBorder();

		timer = new Timer(500, e->{setBorder(border); timer.stop();});
		timer.start();

		setBorder(new LineBorder(bg));
	}

	public void start(){

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(RegisterTextField.this);

		if(!service.isShutdown() && (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled()))
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 3, TimeUnit.SECONDS);
	}

	public void stop(){
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(RegisterTextField.this);

		if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(true);
	}

	public void saveRegister(){

		start();
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
				setText("SAVED");
				new Timer(500, e->start()).start();
			} catch (Exception e) {
				logger.catching(e);
			}
		}

	@Override
	public void run() {

		try{

			GuiControllerAbstract.getComPortThreadQueue().add(getPacket);

		}catch (Exception e) {
			logger.catching(e);
		}
	}
}