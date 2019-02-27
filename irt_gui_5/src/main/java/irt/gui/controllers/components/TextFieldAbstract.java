
package irt.gui.controllers.components;

import java.util.Optional;
import java.util.function.Consumer;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.interfaces.SliderListener;
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
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class TextFieldAbstract extends ScheduledNodeAbstract implements SliderListener {

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

	protected volatile Value	value;		/* Actual value	*/							public Value getValue() { return value; }
	private final ChangeListener<String> 	numericChecker 	= getNumericChecker();		public abstract ChangeListener<String> 	getNumericChecker();
	private Menu menu;
	private TextFieldErrorController errorController;

	protected abstract void setup();
	protected abstract Menu getMenu();
	protected abstract Node getRootNode();
	protected abstract void createMenuItems();
	protected abstract void sendValue(Value value) 																	throws PacketParsingException;
	protected abstract void setPacket(String keyStartWith) 															throws PacketParsingException ;
	public 	  abstract void save() 																					throws PacketParsingException;

	private String step = "1";
	public String getStep() { return step; }
	public String setStep(String step){
		this.step = step;
		return this.step;
	};

	@FXML protected TextField textField; 		public TextField getTextField() { return textField; }

	@FXML protected MenuItem	menuItemStartStop;
	@FXML protected MenuItem	menuItemSave;
	@FXML protected MenuItem	menuItemReset;
	@FXML protected MenuItem	menuItemSet;

	@FXML protected void initialize(){

		menu = getMenu();

		errorController = new TextFieldErrorController(textField);

		final StringProperty textProperty = textField.textProperty();
		textProperty.addListener(numericChecker);
		textProperty.addListener(( ob, oldV, newV)->checkForValueChange(newV));

		new TextFieldFocusListener(textField);
		getRootNode().setUserData(this);
		textField.setUserData(this);
		createMenuItems();
		setup();
	}

	@FXML public void onActionTextField() {

		if(value!=null)
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

	private Consumer<KeyEvent> onKeyPressed;
										public Consumer<KeyEvent> getOnKeyPressed() {
											return onKeyPressed;
										}
										public void setOnKeyPressed(Consumer<KeyEvent> onKeyPressed) {
											this.onKeyPressed = onKeyPressed;
										}

	@FXML protected void onKeyPressed(KeyEvent event) {

		Optional.ofNullable(onKeyPressed).ifPresent(onKey->onKey.accept(event));

		if(value!=null && event.getCode()==KeyCode.ESCAPE)
			start();
    }

	public void setKeyStartWith(String propertiesKeyStartWith){

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

	@Override public boolean start(){
//		logger.error(this.getClass().getSimpleName());

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

		return super.start();
	}

	public void stop(boolean leaveEditable){
//		logger.error("{} : {}", this.getClass().getSimpleName(), leaveEditable);

		super.stop(leaveEditable);

		Platform.runLater(()->{
			textField.setEditable(leaveEditable);
			menuItemStartStop.setText("Start");
			menuItemStartStop.setId(START);
		});
	}

	private void checkForValueChange(String newValue) {
		if(value==null)
			return;

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
//		logger.error(text);

		if(text.equals(textField.getText()))
			return;

		Platform.runLater(()->{

			final StringProperty textProperty = textField.textProperty();
			textProperty.removeListener(fractionalNumberChecker);
			textField.setText(text);
			textField.setTooltip(new Tooltip(text));
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
		if(Double.compare(slider.getMax(), max)!=0){

			final DoubleProperty majorTickUnitProperty = slider.majorTickUnitProperty();

			final double range = max - slider.getMin();

			final double minMajor = range/1000;//	2000 is the Maximum major ticks
			final double major = majorTickUnitProperty.get();

			if(Double.compare(minMajor, major)>0)
				majorTickUnitProperty.set(Math.ceil(minMajor));

//			final IntegerProperty minorTickCountProperty = slider.minorTickCountProperty();
//
//			final double minMinor = 10000;//	10000 is the Maximum major ticks
//			final int minor = minorTickCountProperty.get();
//			logger.error("minMinor = {}; minor = {}", minMinor, minor);
//
//			if(Double.compare(minMinor, minor)>0)
//				minorTickCountProperty.set((int) Math.ceil(minMinor));

			slider.setMax(max);
		}
	}

	public void setSliderMin(Slider slider, double min) {
		if(Double.compare(slider.getMin(), min)!=0)
			slider.setMin(min);
	}

	@Override protected void addPacket(LinkedPacket packet) {
		super.addPacket(packet);
		// if no answer add 'error' ( css class )
		packet.addObserver(errorController);
	}
}
