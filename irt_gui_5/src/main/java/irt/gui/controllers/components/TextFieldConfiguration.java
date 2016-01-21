package irt.gui.controllers.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.data.GuiUtility;
import irt.gui.data.listeners.FractionalNumberPlusPrefixChecker;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.data.packet.observable.configuration.FrequencyPacket;
import irt.gui.data.value.Value;
import irt.gui.data.value.ValueDouble;
import irt.gui.data.value.ValueFrequency;
import irt.gui.errors.PacketParsingException;
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

public class TextFieldConfiguration extends TextFieldAbstract {

	private static final FractionalNumberPlusPrefixChecker FRACTIONAL_NUMBER_CHECKER = new FractionalNumberPlusPrefixChecker();

	public static final String FXML_PATH = "/fxml/components/TextFieldConfiguration.fxml";
	public static final String PROPERTY_STARTS_WITH = "gui.control.";

	public static final Class<? extends Node> rootClass = BorderPane.class;

	private Class<? extends LinkedPacket> packetClass;

	public final Updater updater = new Updater();

	@FXML private BorderPane 	borderPane;
    @FXML private Label label;
    @FXML private Menu menuConfiguration;

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

			 // Stop sending packets
			 stop(true);

			 value = null;
			 removeAllPackets();

			 Optional
			 .ofNullable(createNewPacket(classNamw))
			 .ifPresent(p->{
				 addPacket(p);
				 start();
			 });

		 });

	}

	private LinkedPacket createNewPacket(Number value) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Constructor<? extends LinkedPacket> constructor;

		LinkedPacket newInstance;
		if(packetClass.isInstance(ValueFrequency.class)){
			constructor = packetClass.getConstructor(Integer.class);
			newInstance = constructor.newInstance(value.intValue());
		}else{
			constructor = packetClass.getConstructor(Short.class);
			newInstance = constructor.newInstance(value.shortValue());
		}

		return newInstance;
	}

	private LinkedPacket createNewPacket(String className) {
		logger.entry(className);

		LinkedPacket packet = null;
		try {

			@SuppressWarnings("unchecked")
			Class<? extends LinkedPacket> clazz = (Class<? extends LinkedPacket>) Class.forName(className);
			packet = (LinkedPacket) clazz.newInstance();

		} catch (Exception e) {
			logger.catching(e);
		}



		return packet;
	}

	@Override protected void createMenuItems() {

		EventHandler<ActionEvent> onActionRegisterSelect = e->{
			final MenuItem source = (MenuItem) e.getSource();
			setKeyStartWith(source.getId());
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, onActionRegisterSelect, menuConfiguration.getItems());
	}

	private void setValue(LinkedPacket packet) {

		int precision = Optional
				.ofNullable(IrtGuiProperties.getLong(propertyName + PRECISION))
				.orElse(0l)
				.intValue();

		if(precision==0){

			final long[] array = packet.getPayloads().get(0).getArrayLong();
			value = new ValueFrequency(array[0], array[0], array[1]);
			logger.error("\n\tRange: {}", array);

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

	@Override protected void sendValue(Value value) throws PacketParsingException {
		logger.trace(value);

		final Optional<Value> val = Optional
				.ofNullable(value)
				.filter(v->!v.isError());

		//if value is present and does not have errors
		if (val.isPresent()) {
			try {

				final LinkedPacket packet = createNewPacket(value.getValue());
				packet.addObserver(this);
				SerialPortController.QUEUE.add(packet);

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

	@Override public void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver, NumericChecker stepNumericChecker) {

		addCssClass(cssClass);

		final int factor = value.getFactor();
		final double max = (double)value.getMaxValue()/factor;
		final double min = (double)value.getMinValue()/factor;
		final double v = (double)value.getValue()/factor;
		logger.trace("{} : {} : {} : {}", v, min, max, factor);

			//set limit for text field
//			stepNumericChecker.setMaximum(max);

		//set slider values
		setSliderValue(slider, sliderChangeListener, v, min, max);

		value.addObserver(valueObserver);
	}

	public void addCssClass(String cssClass) {
		final ObservableList<String> styleClass = textField.getStyleClass();
		if(!styleClass.contains(cssClass))
			styleClass.add(cssClass);
	}

	@Override protected void setPacket(String keyStartWith) throws PacketParsingException { }
	@Override public void save() throws PacketParsingException { }

	private final NumberFormat formatter = new DecimalFormat("#0.0");
	@Override public void setText(double value) {
		setText(formatter.format(value), FRACTIONAL_NUMBER_CHECKER);
	}

	@Override public void update(Observable observable, Object arg) {
		logger.trace("{} : {}", observable, arg);
		updater.setPacket(((LinkedPacket)observable));
		SERVICES.execute(updater);
	}

	//********************************************** class Updater   ***************************************************
	private class Updater implements Runnable{

		final Logger logger = LogManager.getLogger();

		private LinkedPacket packet;

		void setPacket(LinkedPacket packet){
			this.packet = packet;
		}

		@Override
		public void run() {
			
			try {

				if(packet.getAnswer()==null)
					return;

				packetClass = packet.getClass();
				final Constructor<? extends LinkedPacket> constructor = packetClass.getConstructor(byte[].class);
				LinkedPacket p = constructor.newInstance(this.packet.getAnswer());
				final PacketHeader header = p.getPacketHeader();

				if(header.getPacketType()==PacketType.RESPONSE && header.getPacketErrors()==PacketErrors.NO_ERROR){
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
				 .ofNullable(createNewPacket(packet.getClass().getName().replace("Range", "")))
				 .ifPresent(p->{
					 addPacket(p);
					 packetClass = p.getClass();
					 start();
				});

			}
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
