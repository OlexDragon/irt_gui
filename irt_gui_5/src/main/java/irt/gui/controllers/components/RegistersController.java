package irt.gui.controllers.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.TextFieldFocusListener;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.data.value.Value;
import irt.gui.data.value.Value.Status;
import irt.gui.data.value.ValueDouble;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;

public class RegistersController implements Observer {

	private static final String ACTIVE 		= "active";

	public static final String REGISTER_PROPERTIES 		= "gui.register.controller.";
	private static final String REGISTER_PROPERTY_NAME 		= REGISTER_PROPERTIES 		+ "name.";
	private static final String REGISTER_PROPERTY_NAME_ID 	= REGISTER_PROPERTY_NAME 	+ "%d";
	private static final String REGISTER_ROW		 		= REGISTER_PROPERTIES 		+ "row.";
	private static final String REGISTER_ROW_ID		 		= REGISTER_ROW 				+ "%d";
	private static final String REGISTER_COLUMN		 		= REGISTER_PROPERTIES 		+ "column.";
	private static final String REGISTER_COLUMN_ID			= REGISTER_COLUMN	 		+ "%d";
	private static final String REGISTER_BACKGROUND	 		= REGISTER_PROPERTIES 		+ "background.";
	private static final String REGISTER_BACKGROUND_ID	 	= REGISTER_BACKGROUND 		+ "%d";
	private static final String REGISTER_ALIGNMENT_ID 		= REGISTER_PROPERTIES 		+ "alignment.%d.";
	private static final String REGISTER_ALIGNMENT	 		= REGISTER_ALIGNMENT_ID 	+ "%d.%d";			//gui.regicter.controller.alignment.profikeId.column.row (ex. gui.regicter.controller.alignment.3.5.7)

	private final Logger logger = LogManager.getLogger();

	@FXML private Slider 		slider;
	@FXML private Button		buttonInitialize;
    @FXML private Button 		buttonCalibMode;
    @FXML private Button 		resetButton;
    @FXML private CheckBox 		stepCheckBox;
    @FXML private TextField 	stepTextField;
    @FXML private RegisterPanel registersPanelController;

    @FXML private ComboBoxUnitAddress 	comboBoxUnitAddressController;

    @FXML private MenuItem 		menuSave;
    @FXML private Menu			menuProfile;

    @FXML private ButtonCalibrationMode buttonCalibModeController;

    private NumericChecker 		stepNumericChecker;
    private TextField 			selectedTextField;
    private TextInputDialog 	dialog 				= new TextInputDialog("default");
	private ToggleGroup 		profilesToggleGroup = new ToggleGroup();

	private int 	profileId;
	private Boolean editable;

