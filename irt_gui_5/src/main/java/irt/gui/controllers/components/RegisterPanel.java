package irt.gui.controllers.components;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.data.GuiUtility;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class RegisterPanel{
	private final Logger logger = LogManager.getLogger();

	public static final File 	IRT_HOME			= new File(System.getProperty("user.home"), "irt") ;
	public static final String 	RESOURCE_FOLDER 	= "gui.register.gridPane.background.resource";
	public static final String 	FILE_SYSTEM_PATH 	= "gui.register.gridPane.background.path";

	@FXML private GridPane gridPane;

	@FXML private ContextMenu contextMenu;
    @FXML private Menu 		menuRegister;
	@FXML private MenuItem 	menuItemShowGrid;
	@FXML private MenuItem 	menuItemAddColumn;
	@FXML private MenuItem 	menuItemDeleteColumn;
	@FXML private MenuItem 	menuItemAddRow;
	@FXML private MenuItem 	menuItemDeleteRow;

	@FXML private Menu 		menuAlignment;
	@FXML private Menu 		menuBackground;
	@FXML private Menu 		menuValueLabel;
	@FXML private Menu 		menuRegisterLabel;

	private VBox 		paneUnderMouse;
	private ToggleGroup backgroundToggleGroup = new ToggleGroup();
	private String 		backgroundPath; 		public String getBackgroundPath() { return backgroundPath; }

	private ChangeListener<Boolean> focusListener; public ChangeListener<Boolean> getFocusListener() { return focusListener; } public void setFocusListener(ChangeListener<Boolean> focusListener) { this.focusListener = focusListener; }

	private final EventHandler<? super MouseEvent> mouseEvent = e->paneMouseEntered(e);

	private boolean disable;

	@FXML private void initialize(){
		createMenuItems();
	}

	@FXML private void addColumnonActionMenuItems() {

		final int columnIndex = Optional.ofNullable(GridPane.getColumnIndex(paneUnderMouse)).map(ci->++ci).orElse(1);

		addColumn(columnIndex);

		if(menuItemDeleteColumn.isDisable())
			menuItemDeleteColumn.setDisable(false);
	}

	private void addColumn(final int columnIndex) {
		addColumnConstaints(columnIndex);
		shiftColumnsNodes(true, columnIndex);
		fillColumn(columnIndex);
		setColumnPercentage();
	}

	@FXML private void deleteColumnonActionMenuItems() {

		final int columnIndex = Optional.ofNullable(GridPane.getColumnIndex(paneUnderMouse)).orElse(0);

		shiftColumnsNodes(false, columnIndex);
		removeColumnConstraints(columnIndex);
		setColumnPercentage();

		if(gridPane.getColumnConstraints().size()<=1)
			menuItemDeleteColumn.setDisable(true);
	}

	@FXML private void addRowonActionMenuItems() {

		final Integer rowIndex = Optional.ofNullable(GridPane.getRowIndex(paneUnderMouse)).map(ci->++ci).orElse(1);
		addRow(rowIndex);

		if(menuItemDeleteRow.isDisable())
			menuItemDeleteRow.setDisable(false);
	}

	private void addRow(final Integer rowIndex) {
		addRowConstraints(rowIndex);
		shiftRowsNodes(true, rowIndex);
		fillRow(rowIndex);
		setRowPercentage();
	}

	@FXML private void onActionMenuItemsDeleteRow() {

		final Integer rowIndex = Optional.ofNullable(GridPane.getRowIndex(paneUnderMouse)).orElse(0);

		shiftRowsNodes(false, rowIndex);
		removeRowConstraints(rowIndex);
		setRowPercentage();

		if(gridPane.getRowConstraints().size()<=1)
			menuItemDeleteRow.setDisable(true);
	}

	@FXML private void alignmentonActionMenuItems(ActionEvent e) {
		MenuItem m = (MenuItem)e.getSource();
		paneUnderMouse.setAlignment((Pos) m.getUserData());
	}

	@FXML private void showGridonActionMenuItems() {
		final boolean gridLinesVisible = gridPane.isGridLinesVisible();
		gridPane.setGridLinesVisible(!gridLinesVisible);
		menuItemShowGrid.setText(gridLinesVisible ? "Show Grid" : "Hide Grid");
	}

	@FXML private void gridPanContextMenuRequest(ContextMenuEvent event) {
		contextMenu.show(gridPane, event.getScreenX(), event.getScreenY());
    }

    @FXML private void paneMouseEntered(MouseEvent event) {
    	paneUnderMouse = (VBox) event.getSource();
    }

    @FXML private void onActionMenuItemRemove(){
    	paneUnderMouse.getChildren().clear();
	}

	private void createMenuItems() {
		createMenuItemsRegister();
		createMenuItemsAlignment();
		createBackgroundRadioMenuItems();
		createValueLabelMenuItems();
		createRegisterLabelMenuItems();
	}

	private void createRegisterLabelMenuItems() {
		GuiUtility.createMamuItems(RegisterLabel.PROPERTY_STARTS_WITH, onActionMenuItemRegisterLabel, menuRegisterLabel.getItems());
	}

	private void createValueLabelMenuItems() {
		GuiUtility.createMamuItems(ValueLabel.PROPERTY_STARTS_WITH, onActionMenuItemValueLabel, menuValueLabel.getItems());
	}

	private void createMenuItemsRegister() {
		GuiUtility.createMamuItems(RegisterTextField.PROPERTY_STARTS_WITH, registeronActionMenuItems, menuRegister.getItems());
	}

	private void createMenuItemsAlignment() {
		MenuItem[] mis = Arrays.stream(Pos.values())
						.map(p->{
								MenuItem mi = new MenuItem(p.name());
								mi.setOnAction(e->alignmentonActionMenuItems(e));
								mi.setUserData(p);
								return mi;})
						.toArray(MenuItem[]::new);
		menuAlignment.getItems().addAll(mis);
	}

	private void createBackgroundRadioMenuItems() {
		try {

			createtMenuBackgroundItemsFromResource();
			createBackgrounBackgroundFromPath();
			

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void createtMenuBackgroundItemsFromResource() throws IOException, URISyntaxException {

		final String resourceFolder = IrtGuiProperties.getProperty(RESOURCE_FOLDER);
		final List<Object> resourceFiles = GuiUtility.getResourceFiles(resourceFolder);
		final Optional<List<Object>> optional = Optional.ofNullable(resourceFiles);
		optional
		.ifPresent(fs->{
			final List<RadioMenuItem> menuItems = createBackgroundRadioMenuItems(fs);

			menuBackground.getItems().addAll(menuItems);
		});
	}

	private List<RadioMenuItem> createBackgroundRadioMenuItems(List<Object> files) {
		return files.parallelStream().map(file -> {

			RadioMenuItem rmi = new RadioMenuItem();
			rmi.setText(file instanceof JarEntry ? ((JarEntry) file).getName() : ((File) file).getName());

			Platform.runLater(()->{
				rmi.setUserData(file);
				rmi.setOnAction(backgrountonActionMenuItems);
				rmi.setToggleGroup(backgroundToggleGroup);
			});
			return rmi;
		})
				.sorted((rmi1, rmi2)->rmi1.getText().compareTo(rmi2.getText()))
				.collect(Collectors.toList());
	}

	private void createBackgrounBackgroundFromPath() {
		final String resourceFolder = IrtGuiProperties.getProperty(FILE_SYSTEM_PATH);
		Optional
		.ofNullable(resourceFolder)
		.ifPresent(path->{

			final ObservableList<MenuItem> menuItems = menuBackground.getItems();
			menuItems.add(new SeparatorMenuItem());

			final List<Object> files = Arrays
										.stream(path.split(","))
										.parallel()
										.map(p->p.contains(":") ? new File(p) : new File(IRT_HOME, p))
										.map(File::listFiles)
										.filter(Objects::nonNull)
										.flatMap(fs->Arrays
												.stream(fs).parallel())
										.collect(Collectors.toList());

			final List<RadioMenuItem> mi = createBackgroundRadioMenuItems(files);
			menuItems.addAll(mi);

			// C:\Users\Oleksandr\images\register\background
			//C:\Users\Oleksandr\irt\images\register\background
		});
	}

	private final EventHandler<ActionEvent> registeronActionMenuItems = e->{
		Platform.runLater(()->{

				try {

					FXMLLoader loader = loadNode(RegisterTextField.class, paneUnderMouse.getChildren());
					RegisterTextField textFieldController = loader.getController();
					textFieldController.setKeyStartWith(((MenuItem)e.getSource()).getId());

				} catch (Exception ex) {
					logger.catching(ex);
				}
		});
	};

	private void fillColumn(final int columnIndex) {
		IntStream.range(0, gridPane.getRowConstraints().size())
		.forEach(rowIndex->gridPane.add(getPane(), columnIndex, rowIndex));
	}

	private void fillRow(final Integer rowIndex) {
		IntStream.range(0, gridPane.getColumnConstraints().size())
		.forEach(columnIndex->gridPane.add(getPane(), columnIndex, rowIndex));
	}

	private void addColumnConstaints(final int columnIndex) {
		ColumnConstraints colConstraints = new ColumnConstraints();
        colConstraints.setHgrow(Priority.ALWAYS);
		gridPane.getColumnConstraints().add(columnIndex, colConstraints);
	}

	private void addRowConstraints(final int rowIndex) {
		RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
		gridPane.getRowConstraints().add(rowIndex, rowConstraints);
	}

	private void removeColumnConstraints(final int columnIndex) {
		final ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
		columnConstraints.remove(columnIndex);
	}

	private void removeRowConstraints(final int rowIndex) {
		final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
		rowConstraints.remove(rowIndex);
	}

	private void shiftColumnsNodes(boolean shiftToRight, int columnIndex) {
		final int shift = shiftToRight ? 1 : -1;
		final int size = gridPane.getColumnConstraints().size();

		final ObservableList<Node> children = gridPane.getChildren();
		children.parallelStream().forEach(child->{

			final Integer cIndex = Optional.ofNullable(GridPane.getColumnIndex(child)).orElse(0);
			final Integer rIndex = Optional.ofNullable(GridPane.getRowIndex(child)).orElse(0);

			if(cIndex>=columnIndex){

				Platform.runLater(()->{
					children.remove(child);

					int ci = cIndex + shift;
					if(ci>=columnIndex && ci<size)
						gridPane.add(child, ci, rIndex);
				});
			}
		});
	}

	private void shiftRowsNodes(boolean shiftToBottom, Integer rowIndex) {
		final int shift = shiftToBottom ? 1 : -1;
		final int size = gridPane.getRowConstraints().size();
		final ObservableList<Node> children = gridPane.getChildren();

		children.parallelStream().forEach(child->{

			final Integer cIndex = Optional.ofNullable(GridPane.getColumnIndex(child)).orElse(0);
			final Integer rIndex = Optional.ofNullable(GridPane.getRowIndex(child)).orElse(0);

			if(rIndex>=rowIndex){

				Platform.runLater(()->{
					children.remove(child);

					int ri = rIndex + shift;
					if(ri>=rowIndex && ri<size)
						gridPane.add(child, cIndex, ri);
				});
			}
		});
	}

	private void setRowPercentage() {
		final ObservableList<RowConstraints> rowConstraints = gridPane.getRowConstraints();
		final double percentage = 100.0 / rowConstraints.size();
		rowConstraints.parallelStream().forEach(r->r.setPercentHeight(percentage));
	}

	private void setColumnPercentage() {
		final ObservableList<ColumnConstraints> columnConstraints = gridPane.getColumnConstraints();
		final double percentage = 100.0 / columnConstraints.size();
		columnConstraints.parallelStream().forEach(c->c.setPercentWidth(percentage));
	}

	private Pane getPane() {
		final VBox pane = new VBox();
		pane.setAlignment(Pos.CENTER);
		pane.setFillWidth(false);
		pane.setOnMouseEntered(mouseEvent);
		pane.setFocusTraversable(false);
		pane.getStyleClass().add("mouseOver");
		return pane;
	}

	public void setDisable(boolean disable) {
		this.disable = disable;

		gridPane
		.getChildren()
		.parallelStream()
		.filter(n->n instanceof VBox)
		.map(n->(VBox)n)
		.filter(v->v.getChildren()!=null)
		.map(VBox::getChildren)
		.flatMap(ch->ch.parallelStream())
		.filter(ch->ch instanceof TextField)
		.map(ch->(TextField)ch)
		.forEach(tf->Platform.runLater(()->tf.setDisable(disable)));
	}

	public void reset() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Stream<RegisterTextField> controllers = getAllTextFieldControllers();
		controllers.forEach(controller->reset(controller));
	}

	private void reset(RegisterTextField controller) {
		try { controller.reset(); } catch (Exception e) { logger.catching(e); }
	}

	public void save() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Stream<RegisterTextField> controllers = getAllTextFieldControllers();
		controllers.forEach(controller->save(controller));
	}

	private void save(RegisterTextField controller) {
		try { controller.save(); } catch (PacketParsingException e) { logger.catching(e); };
	}

	@SuppressWarnings("unchecked")
	private Stream<RegisterTextField> getAllTextFieldControllers() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (Stream<RegisterTextField>) getAllControllersOf(RegisterTextField.class);
	}

	private Stream<? extends ScheduledNode> getAllControllersOf(Class<? extends ScheduledNode> controllerClass) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getAllNodesOf(controllerClass).map(tf->controllerClass.cast(tf.getUserData()));
	}

	public Stream<VBox> getAllVBoxex() {
		return gridPane.getChildren()
				.parallelStream()
				.filter(ch->ch instanceof VBox)
				.map(ch->(VBox)ch);
	}

	private Stream<? extends Node> getAllNodesOf(Class<? extends ScheduledNode> controllerClass) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.entry(controllerClass);

		final Method method = controllerClass.getDeclaredMethod("getPootClass");
		@SuppressWarnings("unchecked")
		final Class<? extends Node> nodeClass = (Class<? extends Node>)method.invoke(null);

		Stream<VBox> e = getAllVBoxex();
		return e.map(vb->vb.getChildren())
				.flatMap(ch->ch.parallelStream())
				.filter(nodeClass::isInstance)
				.filter(ps->controllerClass.isInstance(ps.getUserData()))
				.map(nodeClass::cast);
	}

	private final EventHandler<ActionEvent> onActionMenuItemValueLabel = e->{
		Platform.runLater(()->{

			final FXMLLoader load = loadNode(ValueLabel.class, paneUnderMouse.getChildren());
			final ValueLabel controller = load.getController();
			try{
				controller.setKeyStartWith(((MenuItem) e.getSource()).getId());
			}catch(Exception ex){
				logger.catching(ex);
			}
		});
	};

	private final EventHandler<ActionEvent> onActionMenuItemRegisterLabel = e->{
		Platform.runLater(()->{

			final FXMLLoader load = loadNode(RegisterLabel.class, paneUnderMouse.getChildren());
			final RegisterLabel controller = load.getController();
			try{
				controller.setKeyStartWith(((MenuItem) e.getSource()).getId());
			}catch(Exception ex){
				logger.catching(ex);
			}
		});
		//TODO
	};

	private final EventHandler<ActionEvent> backgrountonActionMenuItems = e->{

		final RadioMenuItem source = (RadioMenuItem) e.getSource();
		final Object userData = source.getUserData();

			try{
				if(userData instanceof JarEntry){

					setBackground(((JarEntry)userData).getName());

				}else{

					final File file = (File)userData;
					if(file.exists())
						setBackground("file:" + file.toURI().toURL().getPath());

				}
			}catch(Exception ex){
				logger.catching(ex);
			}
	};

	public void setBackground(String backgroundPath) {
		if(backgroundPath==null)
			return;

		this.backgroundPath = backgroundPath;

		if(!backgroundPath.startsWith("file:")){
			final String resourceFolder = IrtGuiProperties.getProperty(RESOURCE_FOLDER);
			final URL resource = getClass().getResource("/" + resourceFolder + backgroundPath);
			backgroundPath = resource.toExternalForm();
		}

		final String format = String.format("-fx-background-image: url(\"%s\");", backgroundPath);
		gridPane.setStyle(format);
	}

	public int getRowCount() {
		return gridPane.getRowConstraints().size();
	}

	public int getColumnCount() {
		return gridPane.getColumnConstraints().size();
	}

	public List<Map<String, Object>> getFieldsProperties(Class<? extends ScheduledNode> nodeClass) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		logger.entry(nodeClass);


		return getAllNodesOf(nodeClass)
				.map(tf->{
					Map<String, Object> map = new HashMap<>();
					map.put("name", nodeClass.cast(tf.getUserData()).getPropertyName());
					map.put("row", Optional.ofNullable(GridPane.getRowIndex(tf.getParent())).orElse(0));
					map.put("column", Optional.ofNullable(GridPane.getColumnIndex(tf.getParent())).orElse(0));
					return map;
				})
				.collect(Collectors.toList());
	}

	public List<Map<String, Object>> getVBoxesAlignmentProperties() {
		return getAllVBoxex()
				.map(box->{
					Map<String, Object> map = new HashMap<>();
					map.put("pos", box.getAlignment());
					map.put("row", Optional.ofNullable(GridPane.getRowIndex(box)).orElse(0));
					map.put("column", Optional.ofNullable(GridPane.getColumnIndex(box)).orElse(0));

					return map;
				})
				.collect(Collectors.toList());
	}

	public void setRows(int rows) {
		gridPane.getRowConstraints().clear();

		IntStream
		.range(0, rows)
		.forEach(count->addRow(count));
	}

	public void setColumns(Integer rows) {
		gridPane.getColumnConstraints().clear();

		IntStream
		.range(0, rows)
		.forEach(count->addColumn(count));
	}

	public void setNode(Class<? extends ScheduledNode> fieldClass, String name, int column, int row) throws IOException {

		getVBoxAt(column, row)
		.map(vBox->vBox.getChildren())
		.forEach(child->{
			final FXMLLoader loader = loadNode(fieldClass, child);
			ScheduledNode controller = loader.getController();
			try {
				controller.setKeyStartWith(name);
			} catch (Exception e) {
				logger.catching(e);
			}
		});
	}

	public Stream<VBox> getVBoxAt(int column, int row) {
		return gridPane
				.getChildren()
				.parallelStream()
				.filter(VBox.class::isInstance)
				.filter(child->Optional.ofNullable(GridPane.getColumnIndex(child)).orElse(0)==column)
				.filter(child->Optional.ofNullable(GridPane.getRowIndex(child)).orElse(0)==row)
				.map(child->(VBox)child);
	}

	public void setAlignment(Pos pos, int column, int row) {
		getVBoxAt(column, row)
		.forEach(box->box.setAlignment(pos));
	}

	private FXMLLoader loadNode(Class<? extends ScheduledNode> controllerClass, final ObservableList<Node> children){

		FXMLLoader loader = null;
		try {
			final Field field = controllerClass.getField("FXML_PATH");
			loader = new FXMLLoader( getClass().getResource((String) field.get(null)));

			Node root = loader.load();
			if(focusListener!=null)
				root.focusedProperty().addListener(focusListener);

			if(controllerClass==RegisterTextField.class)
				root.setDisable(disable);

			Platform.runLater(()->children.add(root));

		} catch (Exception e) {
			logger.catching(e);
		}

		return loader;
	}
}
