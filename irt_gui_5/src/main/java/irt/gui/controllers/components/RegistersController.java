package irt.gui.controllers.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.controllers.interfaces.FxmlNode;
import irt.gui.controllers.interfaces.OtherFields;
import irt.gui.controllers.interfaces.ScheduledNode;
import irt.gui.controllers.interfaces.SliderListener;
import irt.gui.controllers.observers.TextFieldValueChangeObserver;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.listeners.TextFieldFocusListener;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.data.value.Value;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public class RegistersController implements Observer, FieldController {

	public static final String KEY_PROFILE_ID = "profile_id";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

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
	private static final String REGISTER_ALIGNMENT	 		= REGISTER_ALIGNMENT_ID 	+ "%d.%d";			//gui.regicter.controller.alignment.profileId.column.row (ex. gui.regicter.controller.alignment.3.5.7)

	private final Logger logger = LogManager.getLogger();

	@FXML private BorderPane registersPane;
	@FXML private GridPane panelRegisters;

	@FXML private Slider 		slider;
	@FXML private Button		buttonInitialize;
    @FXML private Button 		buttonCalibMode;
    @FXML private Button 		resetButton;
    @FXML private CheckBox 		stepCheckBox;
    @FXML private TextField 	stepTextField;
    @FXML private PanelRegisters panelRegistersController;

    @FXML private MenuItem 		menuSave;
    @FXML private Menu			menuProfile;

    @FXML private ButtonCalibrationMode buttonCalibModeController;

    private NumericChecker 		stepNumericChecker;
    private TextField 			selectedTextField;
    private TextInputDialog 	dialog 				= new TextInputDialog("default");
	private ToggleGroup 		profilesToggleGroup = new ToggleGroup();

	private int 	profileId;
	private Boolean editable;

	private TextFieldValueChangeObserver textFieldValueChangeObserver;

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

	private void removeCssClassAndDeleteObserver(String cssClass) {
		Optional
		.ofNullable(selectedTextField)
		.ifPresent(textField->{

			SliderListener textFieldRegister = (SliderListener) selectedTextField.getUserData();
			Value registerValue = textFieldRegister.getValue();
			registerValue.deleteObserver(textFieldValueChangeObserver);

			Platform.runLater(()->textField.getStyleClass().remove(cssClass));
		});
	}

	@SuppressWarnings("unchecked")
	private void showProfile(int profileId) throws NoSuchFieldException, IllegalAccessException {
		setRowsAndColumns(profileId);
		setNodesOf( profileId, TextFieldRegister.class, LabelValue.class, LabelRegister.class, OtherFields.class, TextFieldConfiguration.class);
		setFxmlNodes(profileId);
		menuSave.setDisable(false);
		panelRegistersController.setBackground(IrtGuiProperties.getProperty(String.format(REGISTER_BACKGROUND_ID, profileId)));
		setAlignment(profileId);

		setTabName();
	}

	private void setTabName() {
		if(tab!=null)
			menuProfile
			.getItems()
			.parallelStream()
			.map(RadioMenuItem.class::cast)
			.filter(rm->rm.isSelected())
			.findAny()
			.ifPresent(rm->tab.setText(rm.getText()));
	}

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
				.map(stf->(SliderListener)stf.getUserData())
				.ifPresent(controller->controller.setText(slider.getValue())));
	};

	private final ChangeListener<Boolean> textFieldFocusListenerRegisterPanel = (observable, oldValue, newValue)->{

		final TextField textField = (TextField) ((ReadOnlyBooleanProperty)observable).getBean();
		if(selectedTextField!=textField){

			removeCssClassAndDeleteObserver(ACTIVE);

			if(textField.isDisable() || !textField.isEditable()){

				selectedTextField = null;
				if(!slider.isDisable())
					Platform.runLater(()->slider.setDisable(!editable));

			}else{

				selectedTextField = textField;

				SliderListener textFieldcontroller = (SliderListener) selectedTextField.getUserData();

				//Set slider value, max, min
				textFieldcontroller.setSliderValue(slider, sliderValueChangeListener, ACTIVE, textFieldValueChangeObserver, stepNumericChecker);
				textFieldValueChangeObserver.setMultiplier(textFieldcontroller.getMultiplier());

				if(slider.isDisable())
					Platform.runLater(()->slider.setDisable(false));
			}
		}
	};

	private Tab tab;

    @FXML private void initialize(){

    	textFieldValueChangeObserver = new TextFieldValueChangeObserver(slider, sliderValueChangeListener);

    	registersPane.setUserData(this);
 
    	buttonCalibModeController.addObserver(this);
    	buttonCalibModeController.addObserver((Observer) buttonInitialize.getUserData());

    	stepNumericChecker = new NumericChecker(stepTextField.textProperty());

    	new TextFieldFocusListener(stepTextField);
    	stepTextField.focusedProperty().addListener(stepTextFieldFocusListener);

		panelRegistersController.setFocusListener(textFieldFocusListenerRegisterPanel);

		slider.valueProperty().addListener(sliderValueChangeListener);

		createProfileMenuItems();

		profileId = prefs.getInt(KEY_PROFILE_ID, 0);
		menuProfile
		.getItems()
		.parallelStream()
		.filter(m->m.getUserData()!=null)
		.filter(mp->Integer.parseInt((String) mp.getUserData())==profileId)
		.findAny()
		.ifPresent(mp->{
			try {
				((RadioMenuItem)mp).setSelected(true);
				showProfile(profileId);
			} catch (Exception e) {
				logger.catching(e);
			}
		});
    }

	@FXML private void onActionMenuNewProfile(){

		panelRegistersController.setColumnsAndRows(1, 1);
		menuSave.setDisable(true);
	}

	@FXML private void onActionResetButton(ActionEvent event) {
    	try {

    		panelRegistersController.reset();

    	} catch (Exception e) {
			logger.catching(e);
		}
    }

	@FXML public void onActionMenuProfile(ActionEvent event){

		((Menu) event.getSource())
		.getItems()
		.parallelStream()
		.filter(m->m.getId()==null || !m.getId().equals("menuNewProfile"))
		.map(m->(RadioMenuItem)m)
		.filter(rm->rm.isSelected())
		.findAny()
		.ifPresent(rmi->{
			try{

				profileId = Integer.parseInt((String) rmi.getUserData());// get profile ID
				showProfile(profileId);

				prefs.putInt(KEY_PROFILE_ID, profileId);

			}catch(Exception ex){
				logger.catching(ex);
			}
		});
	}

    @FXML private void saveValues(ActionEvent event) {
    	try {
			panelRegistersController.save();
		} catch (Exception e) {
			logger.catching(e);
		}
    }

    @FXML private void onMouseReleasedSlider() {
    	Optional
    	.ofNullable(selectedTextField)
    	.ifPresent(s->{
    		Platform.runLater(()->{
    			SliderListener controller = (SliderListener) s.getUserData();
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

		dialog.
		getEditor()
		.setText(IrtGuiProperties.getProperty(String.format(REGISTER_PROPERTY_NAME_ID, profileId)));

		dialog
		.showAndWait()
    	.ifPresent(name->{
    		try { saveProfile(name); } catch (Exception e) { logger.catching(e); }
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

				panelRegistersController.setEditable(editable);

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

    	//Save to file
    	try(final FileOutputStream outputStream = new FileOutputStream(new File(IrtGuiProperties.IRT_HOME, IrtGuiProperties.getPropertiesFileName()));){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    		IrtGuiProperties.updateProperties(propertiesFromFile);
    	}
    }

    private void saveProfile(String profileName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, FileNotFoundException, IOException{
 
    	final Properties propertiesFromFile = getPropertiesFromFile();
    	updateProperties(propertiesFromFile, profileId, profileName);

    	final String fileName 	= IrtGuiProperties.getPropertiesFileName();
		final File file 		= new File(IrtGuiProperties.IRT_HOME, fileName);
		try(final FileOutputStream outputStream = new FileOutputStream(file);){
    		propertiesFromFile.store(outputStream, "Gui5 Properties");
    	}

		//Reload Properties and Profile Menu
		IrtGuiProperties.reload();
		menuProfile.getItems().clear();
		createProfileMenuItems();
	}

	private void updateProperties(Properties propertiesFromFile, int profileId, String profileName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		propertiesFromFile.put(String.format(REGISTER_PROPERTY_NAME_ID	, profileId), profileName);
		propertiesFromFile.put(String.format(REGISTER_ROW_ID			, profileId), Integer.toString(panelRegistersController.getRowCount()));
		propertiesFromFile.put(String.format(REGISTER_COLUMN_ID			, profileId), Integer.toString(panelRegistersController.getColumnCount()));

		//TextFields properties
		removeProperties(propertiesFromFile, String.format( TextFieldRegister.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, TextFieldRegister.class);

		//LabelValue properties
		removeProperties(propertiesFromFile, String.format( LabelValue.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, LabelValue.class);

		//LabelRegister properties
		removeProperties(propertiesFromFile, String.format( LabelRegister.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, LabelRegister.class);

		//TextFieldConfiguration properties ( Control )
		removeProperties(propertiesFromFile, String.format( TextFieldConfiguration.FIELD_KEY_ID, profileId));
		putProperies(propertiesFromFile, profileId, TextFieldConfiguration.class);

		//Other properties
		removeProperties(propertiesFromFile, String.format( OtherFields.FIELD_KEY_ID, profileId));
		putOtherFieldsProperies(propertiesFromFile, profileId);

		//FxmlNode properties
		removeProperties(propertiesFromFile, String.format( FxmlNode.FIELD_KEY_ID, profileId));
		putFxmlNodesProperies(propertiesFromFile, profileId);

		final String backgroundPath = panelRegistersController.getBackgroundPath();
		if(backgroundPath!=null)
			propertiesFromFile.put(String.format(REGISTER_BACKGROUND_ID, profileId), backgroundPath);

		//put TextFields alignment properties
		panelRegistersController
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

	private void putOtherFieldsProperies(Properties propertiesFromFile, int profileId) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		List<Map<String, Object>> otherFieldsProperties = panelRegistersController.getOtherFieldsProperties();

		otherFieldsProperties
		.parallelStream()
		.forEach(tfp->{
			String key = String.format(OtherFields.FIELD_KEY, profileId, tfp.get("column"), tfp.get("row"));
			propertiesFromFile.put(key, tfp.get("name"));
		});
	}

	private void putFxmlNodesProperies(Properties propertiesFromFile, int profileId2) {
		List<Map<String, Object>> otherFieldsProperties = panelRegistersController.getFxmlNodesProperties();

		otherFieldsProperties
		.parallelStream()
		.forEach(tfp->{
			String key = String.format(FxmlNode.FIELD_KEY, profileId, tfp.get("column"), tfp.get("row"));
			propertiesFromFile.put(key, tfp.get("fxml"));
		});
	}

	private void putProperies(Properties propertiesFromFile, int profileId, Class<? extends ScheduledNode> nodeClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		List<Map<String, Object>> textFieldsProperties = panelRegistersController.getFieldsProperties(nodeClass);

		textFieldsProperties
		.parallelStream()
		.forEach(tfp->{
			putProperties(propertiesFromFile, profileId, nodeClass, tfp);
		});
	}

	private void putProperties(Properties propertiesFromFile, int profileId, Class<? extends ScheduledNode> nodeClass, Map<String, Object> tfp) {
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
 		Properties properties = new Properties(){
			private static final long serialVersionUID = -3674674113970367029L;

			@Override
 		    public synchronized Enumeration<Object> keys() {
 		        return Collections.enumeration(new TreeSet<Object>(super.keySet()));// alphabetical key order
 		    }
 		};

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
				.sorted((a, b)->((String)a.getValue()).compareTo((String) b.getValue()))
				.map(p->{
					final RadioMenuItem radioMenuItem = new RadioMenuItem((String)p.getValue());
					radioMenuItem.setUserData(((String)p.getKey()).replace(REGISTER_PROPERTY_NAME, ""));
					return radioMenuItem;
				})
				.collect(Collectors.toList());

		final ObservableList<MenuItem> items = menuProfile.getItems();
		items.addAll(menuItems);

		items
		.parallelStream()
		.forEach(rm->Platform.runLater(()->((RadioMenuItem)rm).setToggleGroup(profilesToggleGroup)));
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
					.forEach(map->panelRegistersController.setAlignment(Pos.valueOf(map.get("pos")), Integer.parseInt(map.get("column")), Integer.parseInt(map.get("row"))))
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
				.ifPresent(columns->panelRegistersController.setColumnsAndRows( columns, rows)));
	}

	@SuppressWarnings("unchecked")
	private void setNodesOf( int profileId, Class<? extends ScheduledNode>... fieldClass) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		for(Class<? extends ScheduledNode> c:fieldClass)
			setNodesOf(c, profileId);
	}

	private void setNodesOf(Class<? extends ScheduledNode> fieldClass, int profileId) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		final Field field = fieldClass.getField("FIELD_KEY_ID");
		String key = String.format((String)field.get(null), profileId);

		Optional
		.ofNullable(IrtGuiProperties.selectFromProperties(key))
		.ifPresent(property->
							property.entrySet()
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

	private void setFxmlNodes(int profileId2) {
		String key = String.format(FxmlNode.FIELD_KEY_ID, profileId);

		Optional
		.ofNullable(IrtGuiProperties.selectFromProperties(key))
		.ifPresent(property->
							property
							.entrySet()
							.parallelStream()
							.map(e->{
										String[] split = ((String)e.getKey()).replace(key, "").split("\\.");

										Map<String, String>map = new HashMap<>();
										map.put("fxml", (String) e.getValue());
										map.put("column", split[0]);
										map.put("row", split[1]);
										return map;
							})
							.forEach(map->setFxmlNode(map))
				);
	}

	private void setFxmlNode(Map<String, String> map) {

		try {
			final String fxml = map.get("fxml");
			final int column = Integer.parseInt(map.get("column"));
			final int row = Integer.parseInt(map.get("row"));

			Platform.runLater(()->{
				try {

					Node node = panelRegistersController.loadNode(fxml, column, row);

					Optional
					.ofNullable(tab)
					.filter(t->t.isSelected())
					.ifPresent(t->((FieldController)node.getUserData()).doUpdate(true));

//					Optional
//					.ofNullable(editable)
//					.filter(TextField.class::isInstance);

				} catch (Exception e) {
					logger.catching(e);
				}

			});

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void setNode(Class<? extends ScheduledNode> fieldClass, Map<String, String> map){

		try {
			final String key = map.get("key");
			final int column = Integer.parseInt(map.get("column"));
			final int row = Integer.parseInt(map.get("row"));
			final Class<? extends ScheduledNode> fc = fieldClass.equals(OtherFields.class) ?  (Class<? extends ScheduledNode>) Class.forName(key) : fieldClass;

			Platform.runLater(()->{
				try {

					Node node = panelRegistersController.setNode(fc, key, column, row);

					Optional
					.ofNullable(tab)
					.filter(t->t.isSelected())
					.ifPresent(t->((FieldController)node.getUserData()).doUpdate(true));

					Optional
					.ofNullable(editable)
					.filter(TextField.class::isInstance);

				} catch (Exception e) {
					logger.catching(e);
				}

			});

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void doUpdate(boolean doUpdate) {
		((FieldController)buttonInitialize.getUserData()).doUpdate(doUpdate);
		((FieldController)buttonCalibMode.getUserData()).doUpdate(doUpdate);
		((FieldController)panelRegisters.getUserData()).doUpdate(doUpdate);
	}

	public void setTab(Tab biasTab) {
		tab = biasTab;
		setTabName();
	}
}
