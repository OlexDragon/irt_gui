package irt.tools.fx.update;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.file.ConverterProfileScanner;
import irt.controller.file.ProfileScannerFT;
import irt.data.DeviceInfo;
import irt.data.HardwareType;
import irt.data.ThreadWorker;
import irt.irt_gui.IrtGui;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.fx.update.UpdateMessageFx.Message;
import irt.tools.fx.update.profile.Profile;
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
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;

public class UpdateMessageFx extends Dialog<Message>{

	public static final ButtonData UPDATE_BUTTON = ButtonData.OK_DONE;
	public static final ButtonData TO_PACKAGE_BUTTON = ButtonData.OTHER;

	private final Logger logger = LogManager.getLogger();

	private static final String SYSTEM = "system";
	private String serialNumber = "N/A";

	private static Path profilePath;

	private final TextField tfAddress;

	private String ipAddressStr;

	private CheckBox cbPackage;
	private CheckBox cbProfile;
	private CheckBox cbProgram;

	private Label lblPackage;
	private Label lblProfile;
	private Label lblProgram;

	private ProfileScannerFT findBucProfile;
	private ConverterProfileScanner findConvProfile;

	private DeviceInfo deviceInfo;

	private final ChangeListener<? super String> textListener = (o, oV, nV)->enableUpdateButton(o);
	private final ChangeListener<? super Boolean> cbListener = (o, oV, nV)->enableUpdateButton(o);
	private final ChangeListener<? super Boolean> cbUnitTypeSelectListener = (o,oV,nV)->Optional.of(nV)

			.filter(v->v)
			.ifPresent(
					v->{

						setSerialNumber(Optional.ofNullable(deviceInfo).flatMap(DeviceInfo::getSerialNumber).orElse("N/A"));

						findBucProfile.stop();
						findConvProfile.stop();

						final Node node = (Node)((BooleanProperty)o).getBean();
						FutureTask<?> findProfile = (FutureTask<?>) node.getUserData();

						if(findProfile==findBucProfile && profilePath!=null) {
							setLabelText(lblProfile, cbProfile, profilePath);
							return;
						}

						Platform.runLater(
								()->{
									lblProfile.setText("");
									final ObservableList<String> styleClass = lblProfile.getStyleClass();
									styleClass.add("YELLOW_BORDER");
									styleClass.remove("RED_BORDER");
								});

						new ThreadWorker(findProfile, "Utin Type Select Listener");
						new ThreadWorker(
								()->{

									try {

										final Optional<Path> oPath = ((Optional<?>) findProfile.get(5, TimeUnit.MINUTES)).map(Path.class::cast);
										oPath.ifPresent(
												path->{
													setProfileLabelText(path);
													final ObservableList<String> styleClass = lblProfile.getStyleClass();
													styleClass.remove("YELLOW_BORDER");
													styleClass.remove("RED_BORDER");
												});

										if(!oPath.isPresent()) {
											final ObservableList<String> styleClass = lblProfile.getStyleClass();
											styleClass.remove("YELLOW_BORDER");
											styleClass.add("RED_BORDER");
										}

									} catch (InterruptedException | ExecutionException | TimeoutException e) {

										logger.catching(e);
										lblProfile.setTooltip(new Tooltip(e.getLocalizedMessage()));

										final ObservableList<String> styleClass = lblProfile.getStyleClass();
										styleClass.remove("YELLOW_BORDER");
										styleClass.add("RED_BORDER");
									}
								}, "Set Label Text");
					});

	private void setProfileLabelText(final Path path) {
		final String[] split = path.getFileName().toString().split("\\.");

		if(split.length==0)
			return;

		setSerialNumber(split[0]);
		setLabelText(lblProfile, cbProfile, path);
	}

	private void setSerialNumber(final String newSerialNumber) {
		serialNumber = newSerialNumber;
		miChangeSerialNumber.setText("Change " + serialNumber);
	}
	
//	private FileScanner fileScanner;

	private Button updateButton;
	private Button toPkgButton;

	private RadioButton cbBUC;
	private RadioButton cbConv;

	private String system = SYSTEM;

	private MenuItem miChangeSerialNumber;

