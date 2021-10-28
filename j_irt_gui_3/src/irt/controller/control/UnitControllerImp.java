package irt.controller.control;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.ThreadWorker;
import irt.data.Range;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketWork;
import irt.data.packet.Packets;
import irt.data.packet.configuration.FrequencyPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.RangePacket;
import irt.data.packet.interfaces.ValueToString;
import irt.data.value.Value;
import irt.data.value.ValueDouble;

public class UnitControllerImp implements UnitController{

	private final String KEY;

	protected final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("UnitControllerImp"));

	protected JTextField txtGain;
	protected JSlider slider;
	private JTextField txtStep;

	private boolean rangesAreSet;

	private final RangePacket range;
	private final ValueToString packet;
	private final ValueToString value;

	private final ChangeListener sliderChange;

	private final ChangeListener sliderUpdateText;

	private final Timer focusListenerTimer = new Timer((int) TimeUnit.SECONDS.toMillis(5), a->onFocusLost());
	private final FocusListener txtGainFocusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			onFocusLost();
		}

		@Override
		public void focusGained(FocusEvent e) {
			onFocusGained();
		}
	};

	private final KeyListener txtGainKeyListener = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {

			if(e.getKeyChar()==KeyEvent.VK_ENTER)
				return;

			onFocusGained();
		}
		
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {}
	};

	private final ActionListener txtGainActionListener = a->{
		logger.traceEntry();

		setUnitValue();
		onFocusLost();
	};

	protected void setUnitValue() {
		Optional
		.of(txtGain.getText().replaceAll("[^\\d.E-]", ""))
		.filter(text->!text.isEmpty())
		.ifPresent(text->{
			try{

				double v = Double.parseDouble(text);

				Value value = new ValueDouble(v, minimum, maximum, 1);
				slider.setValue(value.getRelativeValue());

			}catch(Exception ex){
				logger.catching(ex);
			}
		});
	}

	private final ActionListener txtStepActionListener = a->{
		txtGain.requestFocus();
	};

	private final FocusListener txtStepFocusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {

			if(!rangesAreSet)
				return;

			final ValueToString p = createValuePacket(packet);
			p.setValue(txtStep.getText());

			((Optional<?>) p.getValue())
			.filter(Number.class::isInstance)
			.map(Number.class::cast)
			.map(Number::intValue)
			.ifPresent(spacing->{

				final int maximum = slider.getMaximum();

				if(spacing > maximum)
					spacing = maximum;

				if(spacing<=0)
					spacing = 1;

				if(slider.getMinorTickSpacing() == spacing)
					return;

				slider.setMinorTickSpacing(spacing);
				final String txt = value.valueToString(spacing);
				txtStep.setText(txt);
				GuiControllerAbstract.getPrefs().put(KEY, txt);
			});

			onFocusLost();
		}
		
		@Override public void focusGained(FocusEvent e) { }
	};

	protected long relative;
	protected long minimum;
	protected long maximum;

	public UnitControllerImp(JTextField txtGain, JSlider slider, JTextField txtStep, String key, RangePacket rangePacket, ValueToString packet) {
		KEY = key;

		txtGain.setText("");
		this.txtGain = txtGain;
		this.slider = slider;
		this.txtStep = txtStep;

		range = rangePacket;
		this.packet = packet;
		value = createValuePacket(packet);

		sliderChange = e->{

			if(slider.getValueIsAdjusting())
				return;

			final int sliderValue = slider.getValue();
			final long v = relative + sliderValue;

			value.setValue(v);
			GuiControllerAbstract.getComPortThreadQueue().add(value);
		};

		sliderUpdateText = e->{

			final int sliderValue = slider.getValue();
			final long v = relative + sliderValue;

			final String valueToString = packet.valueToString(v);

			if(!txtGain.getText().equals(valueToString))
				txtGain.setText(valueToString);
		};

		focusListenerTimer.setRepeats(false);
	}

	private ValueToString createValuePacket(ValueToString packet) {
		ValueToString vPacket;
		try {

			Class<?> clazz;
			Object v;

			if(packet instanceof FrequencyPacket) {
				clazz = Long.class;
				v = (long)0;

			}else {
				clazz = Short.class;
				v = (short)0;
			}
				
			final Constructor<? extends ValueToString> constructor =  packet.getClass().getConstructor(Byte.class, clazz);

			final byte addr = packet.getLinkHeader().getAddr();
			vPacket = constructor.newInstance(new Object[]{addr, v});

		} catch (Exception e) {
			logger.catching(e);
			vPacket = null;
		}
		return vPacket;
	}

	@Override
	public void run() {

		PacketWork p;
		if(rangesAreSet)
			p = packet;
		else
			p = range;

		logger.trace(p);
		GuiControllerAbstract.getComPortThreadQueue().add(p);
	}

	@Override
	public void start() {
		logger.traceEntry("{}", ()->getClass().getSimpleName());

		if(Optional.ofNullable(scheduledFuture).filter(sch->!sch.isCancelled()).isPresent())
			return;

		addSliderChangeListener(sliderChange);
		addSliderChangeListener(sliderUpdateText);
		txtGain.addFocusListener(txtGainFocusListener);
		txtGain.addKeyListener(txtGainKeyListener);
		txtGain.addActionListener(txtGainActionListener);

		final String txt = GuiControllerAbstract.getPrefs().get(KEY, "1.0 ");
		txtStep.setText(txt);
		txtStep.addFocusListener(txtStepFocusListener);
		txtStep.addActionListener(txtStepActionListener);

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduledFuture = service.scheduleAtFixedRate(this, 0, 15, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		logger.traceEntry("{}", ()->getClass().getSimpleName());

		if(!Optional.ofNullable(scheduledFuture).map(sch->!sch.isDone()).isPresent())
			return;

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);

		focusListenerTimer.stop();

		slider.removeChangeListener(sliderChange);
		slider.removeChangeListener(sliderUpdateText);
		txtGain.removeFocusListener(txtGainFocusListener);
		txtGain.removeKeyListener(txtGainKeyListener);
		txtGain.removeActionListener(txtGainActionListener);

		txtStep.removeActionListener(txtStepActionListener);
		txtStep.removeFocusListener(txtStepFocusListener);

		scheduledFuture.cancel(true);
	}

	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{
			
			if(isValuePacket(packet))
				setValue(Packets.cast(packet));

			else if(isRangePacket(packet))
				setRange(Packets.cast(packet));
		}, "UnitControllerImp.onPacketReceived(Packet)");
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

	private void setValue(Optional<? extends PacketSuper> optional) {

		optional
		.flatMap(p->(Optional<?>)p.getValue())
		.filter(Number.class::isInstance)
		.map(Number.class::cast)
		.map(Number::longValue)
		.ifPresent(value->{
			logger.trace("value={}", value);

			int v = (int) (value - relative);

			if(slider.getValue() == v)
				return;

			SwingUtilities.invokeLater(()->{

				slider.removeChangeListener(sliderChange);
				slider.setValue(v);
				addSliderChangeListener(sliderChange);
			});
		});
	}

	private void setRange(Optional<? extends PacketSuper> optional) {
		optional
		.flatMap(p->(Optional<?>)p.getValue())
		.filter(Range.class::isInstance)
		.map(Range.class::cast)
		.ifPresent(range->{

			minimum = range.getMinimum();
			maximum = range.getMaximum();

			if(minimum==Short.MIN_VALUE || maximum==Short.MIN_VALUE)	// Range is undefined
				return;

			relative = minimum;

			//TODO			slider.setToolTipText("from" + minimum + " to " + maximum);

			slider.removeChangeListener(sliderChange);
			slider.removeChangeListener(sliderUpdateText);
			slider.setMinimum(0);
			final int sliderMax = (int) (maximum - minimum);
			slider.setMaximum(sliderMax);
			addSliderChangeListener(sliderUpdateText);
			addSliderChangeListener(sliderChange);
			logger.trace("minimum={}; maximum={}; sliderMax={}", minimum, maximum, sliderMax);

			rangesAreSet = true;
			txtStepFocusListener.focusLost(null);

			GuiControllerAbstract.getComPortThreadQueue().add(this.packet);
		});
	}

	private void onFocusLost() {
		logger.traceEntry();
		focusListenerTimer.stop();
		sliderUpdateText.stateChanged(null);

		addSliderChangeListener(sliderUpdateText);
	}

	private void onFocusGained() {
		logger.traceEntry();
		focusListenerTimer.restart();

		slider.removeChangeListener(sliderUpdateText);
	}

	private void addSliderChangeListener(ChangeListener listener){

		final boolean present = Arrays.stream(slider.getChangeListeners()).filter(cl->cl==listener).findAny().isPresent();
		if(!present)
			slider.addChangeListener(listener);
	}
}
