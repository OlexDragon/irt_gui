package irt.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import irt.IrtGuiProperties;
import irt.controllers.serial_port.SerialPortController;
import irt.data.FractionalNumberPlusPrefixChecker;
import irt.data.GuiUtility;
import irt.data.LinkedPacketsQueue;
import irt.data.Value;
import irt.data.ValueDouble;
import irt.data.ValueFrequency;
import irt.data.packets.PacketParsingException;
import irt.data.packets.core.Packet;
import irt.data.packets.core.PacketHeader;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.enums.PacketType;
import irt.data.packets.interfaces.LinkedPacket;
import irt.data.packets.interfaces.RangePacket;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class TextFieldConfiguration extends TextFieldAbstract {

	private static final int MULTIPLIER = 1000000;

	private static final FractionalNumberPlusPrefixChecker FRACTIONAL_NUMBER_CHECKER = new FractionalNumberPlusPrefixChecker();

	public static final String FXML_PATH = "/fxml/components/TextFieldConfiguration.fxml";
	public static final String PROPERTY_STARTS_WITH = "gui.control.";

	public static final String FIELD_KEY_ID 		= RegistersController.REGISTER_PROPERTIES 		+ "textField.control.%d.";
	public static final String FIELD_KEY	 		= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profikeId.column.row (ex. gui.regicter.controller.textField.control.3.5.7)

	public static final Class<? extends Node> rootClass = BorderPane.class;

	private Class<? extends LinkedPacket> packetClass;

	public final Updater updater = new Updater();

	@FXML private BorderPane 	borderPane;
    @FXML private Label label;
 
    @Override public void run() {
		// TODO Remove this function( Used for debug)
		super.run();
//		logger.error(packets);
	}

	@FXML private Menu menuConfiguration;

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)borderPane.getParent()).getChildren();
		nodes.remove(borderPane);
	}

	protected void setup() {
	}

	@Override public void setKeyStartWith(String propertiesKeyStartWith) {

		this.propertyName = propertiesKeyStartWith;

		//set title
		 Optional
		 .ofNullable(IrtGuiProperties.getProperty(propertiesKeyStartWith + NAME))
		 .ifPresent(name->Platform.runLater(()->{
			 label.setText(name);
			 label.setTooltip(new Tooltip(name));
		 }));

		 //Period
		 Optional
		 .ofNullable(IrtGuiProperties.getProperty(propertyName + PERIOD))
		 .ifPresent(p->setPeriod(Long.parseLong(p)));

		 // Packet class
		 Optional
		 .ofNullable(IrtGuiProperties.getProperty(propertiesKeyStartWith + CLASS))
		 .ifPresent(classNamw->{

			 value = null;
			 removeAllPackets();

			 Optional
			 .ofNullable(Packet.createNewPacketBy(classNamw))
			 .ifPresent(p->{
				 addPacket(p);
			 });

		 });

	}

	private LinkedPacket createNewPacket(Number value) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Constructor<? extends LinkedPacket> constructor;