	// ***************************************************************************************************************** //
	// 																													 //
	// 									 constructor UpdateMessageFx													 //
	// 																													 //
	// ***************************************************************************************************************** //
	public UpdateMessageFx(DeviceInfo deviceInfo, boolean isProduction) {

		ThreadWorker.runThread(
				()->{
					try {

						URL url = new URL("http://op-2123100/update.cgi");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST"); // PUT is another valid option
						connection.setDoOutput(true);

						connection.setDoOutput(true);
						connection.setDoInput(true);
						connection.setUseCaches(false);
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Connection", "Keep-Alive");
						final String value = "IRT GUI" + IrtGui.VERTION + "; User: " + System.getProperty("user.name") + "; os.name: " + System.getProperty("os.name") + "; os.arch: " + System.getProperty("os.arch") + ";";
						connection.setRequestProperty("User-Agent", value);
						connection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");

					} catch (IOException e) {
						logger.catching(e);
					}
				}, "Test");

		this.deviceInfo = deviceInfo;
		miChangeSerialNumber = new MenuItem("Change Serial Number");

		Thread currentThread = Thread.currentThread();
		currentThread.setName(getClass().getSimpleName() + "-" + currentThread.getId());

		setTitle("IP Address");
		setHeaderText("Type a valid IP address.");
		final DialogPane dialogPane = getDialogPane();
		dialogPane.getStylesheets().add(MonitorPanelFx.class.getResource("fx.css").toExternalForm());

		final ButtonType updateButtonType = new ButtonType("Update", UPDATE_BUTTON);
		ButtonType toPkgButtonType = null;

		ButtonType[] buttonTypes;
		if(isProduction) {
			toPkgButtonType = new ButtonType("To Package", TO_PACKAGE_BUTTON);
			buttonTypes = new ButtonType[] {toPkgButtonType, updateButtonType, ButtonType.CANCEL};

		}else
			buttonTypes = new ButtonType[] {updateButtonType, ButtonType.CANCEL};

		dialogPane.getButtonTypes().addAll(buttonTypes);

		// To Package Button
		Optional.ofNullable(toPkgButtonType)
		.ifPresent(
				bt->{

					toPkgButton = (Button) dialogPane.lookupButton(bt);
					toPkgButton.setDisable(true);
				});

		// Update button

		updateButton = (Button) dialogPane.lookupButton(updateButtonType);
		updateButton.setDisable(true);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		//IP Address row #0

		tfAddress = new TextField();
		deviceInfo.getSerialNumber().ifPresent(
				sn->{
					tfAddress.setText(sn);
					setSerialNumber(sn);
				});
		tfAddress.textProperty().addListener( textListener );
		grid.addRow(0, new Label("IP Address:"), tfAddress);

		tfAddress.setPromptText("192.168.0.1");

		tfAddress.textProperty()
		.addListener(
				(o, oV, nV)->{

					final boolean disable = !validateNodes();
					updateButton.setDisable(disable);
					Optional.ofNullable(toPkgButton).ifPresent(b->b.setDisable(disable));
				});

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

		if(isProduction){

			// Search profile by the unit serial number on the drive Z:
			findBucProfile = new ProfileScannerFT(deviceInfo);			
			findConvProfile = new ConverterProfileScanner(deviceInfo.getLinkHeader().getAddr());
			createProductionFields(grid);
		}

		dialogPane.setContent(grid);

		// 
		setResultConverter(

				buttonType->{

					if(buttonType != ButtonType.CANCEL)
						return getMessage(buttonType);

					return null;
				});
	}

	private Message getMessage(ButtonType buttonType) {
		final Message message = new Message(tfAddress.getText());
		message.setButtonType(buttonType);

		if(cbPackage.isSelected()){
			message.put(PacketFormats.PACKAGE, lblPackage.getTooltip().getText());
			return message;
		}

		if(cbProfile.isSelected())
			message.put(PacketFormats.PROFILE, lblProfile.getTooltip().getText());

		if(cbProgram.isSelected())
			message.put(PacketFormats.IMAGE, lblProgram.getTooltip().getText());

		return message;
	}

