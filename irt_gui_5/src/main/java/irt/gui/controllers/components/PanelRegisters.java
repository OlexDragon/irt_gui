package irt.gui.controllers.components;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.controllers.interfaces.OtherFields;
import irt.gui.controllers.interfaces.ScheduledNode;
import irt.gui.data.GuiUtility;
import irt.gui.data.packet.interfaces.ConfigurationGroup;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class PanelRegisters implements Initializable, FieldController {
	private final Logger logger = LogManager.getLogger();

	public static final File 	IRT_HOME			= new File(System.getProperty("user.home"), "irt") ;
	public static final String 	RESOURCE_FOLDER 	= "gui.register.gridPane.background.resource";
	public static final String 	FILE_SYSTEM_PATH 	= "gui.register.gridPane.background.path";
	private ResourceBundle bundle;	

	@FXML private GridPane gridPane;

	@FXML private ContextMenu contextMenu;
	@FXML private MenuItem 	menuItemShowGrid;
//	@FXML private MenuItem 	menuItemAddColumn;
	@FXML private MenuItem 	menuItemDeleteColumn;
//	@FXML private MenuItem 	menuItemAddRow;
	@FXML private MenuItem 	menuItemDeleteRow;

	@FXML private Menu 		menuAlignment;
	@FXML private Menu 		menuBackground;

	@FXML private Menu 		menuRegister;
	@FXML private Menu 		menuValueLabel;
	@FXML private Menu 		menuRegisterLabel;
	@FXML private Menu 		menuControl;
	@FXML private Menu 		menuOther;

	private VBox 		paneUnderMouse;
	private ToggleGroup backgroundToggleGroup = new ToggleGroup();
	private String 		backgroundPath; 		public String getBackgroundPath() { return backgroundPath; }

	private ChangeListener<Boolean> focusListener; public ChangeListener<Boolean> getFocusListener() { return focusListener; } public void setFocusListener(ChangeListener<Boolean> focusListener) { this.focusListener = focusListener; }

	private final EventHandler<? super MouseEvent> mouseEvent = e->onMouseEntered(e);

	private boolean editable;

	private final EventHandler<ActionEvent> onActionMenuItemRegister 		= e->{
																					Node node = loadNode(TextFieldRegister.class, ((MenuItem) e.getSource()).getId(), paneUnderMouse.getChildren());
																					((FieldController)node.getUserData()).doUpdate(true);
	};
	private final EventHandler<ActionEvent> onActionMenuItemValueLabel 		= e->{
																					Node node = loadNode(LabelValue.class, 	((MenuItem) e.getSource()).getId(), paneUnderMouse.getChildren());
																					((FieldController)node.getUserData()).doUpdate(true);
	};
	private final EventHandler<ActionEvent> onActionMenuItemControl 		= e->{
																					Node node = loadNode(TextFieldConfiguration.class, ((MenuItem) e.getSource()).getId(), paneUnderMouse.getChildren());
																					((FieldController)node.getUserData()).doUpdate(true);
	};
	private final EventHandler<ActionEvent> onActionMenuItemRegisterLabel 	= e->{
																					Node node = loadNode(LabelRegister.class, ((MenuItem) e.getSource()).getId(), paneUnderMouse.getChildren());
																					((FieldController)node.getUserData()).doUpdate(true);
	};
	private final EventHandler<ActionEvent> onActionMenuItemOther			= e->loadNode(((MenuItem) e.getSource()).getId(), paneUnderMouse.getChildren());

	@Override public void initialize(URL location, ResourceBundle resources){
		gridPane.setUserData(this);
		bundle = resources;
//		this.location = location;

		createMenuItemsRegisterTextField();
		createMenuItemsControlTextField();
		createMenuItemsValueLabel();
		createMenuItemsRegisterLabel();
		createMenuItemsOther();
		createMenuItemsAlignment();
		createMenuItemsBackground();
	}

	@FXML private void onActionMenuItemAddColumn() {

		final int columnIndex = Optional
									.ofNullable(GridPane.getColumnIndex(paneUnderMouse))
									.map(ci->++ci)
									.orElse(1);

		addColumn(columnIndex);

		if(menuItemDeleteColumn.isDisable())
			menuItemDeleteColumn.setDisable(false);
	}

	@FXML private void onActionMenuItemDeleteColumn() {

		final int columnIndex = Optional.ofNullable(GridPane.getColumnIndex(paneUnderMouse)).orElse(0);

		shiftColumnsNodes(false, columnIndex);
		removeColumnConstraints(columnIndex);
		setColumnPercentage();

		if(gridPane.getColumnConstraints().size()<=1)
			menuItemDeleteColumn.setDisable(true);
	}

	@FXML private void onActionMenuItemAddRow() {

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

	@FXML private void onActionMenuItemDeleteRow() {

		final Integer rowIndex = Optional.ofNullable(GridPane.getRowIndex(paneUnderMouse)).orElse(0);

		shiftRowsNodes(false, rowIndex);
		removeRowConstraints(rowIndex);
		setRowPercentage();

		if(gridPane.getRowConstraints().size()<=1)
			menuItemDeleteRow.setDisable(true);
	}

	@FXML private void onActionMenuItemClear(){
		setColumnsAndRows(1, 1);
	}

	@FXML private void alignmentonActionMenuItems(ActionEvent e) {
		MenuItem m = (MenuItem)e.getSource();
		paneUnderMouse.setAlignment((Pos) m.getUserData());
	}

	@FXML private void onActionMenuItemShowGridon() {
		final boolean gridLinesVisible = gridPane.isGridLinesVisible();
		gridPane.setGridLinesVisible(!gridLinesVisible);
		menuItemShowGrid.setText(gridLinesVisible ? "Show Grid" : "Hide Grid");
	}

	@FXML private void gridPanContextMenuRequest(ContextMenuEvent event) {
		contextMenu.show(gridPane, event.getScreenX(), event.getScreenY());
    }

    @FXML private void onMouseEntered(MouseEvent event) {
    	if(!contextMenu.isShowing())
    		paneUnderMouse = (VBox) event.getSource();
    }

    @FXML private void onActionMenuItemRemove(){
    	paneUnderMouse.getChildren().clear();
	}

	private void createMenuItemsControlTextField() {
		GuiUtility.createMamuItems(TextFieldConfiguration.PROPERTY_STARTS_WITH, onActionMenuItemControl, menuControl.getItems());
	}

	private void createMenuItemsRegisterLabel() {
		GuiUtility.createMamuItems(LabelRegister.PROPERTY_STARTS_WITH, onActionMenuItemRegisterLabel, menuRegisterLabel.getItems());
	}

	private void createMenuItemsValueLabel() {
		GuiUtility.createMamuItems(LabelValue.PROPERTY_STARTS_WITH, onActionMenuItemValueLabel, menuValueLabel.getItems());
	}

	private void createMenuItemsRegisterTextField() {
		GuiUtility.createMamuItems(TextFieldRegister.PROPERTY_STARTS_WITH, onActionMenuItemRegister, menuRegister.getItems());
	}

	private void createMenuItemsOther() {
		GuiUtility.createMamuItems(OtherFields.PROPERTY_STARTS_WITH, onActionMenuItemOther, menuOther.getItems());
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

	private void createMenuItemsBackground() {
		try {

			createMenuBackgroundItemsFromResource();

			menuBackground.getItems().add(new SeparatorMenuItem());

			createMenuBackdroundItemsFromPath(IrtGuiProperties.getProperty(FILE_SYSTEM_PATH));
			

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void createMenuBackgroundItemsFromResource() throws IOException, URISyntaxException {

		final Optional<String> recourceFolderPath = Optional.ofNullable(IrtGuiProperties.getProperty(RESOURCE_FOLDER));
		if(recourceFolderPath.isPresent()){
			
			Optional
			.ofNullable(GuiUtility.getResourceFiles(recourceFolderPath.get()))
			.ifPresent(res->{

				final List<RadioMenuItem> menuItems = createBackgroundRadioMenuItems(res);
				menuBackground.getItems().addAll(menuItems);				
			});
		}
	}

	public void createMenuBackdroundItemsFromPath(final String resourceFolder) {
		Optional
		.ofNullable(resourceFolder)
		.ifPresent(path->{

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
			menuBackground.getItems().addAll(mi);

			// C:\Users\Oleksandr\images\register\background
			//C:\Users\Oleksandr\irt\images\register\background
		});
	}

	private List<RadioMenuItem> createBackgroundRadioMenuItems(List<Object> files) {
		return files.parallelStream().map(file -> {

			RadioMenuItem rmi = new RadioMenuItem();
			rmi.setText(file instanceof JarEntry ? ((JarEntry) file).getName() : ((File) file).getName());

			Platform.runLater(()->{
				rmi.setUserData(file);
				rmi.setOnAction(onActionMenuItemBackgrount);
				rmi.setToggleGroup(backgroundToggleGroup);
			});
			return rmi;
		})
				.sorted((rmi1, rmi2)->rmi1.getText().compareTo(rmi2.getText()))
				.collect(Collectors.toList());
	}

	private void fillColumn(final int columnIndex) {
		
		IntStream
		.range(0, gridPane.getRowConstraints().size())
		.filter(rowIndex->!getVBoxAt(columnIndex, rowIndex).isPresent())
		.forEach(rowIndex->{
			final Pane pane = getPane();
			Platform.runLater(()->gridPane.add(pane, columnIndex, rowIndex));
		});
	}

	private void fillRow(final Integer rowIndex) {
		IntStream
		.range(0, gridPane.getColumnConstraints().size())
		.filter(columnIndex->!getVBoxAt(columnIndex, rowIndex).isPresent())
		.forEach(columnIndex->{
			final Pane pane = getPane();
			Platform.runLater(()->gridPane.add(pane, columnIndex, rowIndex));
		});
	}

	private void addColumn(final int columnIndex) {
		addColumnConstaints(columnIndex);
		shiftColumnsNodes(true, columnIndex);
		fillColumn(columnIndex);
		setColumnPercentage();
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

		children
		.parallelStream()
		.forEach(child->{

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
		pane.setFillWidth(false);
		pane.setAlignment(Pos.CENTER);
		pane.setOnMouseEntered(mouseEvent);
		pane.setFocusTraversable(false);
		pane.getStyleClass().add("mouseOver");
		return pane;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;

		gridPane
		.getChildren()
		.parallelStream()
		.filter(n->n instanceof VBox)
		.map(n->(VBox)n)
		.filter(v->v.getChildren()!=null)
		.map(VBox::getChildren)
		.flatMap(ch->ch.parallelStream())
		.filter(ch->ch instanceof TextField)
		.filter(ch->!(ch instanceof ConfigurationGroup))
		.map(ch->(TextField)ch)
		.forEach(tf->Platform.runLater(()->tf.setEditable(editable)));
	}

	public void reset() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Stream<TextFieldRegister> controllers = getAllTextFieldControllers();
		controllers.forEach(controller->reset(controller));
	}

	private void reset(TextFieldRegister controller) {
		try { controller.reset(); } catch (Exception e) { logger.catching(e); }
	}

	public void save() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Stream<TextFieldRegister> controllers = getAllTextFieldControllers();
		controllers.forEach(controller->save(controller));
	}

	private void save(TextFieldRegister controller) {
		try { controller.save(); } catch (PacketParsingException e) { logger.catching(e); };
	}

	@SuppressWarnings("unchecked")
	private Stream<TextFieldRegister> getAllTextFieldControllers() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return (Stream<TextFieldRegister>) getAllControllersOf(TextFieldRegister.class);
	}

	private Stream<? extends ScheduledNode> getAllControllersOf(Class<? extends ScheduledNode> controllerClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		return getAllNodesOf(controllerClass).map(tf->controllerClass.cast(tf.getUserData()));
	}

	public Stream<VBox> getAllVBoxex() {
		return gridPane.getChildren()
				.parallelStream()
				.filter(ch->ch instanceof VBox)
				.map(ch->(VBox)ch);
	}

	private Stream<? extends Node> getAllNodesOf(Class<? extends ScheduledNode> controllerClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		final Field field = controllerClass.getDeclaredField("rootClass");
		@SuppressWarnings("unchecked")
		final Class<? extends Node> rootClass = (Class<? extends Node>)field.get(null);

		return getAllVBoxex()
				.map(vb->vb.getChildren())
				.flatMap(ch->ch.parallelStream())
				.filter(rootClass::isInstance)
				.filter(ps->controllerClass.isInstance(ps.getUserData()))
				.map(rootClass::cast);
	}

	private final EventHandler<ActionEvent> onActionMenuItemBackgrount = e->{

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

	public List<Map<String, Object>> getFieldsProperties(Class<? extends ScheduledNode> nodeClass) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{

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

	public List<Map<String, Object>> getOtherFieldsProperties() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		return getAllVBoxex()
				.map(vb->vb.getChildren())
				.flatMap(ch->ch.parallelStream())
				.filter(c->c.getUserData() instanceof OtherFields)
				.map(c->{
					Map<String, Object> map = new HashMap<>();
					final OtherFields otherFields = (OtherFields)c.getUserData();
					map.put("name", otherFields.getPropertyName());
					map.put("row", Optional.ofNullable(GridPane.getRowIndex(c.getParent())).orElse(0));
					map.put("column", Optional.ofNullable(GridPane.getColumnIndex(c.getParent())).orElse(0));
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

	private void clear() {
		//Stop all scheduled tasks
		getAllVBoxex()
		.map(b->b.getChildren())
		.flatMap(c->c.parallelStream())
		.map(Node::getUserData)
		.filter(Objects::nonNull)
		.filter(ScheduledNode.class::isInstance)
		.map(ScheduledNode.class::cast)
		.forEach(sn->sn.stop(true));

		gridPane.getColumnConstraints().clear();
		gridPane.getRowConstraints().clear();
		gridPane.getChildren().clear();
	}

	public void setColumnsAndRows(Integer columns, Integer rows) {
		clear();

		IntStream
		.range(0, columns)
		.forEach(count->addColumn(count));

		IntStream
		.range(0, rows)
		.forEach(count->addRow(count));
	}

	public Node setNode(Class<? extends ScheduledNode> fieldClass, String name, int column, int row) throws IOException {

		Node node = null;

		final Optional<VBox> vBoxAt = getVBoxAt(column, row);
		if(vBoxAt.isPresent())
			node = loadNode(fieldClass, name, vBoxAt.get().getChildren());

		return node;
	}

	public Optional<VBox> getVBoxAt(int column, int row) {
		return gridPane
				.getChildren()
				.parallelStream()
				.filter(VBox.class::isInstance)
				.filter(child->Optional.ofNullable(GridPane.getColumnIndex(child)).orElse(0)==column)
				.filter(child->Optional.ofNullable(GridPane.getRowIndex(child)).orElse(0)==row)
				.map(VBox.class::cast)
				.findAny();
	}

	public void setAlignment(Pos pos, int column, int row) {
		getVBoxAt(column, row)
		.ifPresent(box->box.setAlignment(pos));
	}

	private Node loadNode(final Class<? extends ScheduledNode> controllerClass, String name, ObservableList<Node> children) {
		try {

			final Field field = controllerClass.getField("FXML_PATH");
			final String fxml = (String) field.get(null);

			final URL resource = getClass().getResource(fxml);
			FXMLLoader loader = new FXMLLoader( resource, bundle);
			Node node = loader.load();
			Platform.runLater(()->children.add(node));

			if(focusListener!=null)
				addFocusListener(node);


			final ScheduledNode controller = loader.getController();
			controller.setKeyStartWith(name);

			if(controllerClass.getSuperclass().equals(TextFieldAbstract.class))
				Platform.runLater(()->((TextFieldAbstract)controller).getTextField().setEditable(editable));

			return node;

		} catch (Exception e) {
			logger.catching(e);
			return null;
		}
	}

	private Node loadNode(String keyStartWith, ObservableList<Node> children) {

		final String fxml = IrtGuiProperties.getProperty(keyStartWith + "fxml");

		try {

			final URL resource = getClass().getResource(fxml);
			FXMLLoader loader = new FXMLLoader( resource, bundle);
			Node node = loader.load();
			Platform.runLater(()->children.add(node));
			((FieldController)node.getUserData()).doUpdate(true);

			return node;

		} catch (Exception e) {
			logger.catching(e);
			return null;
		}
	}

	private void addFocusListener(Node node) {
		if(node.getUserData() instanceof TextFieldAbstract)
			((TextFieldAbstract)node.getUserData()).addFocusListener(focusListener);
	}

	@Override
	public void doUpdate(boolean doUpdate) {

			getAllFieldControllers()
			.forEach(fc->fc.doUpdate(doUpdate));
	}

	private Stream<FieldController> getAllFieldControllers() {
		return getAllVBoxex()
				.map(vb->vb.getChildren())
				.flatMap(ch->ch.parallelStream())
				.map(ps->ps.getUserData())
				.filter(ps->FieldController.class.isInstance(ps))
				.map(FieldController.class::cast);
	}
}
