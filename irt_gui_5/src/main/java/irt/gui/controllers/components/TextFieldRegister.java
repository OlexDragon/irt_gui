package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.data.GuiUtility;
import irt.gui.data.RegisterValue;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.Register;
import irt.gui.data.packet.observable.device_debug.RegisterPacket;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class TextFieldRegister extends TextFieldAbstract implements Register{

	private static final int MULTIPLIER = 1;
	private static final NumericChecker NUMERIC_CHECKER = new NumericChecker();

	public static final String FXML_PATH = "/fxml/components/TextFieldRegister.fxml";
	public static final String PROPERTY_STARTS_WITH	= "gui.textField.register.";

	public static final String FIELD_KEY_ID 		= RegistersController.REGISTER_PROPERTIES 		+ "textField.%d.";
	public static final String FIELD_KEY	 		= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profikeId.column.row (ex. gui.regicter.controller.textField.3.5.7)

	public static final Alert alert = new Alert(AlertType.INFORMATION);
	static{
		alert.setTitle("Save");
		alert.setResizable(true);
		alert.setHeight(500);
		alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setWrapText(true));
	}
	private static StringBuilder alertBuilder = new StringBuilder();

	public static final Class<? extends Node> rootClass = TextField.class;

	public final Updater updater = new Updater();

	private	RegisterValue 	registerValueToSet;			//Register's index and address
	private	Value			savedValue;					// Saved value in the register
	private RegisterPacket 	valuePacket;
	private RegisterPacket 	isSetValuePacket;

	@FXML private TextField textField;
	@FXML private Menu 		menuRegister;

	private String name;

	@Override protected void setup(){	}

	@FXML private void onActionMenuItemReset() {
				try {
					reset();
				} catch (PacketParsingException e) {
					logger.catching(e);
				}
	}

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)textField.getParent()).getChildren();
		nodes.remove(textField);
	}

	public void save() throws PacketParsingException {

		final RegisterValue registerValue = new RegisterValue(registerValueToSet.getIndex(), registerValueToSet.getAddr()+3, 0);
		RegisterPacket packet = new RegisterPacket("TextFieldRegister.save()", registerValue);
		packet.addObserver(new Observer() {

			private int times = 3;

			@Override
			public void update(Observable observable, Object object) {
				LinkedPacketsQueue.SERVICES.execute(new Runnable() {

						@Override
						public void run() {
							if(observable instanceof LinkedPacket){
								try {
									alertBuilder.setLength(0);

									if(alert == null || !alert.isShowing())
										Platform.runLater(()->{

											alert.setContentText("");
											alert.show();
										});

									RegisterPacket p = new RegisterPacket(((LinkedPacket)observable).getAnswer(), true);

									if((p.getPacketHeader().getPacketType()!=PacketType.RESPONSE || p.getPacketHeader().getPacketError()!=PacketErrors.NO_ERROR) && --times>=0){

										logger.warn("Not posible to save register " + registerValue);
										Thread.sleep(20);
										SerialPortController.getQueue().add(packet, true);
										packet.deleteObservers();

										Platform.runLater(()->{
											alertBuilder.append(alert.getContentText());
											alertBuilder.append(String.format("\nImpossible to save register %s", name));
											alert.setContentText(alertBuilder.toString());
//											alert.setContentText("I have a great message for you!");

										});

									}else{

										Platform.runLater(()->{
											alertBuilder.append(alert.getContentText());
											alertBuilder.append(String.format("\nRegister %s has been saved", name));
											alert.setContentText(alertBuilder.toString());
//											alert.setContentText("I have a great message for you!");
										});
									}
									 
//									alert.getDialogPane().setMinHeight(200);
									Platform.runLater(()->{
										alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Labeled).forEach(node -> ((Labeled)node).setMinHeight(Region.USE_PREF_SIZE));
									});

								} catch (Exception e) {
									logger.catching(e);
									packet.deleteObservers();
								}
							}
						}
					});
					}
			});
			SerialPortController.getQueue().add(packet, true);
	}

	@Override protected void setPacket(String propertiesKeyStartWith) throws PacketParsingException {
		logger.traceEntry(propertiesKeyStartWith);

		removeAllPackets();

		name = IrtGuiProperties.getProperty(propertiesKeyStartWith + "name");
		int index = Integer.parseInt(IrtGuiProperties.getProperty(propertiesKeyStartWith + INDEX));
		int address = Integer.parseInt(IrtGuiProperties.getProperty(propertiesKeyStartWith + ADDRESS));

		registerValueToSet = new RegisterValue(index, address);
		valuePacket = new RegisterPacket(propertiesKeyStartWith, registerValueToSet);
		addPacket(valuePacket);

		RegisterValue rv = new RegisterValue(index, address==0 ? 0x10+2 : 0x10+3); //0x10+2 --> RDAC:MEM2; 0x10+3 --> RDAC:MEM3
		isSetValuePacket = new RegisterPacket(propertiesKeyStartWith, rv);
		addPacket(isSetValuePacket);
	}

	@Override protected Value setValues(String keyStartWith) {
		Value v = super.setValues(keyStartWith);
		savedValue = new Value(null, v.getMinValue(), v.getMaxValue(), 0);
		return value;
	}

	@Override protected void createMenuItems() {

		EventHandler<ActionEvent> onActionRegisterSelect = e->{
			final MenuItem source = (MenuItem) e.getSource();
			setKeyStartWith(source.getId());
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, onActionRegisterSelect, menuRegister.getItems());
	}

	private void compareValues() {

		if(value.equals(savedValue) && !savedValue.isError())
			Platform.runLater(()->textField.getStyleClass().remove(CLASS_NOT_SAVED));

		else
			Platform.runLater(()->{
				final ObservableList<String> styleClass = textField.getStyleClass();
				if(styleClass.size()>0)
					styleClass.remove(CLASS_NOT_SAVED);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException
				styleClass.add(CLASS_NOT_SAVED);	//add to end of the list
			});
	}

	public void reset() throws PacketParsingException {
		sendValue(savedValue);
	}

	protected void sendValue(final Value value) throws PacketParsingException {

		final Optional<Value> val = Optional
				.ofNullable(value)
				.filter(v->!v.isError());

		if (val.isPresent()) {
			int i = registerValueToSet.getIndex();
			int a = registerValueToSet.getAddr();
			int v = Optional
					.ofNullable(value.getValue())
					.map(l->l.intValue())
					.orElse(-1);
			if(v>=0){
				RegisterValue rValue = new RegisterValue(i, a, v);
				RegisterPacket packet  = new RegisterPacket("TextFieldRegister.sendValue(value); value = " + value, rValue);
				packet.addObserver(this);
				SerialPortController.getQueue().add(packet, true);
			}
		}
	}

	@Override public ChangeListener<String> getNumericChecker() {
		return NUMERIC_CHECKER;
	}

	@Override protected Node getRootNode() {
		return textField;
	}

	@Override protected Menu getMenu() {
		return menuRegister;
	}

	@Override public void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver, NumericChecker stepNumericChecker) {
		Platform.runLater(()->{

			final ObservableList<String> styleClass = textField.getStyleClass();

			Platform.runLater(()->{
				slider.setMinorTickCount(1);
				slider.setMajorTickUnit(10);
			});

			if(styleClass.size()>0)
				styleClass.remove(cssClass);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException
			styleClass.add(cssClass);

			final long max = value.getRelativeMaxValue();
			final int v = value.getRelativeValue();
			final int min = value.getRelativeMinValue();

			//set limit for text field
			stepNumericChecker.setMaximum(max);

			//set slider values
			setSliderValue(slider, sliderChangeListener, v, min, max);

			value.addObserver(valueObserver);
	   });
	}

	@Override public void update(Observable observable, Object object) {
//		if(!packets.contains(observable))
//			try {
//				logger.error("{}:{}:{} {} {}", this, ((LinkedPacket)observable).getObservers(), valuePacket.getObservers(), observable, valuePacket);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				throw new UnsupportedOperationException("Auto-generated method stub", e);
//			}

		if(packets.contains((LinkedPacket)observable))
			LinkedPacketsQueue.SERVICES.execute(updater.setPacket(((LinkedPacket)observable)));

		else
			((LinkedPacket)observable).deleteObserver(this);
	}

	@Override public int getMultiplier() {
		return MULTIPLIER;
	}

	//********************************************** class Updater   ***************************************************
	private class Updater implements Runnable{

		private LinkedPacket packet;
		private boolean writeLogger;
		private byte[] answer;
		private boolean error;

		Updater setPacket(LinkedPacket packet){
			answer = packet.getAnswer();
			writeLogger = answer==null && (this.packet==null || this.packet.getAnswer()!=null);
			this.packet = packet;
			return this;
		}

		@Override
		public void run() {
			try {

				if(answer==null){
					if(writeLogger)
						logger.warn("Package does not contain the answer. {}", packet);
					return;
				}

				LinkedPacket packet = new RegisterPacket(answer, true);
				error = false;

				final PacketHeader packetHeader = packet.getPacketHeader();
				if(packetHeader.getPacketType()==PacketType.RESPONSE){

					final PacketErrors packetErrors = packetHeader.getPacketError();
					if(packetErrors!=PacketErrors.NO_ERROR){
						showError(packetErrors);
						return;
					}

					RegisterValue rv = packet.getPayloads().get(0).getRegisterValue();

					//if it is set value
					if(registerValueToSet.equals(rv))

						setText(rv);

					else 
						//else if it's saved value
						setTooltip(rv);

				}else
					logger.warn("\n\t This Packet has error: {}", packet);

			} catch (Exception e) {
				if(!error){	//not to repeat the same error message
					error = true;
					logger.catching(e);
				}
			}
		}

		public void showError(PacketErrors packetErrors) {
			if(packet == valuePacket)
				setText((String)null);
			else
				setTooltip(packetErrors.toString());
		}

		private void setText(String text) {

			Optional
			.ofNullable(text)
			.filter(t->t.equals(textField.getText()))
			.orElseGet(()->{

				Platform.runLater(() ->{

					//remove CLASS_HAS_CHANGED css class
					final ObservableList<String> styleClass = textField.getStyleClass();
					if(styleClass.size()>0)
						styleClass.remove(CLASS_HAS_CHANGED);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException

					textField.setText(text);
				});

				compareValues();

				return null;
			});
		}

		/**
		 * If value changed set textField text
		 * @param registerValueToSet	- sent value
		 */
		private void setText(RegisterValue setRegisterValue) {

			Integer rv = setRegisterValue.getValue();
			setValue(value, rv);
			setText(value.toString());
		}

		private void setTooltip(String text) {

			Optional
			.ofNullable(textField.getTooltip())
			.filter(tt->tt.getText().equals(text))
			.orElseGet(()->{

				Platform.runLater(()->textField.setTooltip(new Tooltip(text)));

				compareValues();
				return null;
			});
		}

		private void setTooltip(RegisterValue savedRegisterValue) {

			final Integer sv = savedRegisterValue.getValue();
			setValue(savedValue, sv);
			setTooltip(sv.toString());
		}

		public void setValue(Value value, Integer rv) {
			Optional
			.ofNullable(value.getValue())
			.map(Long::intValue)
			.filter(i->i==rv)
			.orElseGet(()->{
				value.setValue(rv);
				return null;
			});
		}
	}

	@Override public void setText(double value) {
		textField.setText(Integer.toString(((int)value)));
	}
}
