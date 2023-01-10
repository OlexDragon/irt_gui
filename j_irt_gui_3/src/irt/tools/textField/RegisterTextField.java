package irt.tools.textField;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import irt.data.ThreadWorker;
import irt.data.RegisterValue;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketWork;
import irt.data.packet.PacketID;
import irt.data.packet.Payload;
import irt.data.packet.denice_debag.RegisterPacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.value.Value;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.IrtPanel;

public class RegisterTextField extends JTextField implements PacketListener, Runnable {
	private static final long serialVersionUID = 517630309792962880L;
	private final Logger logger = LogManager.getLogger();

	public final int MIN;
	public final int MAX;

	private final RegisterPacket getValuePacket;
	private final RegisterValue valueToSend;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private ScheduledExecutorService	service;

	private Byte unitAddress;
	private PacketID packetId;
	private PacketID packetId_Set;
	private Timer showActionTimer;

	private Timer focusListenerTimer = new Timer((int) TimeUnit.SECONDS.toMillis(10), a->start());
	final FocusAdapter focusListener = new FocusAdapter() {
		@Override
		public void focusGained(FocusEvent e) {
			focusListenerTimer.restart();
			stop();
		}
	};
	private final String toolTip;

	public RegisterTextField(Byte linkAddr, RegisterValue registerValue, PacketID packetId, int min, int max) {
		addFocusListener(focusListener);

		focusListenerTimer.setRepeats(false);
		int index = registerValue.getIndex();
		final int addr = registerValue.getAddr();

		toolTip = "Index=" + index + "; Address=" + addr;
		setToolTipText(toolTip);

		MIN = min;
		MAX = max;
		unitAddress = linkAddr;
		this.packetId = packetId;
		packetId_Set = PacketID.valueOf(packetId.name() + "_SET");

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->stop()));

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
		getValuePacket = new RegisterPacket(linkAddr, registerValue, packetId);
		GuiControllerAbstract.getComPortThreadQueue().add(getValuePacket);

		valueToSend = new RegisterValue(registerValue);
		valueToSend.setValue( new Value(MAX, MIN, MAX, 0)); // if value not null packet type is COMMAND
		logger.debug("valueToSend: {}", valueToSend);

		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				send();
			}
		});
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
//				 logger.error("*** {} ***", keyChar);

				 if(!e.isShiftDown() && keyChar==KeyEvent.VK_ESCAPE){
					 start();
					 return;
				 }

				 if(e.isShiftDown() && keyChar==KeyEvent.VK_ESCAPE){
					 stop();

//				 }else  if(e.isControlDown() && c==KeyEvent.VK_SHIFT){// VK_SHIFT equals CTRL 'P'
//					 logger.error("*** {} ***", keyChar);
//						setToolTipText("Data copied to the clipboard.");

					 return;
				 }

				 if(!(Character.isDigit(keyChar) || keyChar==KeyEvent.VK_BACK_SPACE || keyChar==KeyEvent.VK_DELETE) || keyChar==KeyEvent.VK_ENTER || keyChar==0){
					getToolkit().beep();
					e.consume();
					return;
				}

				 focusListener.focusGained(null);
			}
			@Override public void keyReleased(KeyEvent e) { }
			@Override public void keyPressed(KeyEvent e) { }
		});
	}

	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{

			try{

				final Optional<Packet> o = Optional.ofNullable(packet);

				if(!o.isPresent())
					return;

				byte addr = o.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);

				if(addr!=unitAddress)
					return;

				final String idName = packetId.name();
				final short packetId_Save = Optional.of(idName).filter(i->i.startsWith("DEVICE_DEBUG_POTENTIOMETER_")).map(i->PacketID.valueOf(i + "_SAVE").getId()).orElse((short) -1);

				final Optional<PacketHeader> sameGroupId = o.map(Packet::getHeader)
															.filter(h->h.getPacketId()==packetId.getId() || h.getPacketId()==packetId_Set.getId() || h.getPacketId()==packetId_Save)
															.filter(h->PacketGroupIDs.DEVICE_DEBUG.match(h.getGroupId()));

				if(!sameGroupId.isPresent())
					return;

				Optional<PacketHeader> hasResponse = sameGroupId.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

				if(!hasResponse.isPresent()){
					showAction(Color.PINK);
					logger.warn("Unit is not connected {}", packet);
					return;
				}

				final Optional<PacketHeader> noError = hasResponse.filter(h->h.getError()==PacketImp.ERROR_NO_ERROR);

				if(!noError.isPresent()){
					showAction(Color.RED);
					logger.warn("Packet has error {}", packet);

					setToolTipText("<html>" + toolTip + "<BR>" + hasResponse.get().getErrorStr() + "</html>");
					return;
				}

				showAction(Color.YELLOW);
				noError
				.map(h->packet.getPayloads())
				.map(pls->pls.parallelStream())
				.orElse(Stream.empty())
				.filter(pl->pl.getParameterHeader().getCode()==PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE)
				.map(Payload::getRegisterValue)
				.forEach(rv->{

					final short pID = noError.get().getPacketId();
					if(pID==packetId_Save)
						showSaved();

					else {
						final int aR = rv.getAddr();
						final int aV = valueToSend.getAddr();
						if(aR==aV) 
							setValueToTextField(rv);
					}
				});
			}catch(Exception e){
				logger.catching(e);
			}
		}, "RegisterTextField.onPacketReceived()");
	}

	public void showAction(Color bg) {

		if(showActionTimer!=null && showActionTimer.isRunning())
			return;

		Border border = getBorder();

		showActionTimer = new Timer(500, e->{setBorder(border); showActionTimer.stop();});
		showActionTimer.start();

		setBorder(new LineBorder(bg));
	}

	public void start(){
		if(Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).isPresent())
			return;

		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("RegisterTextField"));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(RegisterTextField.this);

		scheduleAtFixedRate = service.scheduleAtFixedRate(RegisterTextField.this, 1, 3, TimeUnit.SECONDS);
	}

	public void stop(){
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(RegisterTextField.this);
		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	public void saveRegister(){

		start();

		int a;
		int index = valueToSend.getIndex();
		final int addr = valueToSend.getAddr();

		switch(index) {
		case 26:
			final String dacHotMute = IrtPanel.PROPERTIES.getProperty("dac-hot-mute");
			final boolean hasDacHotMute = Optional.ofNullable(dacHotMute).filter(pr->pr.equals("0")).isPresent();
			if(hasDacHotMute) {
				a = addr;
				index++;
			}else
				a = addr+0x10;
			break;

		case 30: //ka band
			a = addr==0 ? 16 : 17;
			break;
		default:
			a = addr+3;
		}

		RegisterValue valueToSave = new RegisterValue(index, a, null);

		getValue().ifPresent(
				v->{
					final Value value = new Value(v, v, v, 0);
					valueToSave.setValue(value);
				});
		
		if(valueToSave.getValue()==null)
			return;

		logger.debug("valueToSave: {}", valueToSave);

		PacketID packetId_Save = PacketID.valueOf(packetId.name() + "_SAVE");
		GuiControllerAbstract.getComPortThreadQueue().add(getPacketToSend(packetId_Save, valueToSave));
	}

	public void send() {
		final String text = getText().replaceAll("\\D", "");
		if(text.isEmpty()){
		    getToolkit().beep();
		    return;
		}

		final int value = Integer.parseInt(text);
		valueToSend.getValue().setValue(value);

		GuiControllerAbstract.getComPortThreadQueue().add(getPacketToSend(packetId_Set, valueToSend));
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

	private PacketWork 		getPacketToSend(PacketID pId, RegisterValue value)					{ return logger.traceExit(new RegisterPacket(unitAddress, value, pId)); }
	private void 			setValueToTextField(final RegisterValue registerValue) 	{ SwingUtilities.invokeLater(()->Optional.of(registerValue.getValue().toString()).filter(str->!str.equals(getText())).ifPresent(this::setText)); }

	@Override
	public void				 run() 		{ GuiControllerAbstract.getComPortThreadQueue().add(getValuePacket); }

	/** @return value from textField */
	public Optional<Integer> getValue() { return Optional.of(getText().replace(",", "")).filter(t->!t.isEmpty()).map(Integer::parseInt); }
}