	private ChangeListener<? super String> enableCheckBox(CheckBox checkBox) {

		return (o, oV, nV)->{
			boolean value = !Optional.ofNullable(nV).filter(str->!str.isEmpty()).isPresent();
			checkBox.setDisable(value);
			if(value)
				checkBox.setSelected(false);
		};
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
			menuItem.setOnAction(
					e-> {
						try {
							Desktop.getDesktop().open(new File(lblProfile.getTooltip().getText()));
						} catch (IOException e1) {
							logger.catching(e1);
						}
					});
			final ObservableList<MenuItem> menuItems = contextMenu.getItems();
			menuItems.add(menuItem);

			menuItem = new MenuItem("Open file location");
			menuItem.setOnAction(
					e-> {
						try {
							Runtime.getRuntime().exec("explorer.exe /select," + lblProfile.getTooltip().getText());
						} catch (IOException e1) {
							logger.catching(e1);
						}
					});
			menuItems.add(menuItem);

			miChangeSerialNumber.setOnAction(
					e->Platform.runLater(
							()->{

								TextInputDialog dialog = new TextInputDialog(serialNumber);
								dialog.setTitle("Serial Number");
								dialog.setHeaderText("Actual Module or Converter Serial Number.");
								dialog.setContentText("Please enter Serial Number:");

								dialog.showAndWait().ifPresent(this::setSerialNumber);
							}));
			menuItems.add(miChangeSerialNumber);
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

		// Get Program Path fom the properties file.
		final String type = deviceInfo.getTypeId()+"."+deviceInfo.getRevision();
		final String key = type + ".path";

		try {
			final Object prop = IrtGui.loadFlash3Properties().get(key);
			Optional.ofNullable(prop).map(Object::toString).map(File::new).filter(File::exists)
			.ifPresent(
					file->{
						lblProgram.setTooltip(new Tooltip(file.getAbsolutePath()));
						lblProgram.setText(file.getName());
					});
		} catch (IOException e1) {
			logger.catching(e1);
		}

		grid.addRow(4, cbProgram, lblProgram, btnProgramSelection);

		ImageView imageView = new ImageView();
		final Image imageBuc = new Image(IrtGui.class.getResourceAsStream("images/AnrBUC.png"));
		final Image imageConv = new Image(IrtGui.class.getResourceAsStream("images/converter.png"));

		imageView.setImage(imageBuc);
		grid.add(imageView, 1, 5);

		VBox vBox = new VBox();

		final ToggleGroup group = new ToggleGroup();

		final EventHandler<ActionEvent> onSelectTypeAction =
				e->{

					if(e.getSource()==cbBUC){

						// Unit

						imageView.setImage(imageBuc);
						system = SYSTEM;
						deviceInfo.getSerialNumber().ifPresent(this::setSerialNumber);

					}else{

						// Converter

						imageView.setImage(imageConv);
						if(deviceInfo.getDeviceType().map(dt->dt.HARDWARE_TYPE).map(ht->ht==HardwareType.CONTROLLER).orElse(false) || deviceInfo.getRevision()>10)
							system = "file";
						else
							system = "256";
					}
				};

		cbBUC = new RadioButton("BUC");
		cbBUC.setToggleGroup(group);
		cbBUC.setOnAction(onSelectTypeAction);
		cbBUC.setUserData(findBucProfile);
		cbBUC.selectedProperty().addListener(cbUnitTypeSelectListener);
		cbBUC.setSelected(true);

		cbConv = new RadioButton("Converter");
		cbConv.setToggleGroup(group);
		cbConv.setOnAction(onSelectTypeAction);
		cbConv.setUserData(findConvProfile);
		cbConv.selectedProperty().addListener(cbUnitTypeSelectListener);

		vBox.getChildren().add(cbBUC);
		vBox.getChildren().add(cbConv);
		grid.add(vBox, 0, 5);
	}