//		logger.error(packetClass);

		LinkedPacket newInstance;
		if(packetClass == FrequencyPacket.class){
			constructor = packetClass.getConstructor(Long.class);
			newInstance = constructor.newInstance(value.longValue());
			
		}else{
			constructor = packetClass.getConstructor(Short.class);
			newInstance = constructor.newInstance(value.shortValue());
		}

		return newInstance;
	}

	@Override protected void createMenuItems() {

		EventHandler<ActionEvent> onActionRegisterSelect = e->{
			final MenuItem source = (MenuItem) e.getSource();
			setKeyStartWith(source.getId());
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, onActionRegisterSelect, menuConfiguration.getItems());
	}

	@Override protected void sendValue(Value value) throws PacketParsingException {

		final Optional<Value> val = Optional
				.ofNullable(value)
				.filter(v->!v.isError());

		//if value is present and does not have errors
		if (val.isPresent()) {
			try {

				final LinkedPacket packet = createNewPacket(value.getValue());
				packet.addObserver(this);
				SerialPortController.getQueue().add(packet, true);

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override public ChangeListener<String> getNumericChecker() {
		return FRACTIONAL_NUMBER_CHECKER;
	}

	@Override protected Node getRootNode() {
		return borderPane;
	}

	@Override protected Menu getMenu() {
		return menuConfiguration;
	}

	@Override public void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver) {

		addCssClass(cssClass);

		int factor = value.getFactor();
		if(value instanceof ValueFrequency)
			factor = factor * MULTIPLIER;

		final double max = (double)value.getMaxValue()/factor;
		final double min = (double)value.getMinValue()/factor;
		final double v = (double)value.getValue()/factor;

//		logger.error("{} : {} : {} : {}", v, min, max, factor);

			//set limit for text field
//			stepNumericChecker.setMaximum(max);

		//set slider values
		setSliderValue(slider, sliderChangeListener, v, min, max);

		value.addObserver(valueObserver);
	}

	public void addCssClass(String cssClass) {
		final ObservableList<String> styleClass = textField.getStyleClass();
		if(styleClass.size()>0)
			styleClass.remove(cssClass);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException
		styleClass.add(cssClass);
	}

	@Override protected void setPacket(String keyStartWith) throws PacketParsingException { }
	@Override public void save() throws PacketParsingException { }

	@Override public void setText(double value) {
		Value v = this.value.getCopy();
		if(v instanceof ValueFrequency){
			BigDecimal bd = new BigDecimal(value);
			BigDecimal bm = new BigDecimal(MULTIPLIER);
			bd = bd.multiply(bm);
			final BigInteger bi = bd.toBigInteger();
			v.setValue(bi.longValue());

//			logger.error("value={}, bm={}, bd={}, bi={}, bi.long={}, v={}, v.isError()={}", value, bm, bd, bi, bi.longValue(), v, v.isError());
		}else
			v.setValue(value);

		setText(v.toString(), FRACTIONAL_NUMBER_CHECKER);
	}

	@Override public void update(Observable observable, Object arg) {
		LinkedPacketsQueue.SERVICES.execute(updater.setPacket(((LinkedPacket)observable)));
	}

	@Override public int getMultiplier() {
		return MULTIPLIER;
	}

	//********************************************** class Updater   ***************************************************
	private class Updater implements Runnable{

		private LinkedPacket packet;

		Updater setPacket(LinkedPacket packet){
			this.packet = packet;
			return this;
		}

		@Override public void run() {
//			if(packet instanceof FrequencyPacket)
//				logger.error(packet);

			if(packet.getPacketHeader().getPacketType()==PacketType.COMMAND)
				packet.deleteObservers();


			if(packet.getAnswer()==null)
				return;

			try {

				LinkedPacket p = (LinkedPacket) Packet.createNewPacket(packet.getClass(), packet.getAnswer(), true);
				final PacketHeader header = p.getPacketHeader();
//
//				if(packet.getPacketHeader().getPacketType()==PacketType.COMMAND)
//					logger.error(p);

				if(header.getPacketType()==PacketType.RESPONSE && header.getPacketError()==PacketErrors.NO_ERROR){
					if(p instanceof RangePacket)
						setRange(p);

					else{
						setTextFieldValue(p);
					}
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}

		protected void setRange(LinkedPacket packet) {

			if(value==null){
				stop(true);
				removeAllPackets();
				setValue(packet);

				//add value packet
				Optional
				 .ofNullable(Packet.createNewPacketBy(packet.getClass().getName().replace("Range", "")))
				 .ifPresent(p->{
					 addPacket(p);
					 packetClass = p.getClass();

					 if(!scheduleAtFixedRate.isCancelled())
						 start();
				});
			}
		}

		private void setValue(LinkedPacket packet) {

			int precision = Optional
					.ofNullable(IrtGuiProperties.getLong(propertyName + PRECISION, null))
					.orElse(0l)
					.intValue();

			if(precision==0){

				final long[] array = packet.getPayloads().get(0).getArrayLong();
				value = new ValueFrequency(array[0], array[0], array[1]);

			}else{

				final short[] array = packet.getPayloads().get(0).getArrayOfShort();
				value = new ValueDouble(array[0], array[0], array[1], precision);
			}

			 //Prefix
			 Optional
			 .ofNullable(IrtGuiProperties.getProperty(propertyName + PREFIX))
			 .ifPresent(p->value.setPrefix(p));

			logger.debug("\n\t"
						+ "value: {}\n\t"
						+ "precision: {}", value, precision);

			FRACTIONAL_NUMBER_CHECKER.setMaximum((double)value.getMaxValue()/value.getFactor());
		}

		private void setTextFieldValue(LinkedPacket packet) {
			long v;
			if(packet instanceof FrequencyPacket)
				v = packet.getPayloads().get(0).getLong();
			else
				v = packet.getPayloads().get(0).getShort(0) & 0x0000FFFF;
			value.setValue(v);

			
			setText(value.toString(), FRACTIONAL_NUMBER_CHECKER);
		}
	}
}
