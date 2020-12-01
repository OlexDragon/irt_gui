package irt.controller;

import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import irt.controller.interfaces.DescriptionPacketValue;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.value.getter.ValueChangeListenerClass;
import irt.data.ThreadWorker;
import irt.data.Range;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketWork;
import irt.data.packet.Payload;
import irt.data.packet.PacketIDs;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.RangePacket;
import irt.data.value.Value;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;

public class ValueController extends ValueChangeListenerClass implements Runnable, PacketListener {

	public static final PacketIDs RANGE = PacketIDs.CONFIGURATION_GAIN_RANGE;
	public static final PacketIDs VALUE = PacketIDs.CONFIGURATION_GAIN;

	private final 	PacketListener 			pl 		= this;
	private final 	ComPortThreadQueue 		cptq 	= GuiControllerAbstract.getComPortThreadQueue();
	private 		ScheduledFuture<?> 		scheduleAtFixedRate;

	private 		DescriptionPacketValue 	descriptionPacketValue;
	private			PacketWork				packetToSend;
	private			Range					range;
	private			Value					value;

	private final	byte 							linkAddr;
	private final JComboBox<DescriptionPacketValue> comboBox;

	public ValueController(byte linkAddr, final JComboBox<DescriptionPacketValue> comboBox) {

		this.linkAddr = linkAddr;
		this.comboBox = comboBox;
		comboBox.addAncestorListener(ancesorListener);
		comboBox.addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(
						c->{
							GuiControllerAbstract.getComPortThreadQueue().removePacketListener(pl);
							service.shutdownNow();
						}));

		reset(comboBox);
	}

	@Override
	public void run() {
		try{

			synchronized (logger) {
				cptq.add(packetToSend);
			}

		}catch(Exception e){
			logger.catching(e);
		}
	}

	private boolean errorWasRecorded;
	@Override
	public void onPacketReceived(final Packet packet) {

		new ThreadWorker(()->{
			
			synchronized (logger) {
				final short id = ((PacketSuper)packetToSend).getHeader().getPacketId();

				final Optional<PacketHeader> p = Optional
													.ofNullable(packet)
													.map(Packet::getHeader)
													.filter(h->h.getPacketId()==id);

				p
				.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
				.ifPresent(
						h->{
							errorWasRecorded = false;
							final Payload pl = packet.getPayload(0);

							if(packetToSend instanceof RangePacket)
								range(pl);

							else
								value(pl);
						});

				p
				.filter(h->h.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE)
				.filter(h->!errorWasRecorded)
				.ifPresent(
						h->{
							logger.warn("the unit does not respond: {}", packet);
							errorWasRecorded = true;
						});
			}
		}, "ValueController.onPacketReceived()");
	}

	private void value(final Payload payload) {
		final Value v = descriptionPacketValue.getValue(range.getMinimum(), range.getMaximum());

		switch(payload.getParameterHeader().getSize()){
		case 2:
			v.setValue(payload.getShort(0));
			break;
		default:
			v.setValue(payload.getLong(0));
		}

		if(value==null || !value.equals(v)){
			value = v;
			fireValueChangeListener(new ValueChangeEvent(v, VALUE));
		}
	}

	private void range(final Payload pl) {
		range = new Range(pl);

		fireValueChangeListener(new ValueChangeEvent(range, RANGE));

		synchronized (logger) {
			packetToSend = descriptionPacketValue.getPacketWork();
		}

		reset();
	}

	public void duUpdate(boolean doUpdate) {

		if(doUpdate){
			if(scheduleAtFixedRate.isCancelled())
				scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 3000, TimeUnit.MILLISECONDS);
		}else
			scheduleAtFixedRate.cancel(true);
	}

	private PacketWork createNewPacket() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		synchronized (logger) {
			final Class<?> clazz = Class.forName(packetToSend.getClass().getName());
			final Class<?> parameterType = descriptionPacketValue.getParameterType();
			final Constructor<?> constructor = clazz.getConstructor(Byte.class, parameterType);

			Object parameter;
			switch(parameterType.getSimpleName()){
			case "Short":
				parameter = (short)value.getValue();
				break;
			default:
				parameter = value.getValue();
			}

			return (PacketWork) constructor.newInstance(new Object[]{linkAddr, parameter});
		}
	}

	public Value getValue() {
		return value!=null ? value.getCopy() : null;
	}

	public void setValue(JTextField valueTextField, JSlider slider) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		if(value!=null && !(value instanceof RangePacket)){

			if(slider.getValue()!=value.getRelativeValue())
				value.setRelativeValue(slider.getValue());
			else
				value.setValue(valueTextField.getText());

			if(value.hasChanged()){

				final PacketWork packet = createNewPacket();
				cptq.add(packet);
				resetValue();
			}
		}
	}

	public void resetValue() {
		value = null;
		duUpdate(true);
	}

	private void reset() {
		duUpdate(false);
		resetValue();
	}

	public void reset(final JComboBox<DescriptionPacketValue> comboBox) {

		synchronized (logger) {
			descriptionPacketValue = (DescriptionPacketValue) comboBox.getSelectedItem();
			packetToSend = descriptionPacketValue.getRangePacket();
		}

		logger.trace(packetToSend);
		if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(true);

		scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 3000, TimeUnit.MILLISECONDS);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   Listeners   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private final ItemListener itemListener = new ItemListener() {
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange()==ItemEvent.SELECTED){
				synchronized (logger) {
					descriptionPacketValue = (DescriptionPacketValue) comboBox.getSelectedItem();
				}
			}
		}
	};
	private final AncestorListener ancesorListener = new AncestorListener() {
		
		@Override
		public void ancestorAdded(AncestorEvent event) {
			cptq.addPacketListener(pl);
			comboBox.addItemListener(itemListener);
		}
		@Override
		public void ancestorRemoved(AncestorEvent event) {
			cptq.removePacketListener(pl);
			scheduleAtFixedRate.cancel(true);
			comboBox.removeItemListener(itemListener);
			comboBox.removeAncestorListener(this);
			scheduleAtFixedRate.cancel(true);
			service.shutdown();
			removeVlueChangeListeners();
		}
		@Override public void ancestorMoved(AncestorEvent event) { }
	};
}
