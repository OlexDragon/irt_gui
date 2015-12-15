package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Observer;

import irt.gui.IrtGuiProperties;
import irt.gui.data.GuiUtility;
import irt.gui.data.RegisterValue;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.TextFieldFocusListener;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.device_debug.RegisterPacket;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RegisterTextField extends ScheduledNode {

	public static final String FXML_PATH = "/fxml/components/RegisterTextField.fxml";

	public static final String PROPERTY_STARTS_WITH	= "gui.register.textField.";
	public static final String START 				= "start";
	public static final String SET 					= "set";
	public static final String SAVE 				= "save";
	public static final String RESET 				= "reset";
	public static final String STOP 				= "stop";
	public static final String CLASS_NOT_SAVED 		= "notSaved";
	public static final String CLASS_HAS_CHANGED 	= "hasChanged";

	public static final String FIELD_KEY_ID 		= RegistersController.REGISTER_PROPERTIES 		+ "textField.%d.";
	public static final String FIELD_KEY	 		= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profikeId.column.row (ex. gui.regicter.controller.textField.3.5.7)

	private					RegisterValue 	setRegisterValue;			//Register's index and address
	private	volatile		Value			value;						/*Actual value	*/							public Value getValue() { return value; }
	private					Value			savedValue;					//Saved value in the register
	private final 			NumericChecker 	numericChecker 	= new NumericChecker();

	@FXML private TextField textField;
	@FXML private Menu 		menuRegister;
	@FXML private MenuItem	startStopMenuItem;
	@FXML private MenuItem	saveMenuItem;
	@FXML private MenuItem	resetMenuItem;
	@FXML private MenuItem	setMenuItem;

	@FXML public void initialize(){

		final StringProperty textProperty = textField.textProperty();
		textProperty.addListener(( ob, oldV, newV)->{ checkForValueChange(newV); });
		textProperty.addListener(numericChecker);
		new TextFieldFocusListener(textField);
		textField.setUserData(this);

		createMenuItems();
	}

	@FXML public void onActionTextField() {
		try {

			final Value v = value.getCopy();
			v.setValue(textField.getText());

			sendValue(v);
			start();

		} catch (Exception e) {
			logger.catching(e);
		}
    }

	@FXML private void onActionMenuItems(ActionEvent e) {
		final MenuItem menuItem = (MenuItem) e.getSource();
		final String id = menuItem.getId();
		try {
			switch (id) {
			case START:
				start();
				break;
			case STOP:
				stop(false);
				break;
			case SET:
				onActionTextField();
				break;
			case RESET:
				reset();
				break;
			case SAVE:
				save();
			}
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	@FXML private void onKeyPressed(KeyEvent event) {

		if(value!=null && event.getCode()==KeyCode.ESCAPE)
			start();
    }

	public void save() throws PacketParsingException {

		final RegisterValue registerValue = new RegisterValue(setRegisterValue.getIndex(), setRegisterValue.getAddr()+3, 0);
		RegisterPacket packet = new RegisterPacket(registerValue);
		packet.addObserver(new Observer() {

			private int times = 3;

			@Override
			public void update(Observable observable, Object object) {
				SERVICES.execute(new Runnable() {

						@Override
						public void run() {
							if(observable instanceof LinkedPacket){
								try {

									RegisterPacket p = new RegisterPacket(((LinkedPacket)observable).getAnswer());

									if((p.getPacketHeader().getPacketType()!=PacketType.RESPONSE || p.getPacketHeader().getPacketErrors()!=PacketErrors.NO_ERROR) && --times>=0){

										logger.warn("Not posible to save register " + registerValue);
										Thread.sleep(20);
										SerialPortController.QUEUE.add(packet);

									}else
										packet.deleteObservers();

								} catch (Exception e) {
									logger.catching(e);
									packet.deleteObservers();
								}
							}
						}
					});
					}
			});
			SerialPortController.QUEUE.add(packet);
	}

	public void setKeyStartWith(String keyStartWith){
		logger.entry(keyStartWith);

		// Stop sending packets
		stop(false);

		if(keyStartWith==null || keyStartWith.isEmpty())
			return;

		this.propertyName = keyStartWith;


			try {
				setPacket(keyStartWith);
			} catch (Exception e) {
				logger.catching(e);
			}
			setValues(keyStartWith);

			setPeriod( Long.parseLong(IrtGuiProperties.getProperty(keyStartWith + "period")));

			startStopMenuItem.setDisable(false);
			saveMenuItem.setDisable(false);
			resetMenuItem.setDisable(false);
			setMenuItem.setDisable(false);

			//Select register menu
			final ObservableList<MenuItem> menuItems = menuRegister.getItems();

			menuItems
			.parallelStream()
			.filter(mi->mi.getId().equals(keyStartWith))
			.forEach(mi->Platform.runLater(()->((RadioMenuItem)mi).setSelected(true)));

			start();
	}

	private void setPacket(String keyStartWith) throws PacketParsingException {
		logger.entry(keyStartWith);

		removeAllPackets();

		int index = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "index"));
		int address = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "address"));

		setRegisterValue = new RegisterValue(index, address);
		addPacket(new RegisterPacket(setRegisterValue));

		RegisterValue rv = new RegisterValue(index, address==0 ? 0x10+2 : 0x10+3); //0x10+2 --> RDAC:MEM2; 0x10+3 --> RDAC:MEM3
		addPacket(new RegisterPacket(rv));
	}

	private void setValues(String keyStartWith) {
		int valueMin = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "value.min"));
		int valueMax = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "value.max"));

		value = new Value(null, valueMin, valueMax, 0);
		savedValue = new Value(null, valueMin, valueMax, 0);
	}

	private void createMenuItems() {

		EventHandler<ActionEvent> onActionRegisterSelect = e->{
			final MenuItem source = (MenuItem) e.getSource();
			setKeyStartWith(source.getId());
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, onActionRegisterSelect, menuRegister.getItems());
	}

	public void start(){

		Platform.runLater(()->{

			final StringProperty textProperty = textField.textProperty();
			textProperty.removeListener(numericChecker);
			textField.setText(value.toString());
			textProperty.addListener(numericChecker);

			textField.setEditable(true);
			startStopMenuItem.setText("Stop");
			startStopMenuItem.setId(STOP);
		});

		super.start();
	}

	public void stop(boolean leaveEditable){

		Platform.runLater(()->{
			textField.setEditable(leaveEditable);
			startStopMenuItem.setText("Start");
			startStopMenuItem.setId(START);
		});

		super.stop(leaveEditable);
	}

	private void compareValues() {

		if(value.equals(savedValue))
			Platform.runLater(()->textField.getStyleClass().remove(CLASS_NOT_SAVED));

		else
			Platform.runLater(()->{
				final ObservableList<String> styleClass = textField.getStyleClass();
				if(!styleClass.contains(CLASS_NOT_SAVED))
					styleClass.add(CLASS_NOT_SAVED);});

//		Platform.runLater(()->logger.error("{}; {}", value.equals(savedValue), textField.getStyleClass()));
	}

	private void checkForValueChange(String newValue) {
		Platform.runLater(()->{

			final ObservableList<String> styleClass = textField.getStyleClass();

			final Value copy = value.getCopy().setValue(newValue);
			logger.trace("value={}; newValue={}, equals={}", value, copy, value.equals(copy));

			if(value.equals(copy)){
				start();
				logger.debug("Start; newValue:{}", newValue);

			}else if(!styleClass.contains(CLASS_HAS_CHANGED)){
				styleClass.add(CLASS_HAS_CHANGED);
				stop(true);
				logger.debug("Stop");
			}
		});
	}
	 
	public void reset() throws PacketParsingException {
		sendValue(savedValue);
	}

	private void sendValue(final Value value) throws PacketParsingException {
		if (!value.isError() && value.getValue()!=null) {
			RegisterValue rValue = new RegisterValue(setRegisterValue.getIndex(), setRegisterValue.getAddr(), value.getValue().intValue());
			RegisterPacket packet  = new RegisterPacket(rValue);
			packet.addObserver(this);
			logger.trace(packet);
			SerialPortController.QUEUE.add(packet);
		}
	}

	@Override
	public void update(Observable observable, Object object) {
		SERVICES.execute(new Update(((LinkedPacket)observable).getAnswer()));
	}

	public static Class<? extends Node> getPootClass() {
		return TextField.class;
	}

	//********************************************** class Update   ***************************************************
	private class Update implements Runnable{

		private byte[] answer;

		Update(byte[] answer){
			this.answer = answer;
		}

		@Override
		public void run() {
			try {

				LinkedPacket packet = new RegisterPacket(answer);

				final PacketHeader packetHeader = packet.getPacketHeader();
				if(packetHeader.getPacketType()==PacketType.RESPONSE && packetHeader.getPacketErrors()==PacketErrors.NO_ERROR){

					Payload payload = packet.getPayloads().get(0);
					RegisterValue rv = new RegisterValue(payload.getInt(0), payload.getInt(1), payload.getInt(2));

					//if it is set value
					if(setRegisterValue.equals(rv)){

						setRegisterValue(rv, packet);

					}else if( savedValue.getValue()==null || savedValue.getValue().intValue()!=rv.getValue()){

						setTooltip(packet, rv);
					}

				}else
					logger.warn("\n\t This Packet has error: {}", packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}

		private void setTooltip(LinkedPacket packet, RegisterValue savedRegisterValue) {
			savedValue.setValue(savedRegisterValue.getValue());
			Platform.runLater(()->textField.setTooltip(new Tooltip(savedValue.toString())));

			logger.trace("\n\t packet:{}\n\t rv:{},\n\t savedValue:{}", packet, savedRegisterValue, savedValue);

			compareValues();
		}

		/**
		 * If value changed set textField text
		 * @param setRegisterValue	- sent value
		 * @param packet - received packet 
		 */
		private void setRegisterValue(RegisterValue setRegisterValue, LinkedPacket packet) {
			final ObservableList<String> styleClass = textField.getStyleClass();
			styleClass.remove(CLASS_HAS_CHANGED);

			if (scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled() || value.getValue()==null || value.getValue().intValue() != setRegisterValue.getValue()) {

				value.setValue(setRegisterValue.getValue());
				Platform.runLater(() -> {
					logger.entry();
					textField.setText(value.toString());
					logger.exit();
				});

				logger.trace("\n\t packet:{}\n\t rv:{},\n\t value:{}", packet, setRegisterValue, value);

				compareValues();
			}
		}
		
	}
}