	protected static Preferences prefs = Preferences.userRoot().node(GuiControllerAbstract.IRT_TECHNOLOGIES_INC);
	private EventHandler<ActionEvent> selectProfile() {
		return e->{

			FileChooser fileChooser = getFileChooser(lblProfile, "*.bin");

			final String key = "selectProfile";
			initialDirectory(key, fileChooser, lblProfile);

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
//				if(fileScanner!=null)
//					fileScanner.cancel(true);	// Cancel fileScaneron before the new path setting
//				logger.error(fileScanner);
				final Path path = result.toPath();
				setLabelText(lblProfile, cbProfile, path);
				prefs.put(key, result.getAbsolutePath());
//				getDialogPane().getScene().getWindow().sizeToScene();
			}
		};
	}

	private EventHandler<ActionEvent> selectProgram() {
		return e->{

			FileChooser fileChooser = getFileChooser(lblProgram, "*.bin");

			final String key = "selectProgram";
			initialDirectory(key, fileChooser, lblProgram);

			final File file = Paths.get("Z:", "4alex", "boards", "SW release", "latest").toFile();
			if(file.exists() && file.isDirectory())
				fileChooser.setInitialDirectory(file);

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				setLabelText(lblProgram, cbProgram, result.toPath());
				prefs.put(key, result.getAbsolutePath());
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


		final Boolean isPackage = oIsSelected.map(cb->cb==cbPackage).orElse(false);
		if(isPackage){

			cbProfile.setSelected(false);
			cbProgram.setSelected(false);
		}


		if(oIsSelected.map(cb->cb!=cbPackage).orElse(false)){

			cbPackage.setSelected(false);
		}

		final boolean disable = !validateNodes();
		Platform.runLater(
				()->{
					updateButton.setDisable(disable);
					Optional.ofNullable(toPkgButton).ifPresent(b->b.setDisable(isPackage? true : disable));
				});
	}

	/** Validate IP Address and Path selection */
	private boolean validateNodes() {

		// validate IP Address
		boolean ipAddress = Optional
						.ofNullable(tfAddress.getText())
						.map(String::trim)
						.map(value->!value.isEmpty())
						.orElse(false);

		if(!ipAddress)
			return false;

		// validate Package
		// return true if address and package exist,
		if(validatePath(lblPackage, cbPackage))
			return true;

		// When none production mode 'lblProfile' and 'lblProgram' equal null.
		return Optional.ofNullable(lblProfile).map(lbl->validatePath(lbl, cbProfile)).orElse(false)
				|| Optional.ofNullable(lblProgram).map(lbl->validatePath(lbl, cbProgram)).orElse(false);
	}

	/**
	 * @param label
	 * @param checkBox
	 * @return true if the label contains a path and the CheckBox is checked, otherwise false
	 */
	private boolean validatePath(Label label, CheckBox checkBox){

		String text = label.getText();
		return Optional.ofNullable(text)
				.filter(value->!value.isEmpty())
				.map(v->checkBox.isSelected())
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

			final String key = "selectPackage";
			initialDirectory(key, fileChooser, lblPackage);

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				setLabelText(lblPackage, cbPackage, result.toPath());
				prefs.put(key, result.getAbsolutePath());
			}
		};
	}

	private void initialDirectory(final String key, FileChooser fileChooser, Label label) {

		final File directory = Optional.ofNullable(label.getTooltip()).map(Tooltip::getText).map(File::new).map(File::getParentFile)

				.orElseGet(()->Optional.ofNullable(prefs.get(key, null)).map(File::new).map(File::getParentFile).orElse(null));

		logger.debug(directory);

		Optional.ofNullable(directory).ifPresent(fileChooser::setInitialDirectory);
	}

	public void setIpAddress(String addrStr) {
		Optional.ofNullable(addrStr).map(String::trim).filter(a->!a.isEmpty()).ifPresent(tfAddress::setText);
	}

	public String getIpAddress() {
		return ipAddressStr;
	}

	//****************** class Message *****************************
	public class Message{

		private final Map<PacketFormats, String> paths = new HashMap<>();
		private String ipAddress;
		private ButtonType buttonType;

		public Message(String address) {
			this.ipAddress = address;
		}

		public ButtonType getButtonType() {
			return buttonType;
		}

		public void setButtonType(ButtonType buttonType) {
			this.buttonType = buttonType;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public void setIpAddress(String address) {
			ipAddress = address;
		}

		public String put(PacketFormats packetFormats, String path) {
			return paths.put(packetFormats, path);
		}

		public Map<PacketFormats, String> getPaths() {
			return paths;
		}

		public boolean isPackage(){
			return paths.get(PacketFormats.PACKAGE)!=null;
		}

		private final static String format = "%s{path{%s}}";
		@Override
		public String toString() {

			return paths
					.entrySet()
					.stream()
					.map(es->String.format(format, es.getKey().name().toLowerCase(), es.getValue()))
					.collect(Collectors.joining("\n"));
		}

		public String getPackagePath() {
			return paths.get(PacketFormats.PACKAGE);
		}

		public final static String setupInfoPathern = "%s any.any.any.%s {%s}";
		public final static String pathPathern = "%s { path {%s} %s }";

		/**
		 * 
		 * @return Setup.info and its MD5
		 * @throws NoSuchAlgorithmException
		 * @throws UnsupportedEncodingException
		 */
		public Pair<String, String> getSetupInfo() throws NoSuchAlgorithmException, UnsupportedEncodingException {

			final String setupInfo = String
					.format(
							setupInfoPathern,
							system,
							serialNumber,
							paths
								.entrySet()
								.stream()
								.map(es->new SimpleEntry<String, String>(es.getKey().name().toLowerCase(), new File(es.getValue()).getName()))
								.map(es->String.format(pathPathern, es.getKey(), es.getValue(), getAddress(es)))
								.collect(Collectors.joining("\n")));

			final byte[] bytes = setupInfo.getBytes(Profile.charEncoding);

			return new Pair<>(

					setupInfo,
					DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytes)));
		}

		private String getAddress(SimpleEntry<String, String> es){

			if(cbBUC.isSelected())
				return "";

			// Converter firmware address
			final String value = es.getValue();
			if(PacketFormats.IMAGE.toString().equals(value))
				return "address {0x08000000}";

			// Converter profile address
			return "address {0x080c0000}";
		}

		public Optional<Profile> getProfile() {
			return Optional
					.ofNullable(paths.get(PacketFormats.PROFILE))
					.map(Paths::get)
					.map(Profile::new);
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

	public static void setProfilePath(Path path) {
		profilePath = path;
	}

	public enum PacketFormats{
		PROFILE,
		BINARY,
		OEM,
		IMAGE,
		PACKAGE
	}
}
