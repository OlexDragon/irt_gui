package irt.controller.control;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.packet.AttenuationPacket;
import irt.data.packet.AttenuationRangePacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketWork;

public class UnitAttenuationController implements UnitController{

	private static final String KEY = "att_step";

	private final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private JTextField txtGain;
	private JSlider slider;
	private JTextField txtStep;

	private boolean limitsAreSet;

	private AttenuationPacket packet;
	private AttenuationPacket value;
	private AttenuationRangePacket range;


	DecimalFormat df = new DecimalFormat("0.0");  

	private ChangeListener sliderChange = e->{
		value.setValue((short)slider.getValue());
		logger.trace(value);
		GuiControllerAbstract.getComPortThreadQueue().add(value);
	};

	private ChangeListener sliderUpdateText = e->txtGain.setText(df.format(slider.getValue()/10.0) + " " + Translation.getValue(String.class, "db", "dB"));

	private Timer focusListenerTimer = new Timer((int) TimeUnit.SECONDS.toMillis(10), a->addChangeListener());
	private FocusListener txtGainFocusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			addChangeListener();
		}

		@Override
		public void focusGained(FocusEvent e) {
			removeChangeListener();
		}
	};

	private KeyListener txtGainKeyListener = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {

			if(e.getKeyChar()==KeyEvent.VK_ENTER)
				return;

			removeChangeListener();
		}
		
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}
	};

	private ActionListener txtGainActionListener = a->{

		Optional
		.of(txtGain.getText().replaceAll("[^\\d.E-]", ""))
		.filter(text->!text.isEmpty())
		.ifPresent(text->{
			try{

				double value = Double.parseDouble(text);
				slider.setValue((int) (value*10));

			}catch(Exception e){
				logger.catching(e);
			}
		});
		addChangeListener();
	};

	private ActionListener txtStepActionListener = a->{
		txtGain.requestFocus();
	};

	private FocusListener txtStepFocusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {

			Optional
			.of(txtStep.getText().replaceAll("[^\\d.E-]", ""))
			.filter(text->!text.isEmpty())
			.ifPresent(text->{
				try{

					double value = Double.parseDouble(text);
					slider.setMinorTickSpacing((int) (value*10));

					final String txt = df.format(value) + " " + Translation.getValue(String.class, "db", "dB");
					txtStep.setText(txt);
					GuiControllerAbstract.getPrefs().put(KEY, txt);

				}catch(Exception ex){
					logger.catching(ex);
				}
			});
			addChangeListener();
		}
		
		@Override public void focusGained(FocusEvent e) { }
	};

	public UnitAttenuationController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		this.txtGain = txtGain;
		this.slider = slider;
		this.txtStep = txtStep;

		range = new AttenuationRangePacket(linkAddr);
		packet = new AttenuationPacket(linkAddr, null);
		value = new AttenuationPacket(linkAddr, (short) 0);

		focusListenerTimer.setRepeats(false);
	}

	@Override
	public void run() {
		
		PacketWork p;
		if(limitsAreSet)
			p = packet;
		else
			p = range;

		GuiControllerAbstract.getComPortThreadQueue().add(p);
	}

	@Override
	public void start() {

		if(Optional.ofNullable(scheduledFuture).map(sch->!sch.isCancelled()).orElse(false))
			return;

		slider.addChangeListener(sliderChange);
		slider.addChangeListener(sliderUpdateText);
		txtGain.addFocusListener(txtGainFocusListener);
		txtGain.addKeyListener(txtGainKeyListener);
		txtGain.addActionListener(txtGainActionListener);

		txtStep.setText(GuiControllerAbstract.getPrefs().get(KEY, "1.0 " + Translation.getValue(String.class, "db", "dB")));
		txtStep.addActionListener(txtStepActionListener);
		txtStep.addFocusListener(txtStepFocusListener);

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {

		if(Optional.ofNullable(scheduledFuture).map(sch->sch.isCancelled()).orElse(true))
			return;

		focusListenerTimer.stop();

		slider.removeChangeListener(sliderChange);
		slider.removeChangeListener(sliderUpdateText);
		txtGain.removeFocusListener(txtGainFocusListener);
		txtGain.removeKeyListener(txtGainKeyListener);
		txtGain.removeActionListener(txtGainActionListener);

		txtStep.removeActionListener(txtStepActionListener);

		scheduledFuture.cancel(true);
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
	}

	@Override
	public void onPacketRecived(Packet packet) {

		if(isValuePacket(packet))
			setValue(packet);
			
		else if(isRangePacket(packet))
			setRange(packet);
	}

	private boolean isValuePacket(Packet packet) {
		return compareIDs(this.packet, packet);
	}

	private boolean isRangePacket(Packet packet) {
		return compareIDs(range, packet);
	}

	private boolean compareIDs(Packet packet1, Packet packet2) {
		final Optional<Short> id1 = Optional.ofNullable(packet1).map(Packet::getHeader).map(PacketHeader::getPacketId);
		final Optional<Short> id2 = Optional.ofNullable(packet2).map(Packet::getHeader).map(PacketHeader::getPacketId);
		return id1.isPresent() && id1.equals(id2);
	}

	private void setValue(Packet packet) {
		Optional
		.ofNullable(packet.getPayloads()).flatMap(pls->pls.stream().findAny())
		.map(pl->pl.getShort(0))
		.ifPresent(v->{
			slider.removeChangeListener(sliderChange);
			slider.setValue(v);
			slider.addChangeListener(sliderChange);
		});
	}

	private void setRange(Packet packet) {
		Optional
		.ofNullable(packet.getPayloads()).flatMap(pls->pls.stream().findAny())
		.map(Payload::getArrayShort)
		.filter(arr->arr.length==2)
		.ifPresent(arr->{

			slider.removeChangeListener(sliderChange);
			slider.setMinimum(arr[0]);
			slider.setMaximum(arr[1]);
			slider.addChangeListener(sliderChange);

			limitsAreSet = true;
			GuiControllerAbstract.getComPortThreadQueue().add(this.packet);
		});
	}

	private void addChangeListener() {
		focusListenerTimer.stop();
		sliderUpdateText.stateChanged(null);
		slider.addChangeListener(sliderUpdateText);
	}

	private void removeChangeListener() {
		focusListenerTimer.restart();
		slider.removeChangeListener(sliderUpdateText);
	}
}