    public RegistersController(){
    	dialog.setTitle("Save provile as...");
    	dialog.setContentText("Type the profile name");
    	dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, e->{
    		Optional
    		.ofNullable(dialog.getEditor().getText())
    		.filter(text->!text.matches("^(?=\\s*\\S).*$"))	//text does not have any characters
    		.ifPresent(t->e.consume());						//do-nothing
     	});

    }

	private final Observer valueObserver = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			logger.entry("{} : {}", o, arg);

			Value v = (Value) o;
			if(((Status)arg)==Status.IN_RANGE){
				if(o instanceof ValueDouble){

					double rv = (double)v.getValue()/v.getFactor();
					if(Double.compare(slider.getValue() ,rv)!=0)
						setSliderValue(rv);

				}else{

					//TODO check max double value
					int rv = v.getValue().intValue();
					if(Double.compare(slider.getValue(), rv)!=0)
						setSliderValue(rv);

				}
			}else
				logger.warn("The value {} is out of range", v.getOriginalValue());
		}

		public void setSliderValue(double rv) {
			logger.entry(rv);
			Platform.runLater(()->{

				final DoubleProperty valueProperty = slider.valueProperty();
				valueProperty.removeListener(sliderValueChangeListener);
				slider.setValue(rv);
				valueProperty.addListener(sliderValueChangeListener);
			});
		}
	};

	private void removeCssClassAndDeleteObserver(String cssClass) {
		Optional
		.ofNullable(selectedTextField)
		.ifPresent(textField->{

			TextFieldAbstract textFieldRegister = (TextFieldAbstract) selectedTextField.getUserData();
			Value registerValue = textFieldRegister.getValue();
			registerValue.deleteObserver(valueObserver);

			Platform.runLater(()->textField.getStyleClass().remove(cssClass));
		});
	}

	@SuppressWarnings("unchecked")
	private final EventHandler<ActionEvent> onActionMenuSelectProfile = e->{
		try{

			RadioMenuItem rmi = (RadioMenuItem)e.getSource();
			profileId = Integer.parseInt(((String) rmi.getUserData()));// get profile ID
			setRowsAndColumns(profileId);
			setNodesOf( profileId, TextFieldRegister.class, LabelValue.class, LabelRegister.class);
			menuSave.setDisable(false);
			registersPanelController.setBackground(IrtGuiProperties.getProperty(String.format(REGISTER_BACKGROUND_ID, profileId)));
			setAlignment(profileId);
			slider.toFront();

		}catch(Exception ex){
			logger.catching(ex);
		}
	};

    private final ChangeListener<Boolean> stepTextFieldFocusListener = (observable, oldValue, newValue)->{
		if(newValue){
			stepCheckBox.setSelected(true);
			stepCheckBoxAction();
		}
	};

	private final ChangeListener<Number> sliderValueChangeListener = (observable, oldValue, newValue)->{
		Platform.runLater(
				()->Optional
				.ofNullable(selectedTextField)
				.map(stf->(TextFieldAbstract)stf.getUserData())
				.ifPresent(controller->controller.setText(slider.getValue())));
	};

	private final ChangeListener<Boolean> registerPanelFocusListener = (observable, oldValue, newValue)->{

		final TextField bean = (TextField) ((ReadOnlyBooleanProperty)observable).getBean();
		Optional
		.ofNullable(editable)
		.filter(e->e==true)
		.filter(e->selectedTextField!=bean)
		.ifPresent(e->{

			removeCssClassAndDeleteObserver(ACTIVE);

			selectedTextField = bean;

			TextFieldAbstract textFieldcontroller = (TextFieldAbstract) selectedTextField.getUserData();

			//Set slider value, max, min
			textFieldcontroller.setSliderValue(slider, sliderValueChangeListener, ACTIVE, valueObserver, stepNumericChecker);

			final boolean disable = selectedTextField.isDisable();
			if(slider.isDisable()!=disable)
				Platform.runLater(()->slider.setDisable(disable));

		});
	};

    @FXML private void initialize(){
 
    	buttonCalibModeController.addObserver(this);
    	buttonCalibModeController.addObserver((Observer) buttonInitialize.getUserData());

    	stepNumericChecker = new NumericChecker(stepTextField.textProperty());

    	new TextFieldFocusListener(stepTextField);
    	stepTextField.focusedProperty().addListener(stepTextFieldFocusListener);

		registersPanelController.setFocusListener(registerPanelFocusListener);

		slider.valueProperty().addListener(sliderValueChangeListener);

		createProfileMenuItems();

		comboBoxUnitAddressController.addObserver(new AddressUpdater());
    }

	@FXML private void onActionNewProfile(){
		registersPanelController.setColumnsAndRows(1, 1);
		menuSave.setDisable(true);
	}

	@FXML private void onActionResetButton(ActionEvent event) {
    	try {
			registersPanelController.reset();
		} catch (Exception e) {
			logger.catching(e);
		}
    }

    @FXML private void saveValues(ActionEvent event) {
    	try {
			registersPanelController.save();
		} catch (Exception e) {
			logger.catching(e);
		}
    }

    @FXML private void onMouseReleasedSlider() {
    	Optional
    	.ofNullable(selectedTextField)
    	.ifPresent(s->{
    		Platform.runLater(()->{
    			TextFieldAbstract controller = (TextFieldAbstract) s.getUserData();
    			controller.onActionTextField();
    		});
    	});
    }

    @FXML private void stepCheckBoxAction() {
    	slider.setSnapToTicks(stepCheckBox.isSelected());
    }

    @FXML private void steponActionTextField(ActionEvent event) {

    	Optional
    	.ofNullable(selectedTextField)
    	.ifPresent(TextField::requestFocus);

    	Optional
    	.ofNullable(stepTextField.getText())
    	.filter(text->!text.isEmpty())
    	.ifPresent(text->slider.setMinorTickCount(Integer.parseInt(text)));
    }

    @FXML private void onActionMenuItemsSaveAs(){
    	Optional<String> result = dialog.showAndWait();
    	result.ifPresent(name->{
    		try {
				saveNewProfile(name);
			} catch (Exception e) {
				logger.catching(e);
			}
    	});
    }

	@FXML private void onActionMenuItemsSave(){
		dialog.getEditor().setText(IrtGuiProperties.getProperty(String.format(REGISTER_PROPERTY_NAME_ID, profileId)));
    	Optional<String> result = dialog.showAndWait();
    	result.ifPresent(name->{
    		try {
				saveProfile(name);
			} catch (Exception e) {
				logger.catching(e);
			}
    	});
    }

	@Override public void update(Observable o, Object arg) {
		Optional
		.ofNullable(arg)
		.filter(CalibrationMode.class::isInstance)
		.map(a->arg==CalibrationMode.ON)
		.filter(b->editable!=b)
		.ifPresent(b->{
			editable = b;
			Platform.runLater(()->{

				registersPanelController.setEditable(editable);

				if(!editable)
					slider.setDisable(true);

				if(selectedTextField != null){
					selectedTextField.getStyleClass().remove(ACTIVE);
					selectedTextField = null;
				}
			});
		});
	}

	private void saveNewProfile(String name) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, FileNotFoundException, IOException {
    	final Properties propertiesFromFile = getPropertiesFromFile();
		final Properties properties = IrtGuiProperties.selectFromProperties(REGISTER_PROPERTIES);
    	int max = getNewProfileID(properties);
    	updateProperties(propertiesFromFile, max, name);

    	try(final FileOutputStream outputStream = new FileOutputStream(new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName()));){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    		IrtGuiProperties.updateProperties(propertiesFromFile);
    	}
    }

    private void saveProfile(String profileName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, FileNotFoundException, IOException{
    	final Properties propertiesFromFile = getPropertiesFromFile();
    	updateProperties(propertiesFromFile, profileId, profileName);

    	logger.trace(propertiesFromFile);
    	try(final FileOutputStream outputStream = new FileOutputStream(new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName()));){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    	}
	}

	private void updateProperties(Properties propertiesFromFile, int profileId, String profileName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		propertiesFromFile.put(String.format(REGISTER_PROPERTY_NAME_ID	, profileId), profileName);
		propertiesFromFile.put(String.format(REGISTER_ROW_ID			, profileId), Integer.toString(registersPanelController.getRowCount()));
		propertiesFromFile.put(String.format(REGISTER_COLUMN_ID			, profileId), Integer.toString(registersPanelController.getColumnCount()));

		//TextFields properties
		removeProperties(propertiesFromFile, String.format( TextFieldRegister.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, TextFieldRegister.class);

		//remove LabelValue properties
		removeProperties(propertiesFromFile, String.format( LabelValue.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, LabelValue.class);

		//remove LabelRegister properties
		removeProperties(propertiesFromFile, String.format( LabelRegister.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, LabelRegister.class);

		final String backgroundPath = registersPanelController.getBackgroundPath();
		if(backgroundPath!=null)
			propertiesFromFile.put(String.format(REGISTER_BACKGROUND_ID, profileId), backgroundPath);

		//put TextFields alignment properties
		registersPanelController
		.getVBoxesAlignmentProperties()
		.parallelStream()
		.forEach(align->{
			final String format = String.format(REGISTER_ALIGNMENT, profileId, align.get("column"), align.get("row"));
			final Object pos = align.get("pos");
			if(pos==Pos.CENTER)
				propertiesFromFile.remove(format);
			else
				propertiesFromFile.put( format, ((Pos)pos).name());
		});
	}

	private void putProperies(Properties propertiesFromFile, int profileId, Class<? extends ScheduledNodeAbstract> nodeClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		logger.entry( propertiesFromFile, profileId, nodeClass);

		List<Map<String, Object>> textFieldsProperties = registersPanelController.getFieldsProperties(nodeClass);

		textFieldsProperties
		.parallelStream()
		.forEach(tfp->{
			putProperties(propertiesFromFile, profileId, nodeClass, tfp);
		});
	}

	private void putProperties(Properties propertiesFromFile, int profileId, Class<? extends ScheduledNodeAbstract> nodeClass, Map<String, Object> tfp) {
		try {

			Field field = nodeClass.getField("FIELD_KEY");
			final String key = (String)field.get(null);

			final String format = String.format(key, profileId, tfp.get("column"), tfp.get("row"));
			propertiesFromFile.put( format, tfp.get("name"));

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void removeProperties(Properties propertiesFromFile, String keyId) {
		IrtGuiProperties
		.selectFromProperties(propertiesFromFile, keyId)
		.entrySet()
		.parallelStream()
		.forEach(e->propertiesFromFile.remove(e.getKey()));
	}

	private int getNewProfileID(final Properties properties) {

		int max = properties
    			.entrySet()
    			.parallelStream()
    			.map(e->(String)e.getKey())
    			.filter(s->s.startsWith(REGISTER_PROPERTY_NAME))
    			.map(e->e.replace(REGISTER_PROPERTY_NAME, ""))
     			.mapToInt(Integer::parseInt)
    			.max()
    			.orElse(0);
		return ++max;
	}

	private Properties getPropertiesFromFile() throws IOException, FileNotFoundException {

		final File file = new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName());
 		Properties properties = new Properties();

 		if(file.exists()) {
			try(final FileInputStream inputStream = new FileInputStream(file);){
				properties.load(inputStream);
			}
		}
 		return properties;
	}

	private void createProfileMenuItems() {

		final Properties selectFromProperties = IrtGuiProperties.selectFromProperties(REGISTER_PROPERTY_NAME);
		final List<RadioMenuItem> menuItems = selectFromProperties
				.entrySet()
				.parallelStream()
				.map(p->{
					final RadioMenuItem radioMenuItem = new RadioMenuItem((String)p.getValue());
					radioMenuItem.setUserData(((String)p.getKey()).replace(REGISTER_PROPERTY_NAME, ""));
					Platform.runLater(()->{
						radioMenuItem.setOnAction(onActionMenuSelectProfile);
						radioMenuItem.setToggleGroup(profilesToggleGroup);
					});
					return radioMenuItem;
				})
				.collect(Collectors.toList());
		final ObservableList<MenuItem> items = menuProfile.getItems();
		((RadioMenuItem)items.get(0)).setToggleGroup(profilesToggleGroup);
		items.addAll(menuItems);
	}

	private void setAlignment(int profileId) {
		String key = String.format(REGISTER_ALIGNMENT_ID, profileId);
		Optional
		.ofNullable(IrtGuiProperties.selectFromProperties(key))
		.ifPresent(properties->properties.entrySet()
					.parallelStream()
					.map(e->{
						String[] split = ((String)e.getKey()).replace(key, "").split("\\.");
						Map<String, String>map = new HashMap<>();
						map.put("pos", (String) e.getValue());
						map.put("column", split[0]);
						map.put("row", split[1]);
						return map;
					})
					.forEach(map->registersPanelController.setAlignment(Pos.valueOf(map.get("pos")), Integer.parseInt(map.get("column")), Integer.parseInt(map.get("row"))))
				);
	}

	private void setRowsAndColumns(int profileId) {
		Optional
		.ofNullable(IrtGuiProperties.getProperty(String.format(REGISTER_ROW_ID, profileId)))				// Get Rows count
		.filter(Objects::nonNull)
		.map(Integer::parseInt)
		.ifPresent(rows->Optional
				.ofNullable(IrtGuiProperties.getProperty(String.format(REGISTER_COLUMN_ID, profileId)))		// Get Columns count
				.filter(Objects::nonNull)
				.map(Integer::parseInt)
				.ifPresent(columns->registersPanelController.setColumnsAndRows( columns, rows)));
	}

	@SuppressWarnings("unchecked")
	private void setNodesOf( int profileId, Class<? extends ScheduledNodeAbstract>... fieldClass) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for(Class<? extends ScheduledNodeAbstract> c:fieldClass)
			setNodesOf(c, profileId);
	}

	private void setNodesOf(Class<? extends ScheduledNodeAbstract> fieldClass, int profileId) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		final Field field = fieldClass.getField("FIELD_KEY_ID");
		String key = String.format((String)field.get(null), profileId);

		Optional
		.ofNullable(IrtGuiProperties.selectFromProperties(key))
		.ifPresent(property->property.entrySet()
					.parallelStream()
					.map(e->{
						String[] split = ((String)e.getKey()).replace(key, "").split("\\.");

						Map<String, String>map = new HashMap<>();
						map.put("key", (String) e.getValue());
						map.put("column", split[0]);
						map.put("row", split[1]);
						return map;
					})
					.forEach(map->setNode(fieldClass, map))
				);
	}

	private void setNode(Class<? extends ScheduledNodeAbstract> fieldClass, Map<String, String> map){

		try {
			final Node node = registersPanelController.setNode(fieldClass, map.get("key"), Integer.parseInt(map.get("column")), Integer.parseInt(map.get("row")));

			if(editable!= null && node instanceof TextField)
				((TextField)node).setEditable(editable);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public class AddressUpdater implements Observer {

		@Override
		public void update(Observable o, Object unitAddress) {
			
			final byte byteValue = ((Integer)unitAddress).byteValue();

			registersPanelController.setUnitAddress(byteValue);
			buttonCalibModeController.setUnitAddress(byteValue);
		}

	}
}
