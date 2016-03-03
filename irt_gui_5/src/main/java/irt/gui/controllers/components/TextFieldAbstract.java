
package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Observer;

import irt.gui.IrtGuiProperties;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.TextFieldFocusListener;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.value.Value;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class TextFieldAbstract extends ScheduledNodeAbstract {

	public static final String START 				= "start";
	public static final String STOP 				= "stop";
	public static final String NAME 				= "name";
	public static final String CLASS 				= "class";
	public static final String PREFIX 				= "prefix";
	public static final String PRECISION 			= "precision";
	public static final String INDEX 				= "index";
	public static final String ADDRESS 				= "address";
//	public static final String SET 					= "set";
//	public static final String SAVE 				= "save";
//	public static final String RESET 				= "reset";
	public static final String CLASS_NOT_SAVED 		= "notSaved";
	public static final String CLASS_HAS_CHANGED 	= "hasChanged";

	protected volatile Value	value;		/* Actual value	*/							public 			Value 					getValue() 			{ return value; }
	private final ChangeListener<String> 	numericChecker 	= getNumericChecker();		public abstract ChangeListener<String> 	getNumericChecker();
	private Menu menu;

	protected abstract void setup();
	protected abstract Menu getMenu();
	protected abstract Node getRootNode();
	protected abstract void createMenuItems();
	protected abstract void sendValue(Value value) 																	throws PacketParsingException;
	protected abstract void setPacket(String keyStartWith) 															throws PacketParsingException ;
	public 	  abstract void save() 																					throws PacketParsingException;
	public 	  abstract void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, String cssClass, Observer valueObserver, NumericChecker stepNumericChecker);
	public 	  abstract void setText(double value);

	@FXML protected TextField textField; 		public TextField getTextField() { return textField; }

	@FXML protected MenuItem	menuItemStartStop;
	@FXML protected MenuItem	menuItemSave;
	@FXML protected MenuItem	menuItemReset;
	@FXML protected MenuItem	menuItemSet;

	@FXML public void initialize(){

		menu = getMenu();

		final StringProperty textProperty = textField.textProperty();
		textProperty.addListener(numericChecker);
		textProperty.addListener(( ob, oldV, newV)->checkForValueChange(newV));

		new TextFieldFocusListener(textField);
		getRootNode().setUserData(this);
		textField.setUserData(this);
		createMenuItems();
		setup();
	}

	@FXML protected void onActionTextField() {

		try {

			final Value v = value.getCopy();
			v.setValue(textField.getText());

			if(!value.equals(v)){
				sendValue(v);
			}

			start();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@FXML protected void onActionMenuItemSave(){
		try {
			save();
		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}

	@FXML void onActionMenuItemStartStop(ActionEvent event){
		final MenuItem menuItem = (MenuItem) event.getSource();
		final String id = menuItem.getId();
		try {
			switch (id) {
			case START:
				start();
				break;
			case STOP:
				stop(false);
				break;
			}
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	@FXML private void onKeyPressed(KeyEvent event) {

		if(value!=null && event.getCode()==KeyCode.ESCAPE)
			start();
    }

	public void setKeyStartWith(String propertiesKeyStartWith){
		logger.entry(propertiesKeyStartWith);

		// Stop sending packets
		stop(false);

		if(propertiesKeyStartWith==null || propertiesKeyStartWith.isEmpty())
			return;

		try {

			this.propertyName = propertiesKeyStartWith;


			setPacket(propertiesKeyStartWith);
			setValues(propertiesKeyStartWith);

			setPeriod( Long.parseLong(IrtGuiProperties.getProperty(propertiesKeyStartWith + "period")));

			menuItemStartStop.setDisable(false);
			menuItemSave.setDisable(false);
			menuItemReset.setDisable(false);
			menuItemSet.setDisable(false);

			//Select register menu
			final ObservableList<MenuItem> menuItems = menu.getItems();

			//select menu
			menuItems
			.parallelStream()
			.filter(mi->mi.getId().equals(propertiesKeyStartWith))
			.forEach(mi->Platform.runLater(()->((RadioMenuItem)mi).setSelected(true)));

			start();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	protected Value setValues(String keyStartWith) {
		int valueMin = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "value.min"));
		int valueMax = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "value.max"));

		value = new Value(null, valueMin, valueMax, 0);
		return value;
	}

	@Override public void start(){
//		logger.error("Start");

		Platform.runLater(()->{

			if(value!=null){
				final StringProperty textProperty = textField.textProperty();
				textProperty.removeListener(numericChecker);
				textField.setText(value.toString());
				textProperty.addListener(numericChecker);
			}

			if(textField.isEditable()){
				menuItemStartStop.setText("Stop");
				menuItemStartStop.setId(STOP);
			}
		});

		super.start();
	}

	public void stop(boolean leaveEditable){
		logger.entry(leaveEditable);

		super.stop(leaveEditable);

		Platform.runLater(()->{
			textField.setEditable(leaveEditable);
			menuItemStartStop.setText("Start");
			menuItemStartStop.setId(START);
		});
	}

	private void checkForValueChange(String newValue) {

		final Value copy = value.getCopy().setValue(newValue);

		Platform.runLater(()->{

			final ObservableList<String> styleClass = textField.getStyleClass();

			//If Integer or double not end by '.' and new value equals set value
			if((value.getClass()==Value.class || newValue.charAt(newValue.length()-1)!='.') && value.equals(copy)){

				
				if(styleClass.size()>0)
					styleClass.remove(CLASS_HAS_CHANGED);	// if size = 0 throw  java.lang.ArrayIndexOutOfBoundsException

				if(scheduleAtFixedRate.isCancelled())
					start();
				

			}else if(!styleClass.contains(CLASS_HAS_CHANGED)){
				styleClass.add(CLASS_HAS_CHANGED);
				stop(true);
//				logger.error("Stop");
			}
		});
	}

	public void addFocusListener(ChangeListener<Boolean> focusListener) {
		textField.focusedProperty().addListener(focusListener);
	}

	protected void setText(String text, ChangeListener<String> fractionalNumberChecker){

		if(text.equals(textField.getText()))
			return;

		Platform.runLater(()->{

			final StringProperty textProperty = textField.textProperty();
			textProperty.removeListener(fractionalNumberChecker);
			textField.setText(text);
			textProperty.addListener(fractionalNumberChecker);
		});
	}

	protected void setSliderValue(Slider slider, ChangeListener<Number> sliderChangeListener, double value, double min, double max){

		Platform.runLater(()->{
			final DoubleProperty valueProperty = slider.valueProperty();
			valueProperty.removeListener(sliderChangeListener);
			setSliderMin(slider, min);
			setSliderMax(slider, max);
			setSliderValue(slider, value);
			valueProperty.addListener(sliderChangeListener);
		});
	}
	public void setSliderValue(Slider slider, double value) {
		if(Double.compare(slider.getValue(), value)!=0)
			slider.setValue(value);
	}
	public void setSliderMax(Slider slider, double max) {
		if(Double.compare(slider.getMax(), max)!=0)
			slider.setMax(max);
	}
	public void setSliderMin(Slider slider, double min) {
		if(Double.compare(slider.getMin(), min)!=0)
			slider.setMin(min);
	}

	@Override protected void addPacket(LinkedPacket packet) {
		super.addPacket(packet);
		packet.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				Platform.runLater(new Runnable() {
					
					private static final String ERROR = "error";

					@Override
					public void run() {
						final ObservableList<String> styleClass = textField.getStyleClass();

						if(packet.getAnswer()==null){
							if(!styleClass.contains(ERROR))
								styleClass.add(ERROR);

						}else if(styleClass.size()>0)	// if size == 0 throw  java.lang.ArrayIndexOutOfBoundsException
							styleClass.remove(ERROR);

					}
				});
			}
		});
	}
}
