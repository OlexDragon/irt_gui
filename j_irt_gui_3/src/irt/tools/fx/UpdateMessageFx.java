package irt.tools.fx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.file.FileScanner;
import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.data.profile.Profile;
import irt.irt_gui.IrtGui;
import irt.tools.fx.UpdateMessageFx.Message;
import irt.tools.panel.head.IrtPanel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class UpdateMessageFx extends Dialog<Message>{

	private final Logger logger = LogManager.getLogger();

	private final TextField tfAddress;

	private String ipAddressStr;

	private CheckBox cbPackage;
	private CheckBox cbProfile;
	private CheckBox cbProgram;

	private Label lblPackage;
	private Label lblProfile;
	private Label lblProgram;

	private ChangeListener<? super String> textListener;
	private ChangeListener<? super Boolean> cbListener;
	
	private FileScanner fileScanner;

	private Button updateButton;

	private RadioButton cbBUC;

	private RadioButton cbConv;

	// ******************************* constructor UpdateMessageFx   ***************************************************
	public UpdateMessageFx(DeviceInfo deviceInfo, boolean isProduction) {

		setTitle("IP Address");
		setHeaderText("Type a valid IP address.");
		getDialogPane().getStylesheets().add(getClass().getResource("fx.css").toExternalForm());

		final ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		// Update button

		updateButton = (Button) getDialogPane().lookupButton(updateButtonType);
		updateButton.setDisable(true);

		// Cancel button

		final Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
		Optional.ofNullable(fileScanner).ifPresent(fs->cancelButton.setOnAction(e->fs.cancel(true)));

		GridPane grid = new GridPane();
		grid.setMinWidth(400);
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(20);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(40);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(40);
		grid.getColumnConstraints().addAll(col1,col2,col3);
		grid.setHgap(10);
		grid.setVgap(10);

		//IP Address row #0

		tfAddress = new TextField();
		cbListener = (o, oV, nV)->enableUpdateButton(o);
		tfAddress.textProperty().addListener( textListener = (o, oV, nV)->enableUpdateButton(o) );
		grid.addRow(0, new Label("IP Address:"), tfAddress);

		tfAddress.setPromptText("192.168.0.1");
		tfAddress.textProperty().addListener(getListener(updateButton));

		//Package selection row #1

		cbPackage = new CheckBox("Package:");
		cbPackage.setDisable(true);
		cbPackage.selectedProperty().addListener(cbListener);
		lblPackage = new Label();
		StringProperty textProperty = lblPackage.textProperty();
		textProperty.addListener(textListener);
		textProperty.addListener(enableCheckBox(cbPackage));
		Button btnPackageSelection = new Button("Package Selection");
		btnPackageSelection.setMaxWidth(Double.MAX_VALUE);
		btnPackageSelection.setOnAction(selectPackage());

		grid.addRow(1, cbPackage, lblPackage, btnPackageSelection);

		if(!isProduction){

			// if the "Main-Class" of manifest.mf is "irt.irt_gui.IrtGui", this means that
			// this is production GUI
			try {
				Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
				while (resources.hasMoreElements()) {
					Manifest manifest = new Manifest(resources.nextElement().openStream());

					final Attributes.Name key = new Attributes.Name("Main-Class");
					isProduction = manifest
										.getMainAttributes()
										.entrySet()
										.stream()
										.filter(es->es.getKey().equals(key))
										.map(es->es.getValue())
										.map(v->v.equals("irt.irt_gui.IrtGui"))
										.findAny()
										.orElse(false);
					if(isProduction)
						break;
				}

				if(!isProduction){
					
					final String property = System.getProperty("sun.java.command");
					isProduction = "irt.irt_gui.IrtGui".equals(property);
				}

			} catch (IOException e) {
				logger.catching(e);
			}
		}

		if(isProduction){
			createProductionFields(grid);

			// Search profile by the unit serial number on the drive Z:
			new MyThreadFactory().newThread(()->{

				deviceInfo.getSerialNumber().map(sn->sn + ".bin").ifPresent(f->{
					
					try {

						fileScanner = new FileScanner( Paths.get(IrtPanel.PROPERTIES.getProperty("path_to_profiles")), f);
						final List<Path> paths = fileScanner.get(10, TimeUnit.SECONDS);

						if(paths.size()!=1)
							return;

						final Path path = paths.get(0);
						setLabelText(lblProfile, cbProfile, path);

					} catch (CancellationException e) {
						logger.info("fileScaner has been canceled.");

					} catch (Exception e) {
						logger.catching(e);
					}
				});
			})
			.start();
		}

		getDialogPane().setContent(grid);

		setResultConverter(button->{

			if(button == updateButtonType)
				return getMessage();

			return null;
		});
	}

	private Message getMessage() {
		final Message message = new Message(tfAddress.getText());

		if(cbPackage.isSelected()){
			message.put(PacketFormats.PACKAGE, lblPackage.getTooltip().getText());
			return message;
		}

		if(cbProfile.isSelected())
			message.put(PacketFormats.PROFILE, lblProfile.getTooltip().getText());

		if(cbProgram.isSelected())
			message.put(PacketFormats.BINARY, lblProgram.getTooltip().getText());

		return message;
	}

	private ChangeListener<? super String> enableCheckBox(CheckBox checkBox) {

		return (o, oV, nV)->checkBox.setDisable(!Optional.ofNullable(nV).filter(str->!str.isEmpty()).isPresent());
	}

	private void createProductionFields(GridPane grid) {
		final Separator separator = new Separator();
		grid.add(separator, 0, 2, 3, 1);

		//Profile row #1

		cbProfile = new CheckBox("Profile:");
		cbProfile.setDisable(true);
		cbProfile.selectedProperty().addListener(cbListener);
		lblProfile = new Label();
		StringProperty textProperty = lblProfile.textProperty();
		textProperty.addListener(textListener);
		textProperty.addListener(enableCheckBox(cbProfile));
		final ChangeListener<? super String> listener = (o,oV,nV)->{

			if(lblProfile.getContextMenu()!=null)
				return;

			ContextMenu contextMenu = new ContextMenu();
			MenuItem menuItem = new MenuItem("Edit");
			menuItem.setOnAction(e-> {
				try {
					Desktop.getDesktop().open(new File(lblProfile.getTooltip().getText()));
				} catch (IOException e1) {
					logger.catching(e1);
				}
			});
			contextMenu.getItems().add(menuItem);
			lblProfile.setContextMenu(contextMenu);

		};
		textProperty.addListener(listener);
		Button btnOrofileSelection = new Button("Profile Selection");
		btnOrofileSelection.setMaxWidth(Double.MAX_VALUE);
		btnOrofileSelection.setOnAction(selectProfile());

		grid.addRow(3, cbProfile, lblProfile, btnOrofileSelection);

		//Program row #2

		cbProgram = new CheckBox("Program:");
		cbProgram.setDisable(true);
		cbProgram.selectedProperty().addListener(cbListener);
		lblProgram = new Label();
		textProperty = lblProgram.textProperty();
		textProperty.addListener(textListener);
		textProperty.addListener(enableCheckBox(cbProgram));
		Button btnProgramSelection = new Button("Program Selection");
		btnProgramSelection.setMaxWidth(Double.MAX_VALUE);
		btnProgramSelection.setOnAction(selectProgram());

		grid.addRow(4, cbProgram, lblProgram, btnProgramSelection);

		ImageView imageView = new ImageView();
		final Image imageBuc = new Image(IrtGui.class.getResourceAsStream("images/AnrBUC.png"));
		final Image imageConv = new Image(IrtGui.class.getResourceAsStream("images/converter.png"));

		imageView.setImage(imageBuc);
		grid.add(imageView, 1, 5);

		VBox vBox = new VBox();

		final ToggleGroup group = new ToggleGroup();
		final EventHandler<ActionEvent> onAction = e->{
			if(e.getSource()==cbBUC)
				imageView.setImage(imageBuc);
			else
				imageView.setImage(imageConv);
		};

		cbBUC = new RadioButton("BUC");
		cbBUC.setSelected(true);
		cbBUC.setToggleGroup(group);
		cbBUC.setOnAction(onAction);

		cbConv = new RadioButton("Converter");
		cbConv.setToggleGroup(group);
		cbConv.setOnAction(onAction);

		vBox.getChildren().add(cbBUC);
		vBox.getChildren().add(cbConv);
		grid.add(vBox, 0, 5);
	}

	private EventHandler<ActionEvent> selectProfile() {
		return e->{

			FileChooser fileChooser = getFileChooser(lblProfile, "*.bin");

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				fileScanner.cancel(true);	// Cancel fileScaneron before the new path setting
				setLabelText(lblProfile, cbProfile, result.toPath());
//				getDialogPane().getScene().getWindow().sizeToScene();
			}
		};
	}

	private EventHandler<ActionEvent> selectProgram() {
		return e->{

			FileChooser fileChooser = getFileChooser(lblProgram, "*.bin");

			final File file = Paths.get("Z:", "4alex", "boards", "SW release", "latest").toFile();
			if(file.exists() && file.isDirectory())
				fileChooser.setInitialDirectory(file);

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				setLabelText(lblProgram, cbProgram, result.toPath());
//				getDialogPane().getScene().getWindow().sizeToScene();
			}
		};
	}

	private FileChooser getFileChooser(Label label, String ext) {
		FileChooser fileChooser = new FileChooser();
		final ObservableList<ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
		extensionFilters.add(new ExtensionFilter("IRT Technologies BIN file", ext));
		extensionFilters.add(new ExtensionFilter("All", "*.*"));

		Optional.ofNullable(label.getTooltip()).map(tt->tt.getText()).map(File::new).ifPresent(file->{

			fileChooser.setInitialDirectory(file.getParentFile());
			fileChooser.setInitialFileName(file.getName());
		});

		return fileChooser;
	}

	private void enableUpdateButton(final ObservableValue<?> observableValue) {

		final Optional<CheckBox> oIsSelected = Optional.of(observableValue)
														.filter(BooleanProperty.class::isInstance)
														.map(BooleanProperty.class::cast)
														.map(BooleanProperty::getBean)
														.map(CheckBox.class::cast)
														.filter(CheckBox::isSelected);

		Optional.ofNullable(fileScanner)
		.ifPresent(fs->{

			if(oIsSelected.map(cb->cb==cbPackage).orElse(false)){

				fs.cancel(true);
				cbProfile.setSelected(false);
				cbProgram.setSelected(false);
			}
		});


		if(oIsSelected.map(cb->cb!=cbPackage).orElse(false)){

			cbPackage.setSelected(false);
		}

		final boolean disable = !validateNodes();
		Platform.runLater(()->updateButton.setDisable(disable));
	}

	private boolean validateNodes() {

		// IP Address
		boolean ipAddress = Optional
						.ofNullable(tfAddress.getText())
						.map(String::trim)
						.map(value->!value.isEmpty())
						.orElse(false);

		if(!ipAddress)
			return false;

		// Package
		// if is address and package return true
		if(validate(lblPackage.getText(), cbPackage))
			return true;

		// When none production mode 'lblProfile' and 'lblProgram' equal null.
		return Optional.ofNullable(lblProfile).map(lbl->validate(lbl.getText(), cbProfile)).orElse(false)
				|| Optional.ofNullable(lblProgram).map(lbl->validate(lbl.getText(), cbProgram)).orElse(false);
	}

	private boolean validate(String text, CheckBox cb){

		return Optional.ofNullable(text)
				.filter(value->!value.isEmpty())
				.map(v->cb.isSelected())
				.orElse(false);
	}

	private void setLabelText(Label label, CheckBox checkBox, final Path path) {

		Platform.runLater(()->{

			label.setTooltip(new Tooltip(path.toString()));
			label.setText(path.getFileName().toString());
			checkBox.setSelected(true);
		});
	}

	private EventHandler<ActionEvent> selectPackage() {
		return e->{

			FileChooser fileChooser = getFileChooser(lblPackage, "*.pkg");

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				setLabelText(lblPackage, cbPackage, result.toPath());
//				getDialogPane().getScene().getWindow().sizeToScene();
			}
		};
	}

	private ChangeListener<? super String> getListener(final Node button) {
		return (o, oV, nV)->{

			final List<String> addr = Optional.ofNullable(nV).filter(a->!a.isEmpty()).map(a->a.split("\\D")).map(Arrays::stream).orElse(Stream.empty()).filter(s->!s.isEmpty()).collect(Collectors.toList());

			if(addr.size()!=4){
				final boolean disable = !validateNodes();
				button.setDisable(disable);
				return;
			}

			ipAddressStr = addr.stream().collect(Collectors.joining("."));
			button.setDisable(false);
		};
	}

	public void setIpAddress(String addrStr) {
		tfAddress.setText(addrStr);
	}

	public String getIpAddress() {
		return ipAddressStr;
	}

	//****************** class Message *****************************
	public class Message{

		private final Map<PacketFormats, String> paths = new HashMap<>();
		private String address;

		public Message(String address) {
			this.address = address;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String put(PacketFormats packetFormats, String path) {
			return paths.put(packetFormats, path);
		}

		public Map<PacketFormats, String> eetPaths() {
			return paths;
		}

		public boolean isPackage(){
			return paths.get(PacketFormats.PACKAGE)!=null;
		}

		private String format = "%s{path{%s}}";
		@Override
		public String toString() {

			return paths
					.entrySet()
					.stream()
					.map(es->String.format(format, es.getKey().name().toLowerCase(), es.getValue()))
					.collect(Collectors.joining("\n"));
		}

		public String getPacksgePath() {
			return paths.get(PacketFormats.PACKAGE);
		}

		public final String setupInfoPathern = "%s any.any.any { %s { path {%s}} }";
		public String getSetupInfo() {

			return paths
					.entrySet()
					.stream()
					.map(es->new java.util.AbstractMap.SimpleEntry<String, String>(es.getKey().name().toLowerCase(), new File(es.getValue()).getName()))
					.map(es->String.format(setupInfoPathern, (cbBUC.isSelected() ? "system" : "256"), es.getKey(), es.getValue()))
					.collect(Collectors.joining("\n"));
		}

		public Optional<Profile> getProfile() {
			return Optional
					.ofNullable(paths.get(PacketFormats.PROFILE))
					.map(path -> {

						try {

							return new Profile(path);

						} catch (IOException e) {
							logger.catching(e);
						}

						return null;
					});
		}

		public Optional<ByteBuffer> getByteBuffer(PacketFormats packetFormat) {
			return Optional
					.ofNullable(paths.get(packetFormat))
					.map(path->{
						try(	RandomAccessFile 	raf = new RandomAccessFile(path, "r");
								FileChannel 		fileChannel 	= raf.getChannel()){

							return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

						} catch (Exception e) {
							logger.catching(e);
						}
						return null;
					});
		}

		public Optional<Path> getPath(PacketFormats packetFormat) {
			return Optional
					.ofNullable(paths.get(packetFormat))
					.map(path->Paths.get(path));
		}
	}

	public enum PacketFormats{
		PROFILE,
		BINARY,
		OEM,
		IMAGE,
		PACKAGE
	}
}
