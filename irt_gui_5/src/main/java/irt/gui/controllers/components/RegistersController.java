package irt.gui.controllers.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

	private static final String REGISTER_PROPERTIES 		= "gui.regicter.controller.";
	private static final String REGISTER_PROPERTY_NAME 		= REGISTER_PROPERTIES 		+ "name.";
	private static final String REGISTER_PROPERTY_NAME_ID 	= REGISTER_PROPERTY_NAME 	+ "%d";
	private static final String REGISTER_ROW		 		= REGISTER_PROPERTIES 		+ "row.";
	private static final String REGISTER_ROW_ID		 		= REGISTER_ROW 				+ "%d";
	private static final String REGISTER_COLUMN		 		= REGISTER_PROPERTIES 		+ "column.";
	private static final String REGISTER_COLUMN_ID			= REGISTER_COLUMN	 		+ "%d";
	private static final String REGISTER_BACKGROUND	 		= REGISTER_PROPERTIES 		+ "background.";
	private static final String REGISTER_BACKGROUND_ID	 	= REGISTER_BACKGROUND 		+ "%d";
	private static final String REGISTER_TEXT_FIELD_ID 		= REGISTER_PROPERTIES 		+ "textField.%d.";
	private static final String REGISTER_TEXT_FIELD	 		= REGISTER_TEXT_FIELD_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profikeId.column.row (ex. gui.regicter.controller.textField.3.5.7)
	private static final String REGISTER_ALIGNMENT_ID 		= REGISTER_PROPERTIES 		+ "alignment.%d.";
	private static final String REGISTER_ALIGNMENT	 		= REGISTER_ALIGNMENT_ID 	+ "%d.%d";			//gui.regicter.controller.alignment.profikeId.column.row (ex. gui.regicter.controller.alignment.3.5.7)

	private final Logger logger = LogManager.getLogger();

	@FXML private Slider 		slider;
    @FXML private Button 		calibModeButton;
    @FXML private Button 		saveButton;
    @FXML private Button 		resetButton;
    @FXML private CheckBox 		stepCheckBox;
    @FXML private TextField 	stepTextField;
    @FXML private RegisterPanel registersPanelController;

    @FXML private MenuItem 		saveMenu;
    @FXML private Menu			profileMenu;

    private NumericChecker 		stepNumericChecker;
    private TextField 			selectedTextField;
    private TextInputDialog 	dialog = new TextInputDialog("default");
	private ToggleGroup profilesToggleGroup = new ToggleGroup();

	private int profileId;

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
			if(((Status)arg)==Status.IN_RANGE){
				Value v = (Value) o;
				slider.setValue(v.getRelativeValue());
			}
		}
	};
   private final ChangeListener<Boolean> registerPanelFocusListener = (observable, oldValue, newValue)->{

	   final TextField bean = (TextField) ((ReadOnlyBooleanProperty)observable).getBean();

	   if(newValue && selectedTextField!=bean){

			Optional
			.ofNullable(selectedTextField)
			.ifPresent(textField->{
				Value registerValue = ((RegisterTextField) selectedTextField.getUserData()).getValue();
				registerValue.deleteObserver(valueObserver);
				textField.getStyleClass().remove(ACTIVE);
			});

			selectedTextField = bean;
			final ObservableList<String> styleClass = selectedTextField.getStyleClass();
			if(!styleClass.contains(ACTIVE))
				styleClass.add(ACTIVE);

			Value registerValue = ((RegisterTextField) selectedTextField.getUserData()).getValue();

			final long max = registerValue.getRelativeMaxValue();
			//set limit for text field
			stepNumericChecker.setMaximum(max);

			//set slider values
			slider.setMin(registerValue.getRelativeMinValue());
			slider.setMax(max);
			slider.setDisable(false);
			slider.setValue(registerValue.getRelativeValue());

			registerValue.addObserver(valueObserver);
		}
	};
	final EventHandler<ActionEvent> selectProfileMenuListener = e->{
		RadioMenuItem rmi = (RadioMenuItem)e.getSource();
		profileId = Integer.parseInt(((String) rmi.getUserData()));
		setRows(profileId);
		setColumns(profileId);
		setTextFields(profileId);
		saveMenu.setDisable(false);
		registersPanelController.setBackground(IrtGuiProperties.getProperty(String.format(REGISTER_BACKGROUND_ID, profileId)));
		setAlignment(profileId);
		//TODO menu action
	};

    private final ChangeListener<Boolean> stepTextFieldFocusListener = (observable, oldValue, newValue)->{
		if(newValue){
			stepCheckBox.setSelected(true);
			stepCheckBoxAction();
		}
	};
	final ChangeListener<Number> sliderChangeListener = (observable, oldValue, newValue)->{
		Platform.runLater(
				()->Optional.ofNullable(selectedTextField)
				.ifPresent(sf->sf.setText(Integer.toString(((int)slider.getValue())))));
	};

    @FXML private void initialize(){
    	((CalibrationModeButton)calibModeButton.getUserData()).addObserver(this);

    	stepNumericChecker = new NumericChecker(stepTextField.textProperty());

    	new TextFieldFocusListener(stepTextField);
    	stepTextField.focusedProperty().addListener(stepTextFieldFocusListener);

		registersPanelController.setFocusListener(registerPanelFocusListener);

		slider.valueProperty().addListener(sliderChangeListener);

		createProfileMenuItems();
    }

	@FXML private void resetValues(ActionEvent event) {
    	registersPanelController.reset();
    }

    @FXML private void saveValues(ActionEvent event) {
    	registersPanelController.save();
    }

    @FXML private void sliderMouseReleased() {
    	Optional
    	.ofNullable(selectedTextField)
    	.ifPresent(s->{
    		Platform.runLater(()->{
    			RegisterTextField controller = (RegisterTextField) s.getUserData();
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

	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(()->{
			final boolean disable = arg==CalibrationMode.OFF;
			registersPanelController.setDisable(disable);
			if(disable) slider.setDisable(true);
			selectedTextField = null;
		});
	}

    private void saveNewProfile(String name) throws FileNotFoundException, IOException {
    	final Properties propertiesFromFile = getPropertiesFromFile();
		final Properties properties = IrtGuiProperties.selectFromProperties(REGISTER_PROPERTIES);
    	int max = getNewProfileID(properties);
    	updateProperties(propertiesFromFile, max, name);

    	try(final FileOutputStream outputStream = new FileOutputStream(new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName()));){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    		IrtGuiProperties.updateProperties(propertiesFromFile);
    	}
    }

    private void saveProfile(String name) throws FileNotFoundException, IOException {
    	final Properties propertiesFromFile = getPropertiesFromFile();
    	updateProperties(propertiesFromFile, profileId, name);

    	logger.trace(propertiesFromFile);
    	try(final FileOutputStream outputStream = new FileOutputStream(new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName()));){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    	}
	}

	private void updateProperties(Properties propertiesFromFile, int profileId, String profileName) {

		propertiesFromFile.put(String.format(REGISTER_PROPERTY_NAME_ID	, profileId), profileName);
		propertiesFromFile.put(String.format(REGISTER_ROW_ID			, profileId), Integer.toString(registersPanelController.getRowCount()));
		propertiesFromFile.put(String.format(REGISTER_COLUMN_ID			, profileId), Integer.toString(registersPanelController.getColumnCount()));

		//remove TextFields properties
		IrtGuiProperties
		.selectFromProperties(propertiesFromFile, String.format(REGISTER_TEXT_FIELD_ID, profileId))
		.entrySet()
		.parallelStream()
		.forEach(e->propertiesFromFile.remove(e.getKey()));

		//put TextFields properties
		registersPanelController
		.getTextFieldsProperties()
		.parallelStream()
		.forEach(tfp->{
			final String format = String.format(REGISTER_TEXT_FIELD, profileId, tfp.get("column"), tfp.get("row"));
			propertiesFromFile.put( format, tfp.get("name"));
		});

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
		//TODO update profile
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
						radioMenuItem.setOnAction(selectProfileMenuListener);
						radioMenuItem.setToggleGroup(profilesToggleGroup);
					});
					return radioMenuItem;
				})
				.collect(Collectors.toList());
		profileMenu.getItems().addAll(menuItems);
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

	private void setRows(int profileId) {
		Optional
		.ofNullable(IrtGuiProperties.getProperty(String.format(REGISTER_ROW_ID, profileId)))
		.filter(Objects::nonNull)
		.map(Integer::parseInt)
		.ifPresent(rows->registersPanelController.setRows(rows));
	}

	private void setColumns(int profileId) {
		Optional
		.ofNullable(IrtGuiProperties.getProperty(String.format(REGISTER_COLUMN_ID, profileId)))
		.filter(Objects::nonNull)
		.map(Integer::parseInt)
		.ifPresent(rows->registersPanelController.setColumns(rows));
	}

	private void setTextFields(int profileId) {

		String key = String.format(REGISTER_TEXT_FIELD_ID, profileId);
		Optional
		.ofNullable(IrtGuiProperties.selectFromProperties(key))
		.ifPresent(property->property.entrySet()
					.parallelStream()
					.map(e->{
						String[] split = ((String)e.getKey()).replace(key, "").split("\\.");
						Map<String, String>map = new HashMap<>();
						map.put("name", (String) e.getValue());
						map.put("column", split[0]);
						map.put("row", split[1]);
						return map;
					})
					.forEach(map->setTextField(map))
				);
	}

	private void setTextField(Map<String, String> map){
		try {
			registersPanelController.setTextField(map.get("name"), Integer.parseInt(map.get("column")), Integer.parseInt(map.get("row")));
		} catch (Exception e) {
			logger.catching(e);
		}
	}
}
