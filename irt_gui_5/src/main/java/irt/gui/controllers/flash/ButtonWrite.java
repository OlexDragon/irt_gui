package irt.gui.controllers.flash;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.flash.enums.UnitAddress;
import irt.gui.controllers.flash.service.EraseObject;
import irt.gui.data.MyThreadFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ButtonWrite extends Observable implements Observer, Initializable {
	private Logger logger = LogManager.getLogger();

	@FXML private Button button;

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private ResourceBundle bundle;
	private UnitAddress unitAddress;
	private File file; 			File getFile() { return file; } 			void setFile(File file) { this.file = file;	button.setTooltip(new Tooltip(file.getName())); }

	private EraseObject setEraseObject;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	private String text;

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
	}

	@FXML private void onAction() {

		final String key = unitAddress.name() + "_file";
		final String path = prefs.get(key, null);
		File toWrite;

		if (file == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("IRT Technologies BIN file", "*.bin"));
			if (path != null) {
				File p = new File(path);
				fileChooser.setInitialDirectory(p.getParentFile());
				fileChooser.setInitialFileName(p.getName());
			}
			fileChooser.setTitle(bundle.getString("write.to_flash"));

			toWrite = fileChooser.showOpenDialog(button.getScene().getWindow());

		} else{
			if(saveToFile())
				toWrite = file;	// if selected ButtonType.OK or ButtonType.NO
			else
				toWrite = null;	// if selected ButtonType.CANSEL
		}

		Optional
		.ofNullable(toWrite)
		.ifPresent(f->{
			prefs.put(key, f.getAbsolutePath());
			button.setTooltip(new Tooltip(f.getName()));

			executor.execute(()->{
				if(setEraseObject.erase(f.length()))
					Platform.runLater(() -> {
								try {
									FXMLLoader loader = new FXMLLoader(
											getClass().getResource("/fxml/flash/PanelWriteFlash.fxml"));
									Parent root = (Parent) loader.load();

									PanelWriteFlash writeFlash = (PanelWriteFlash) loader.getController();

									Stage stage = new Stage();
									writeFlash.write(unitAddress, f);

									stage.setScene(new Scene(root));
									stage.setTitle("Data input");
									stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.gif")));
									stage.initModality(Modality.APPLICATION_MODAL);
									stage.initOwner(button.getScene().getWindow());

									stage.showAndWait();
								} catch (Exception e) {
									logger.catching(e);
								}
							});
			});
		});
	}

	private boolean saveToFile() {
		ButtonType buttonType = ButtonType.YES;
		final Optional<String> ofNullable = Optional
				.ofNullable(text);
		if(ofNullable.isPresent()){
			buttonType = showAlert();
			if(buttonType==ButtonType.YES){
				saveLocalCopy();
				moveOriginal();

				try(PrintWriter out = new PrintWriter(file)){

					out.println( text );

				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		}
		return buttonType != ButtonType.CANCEL;
	}

	private ButtonType showAlert() {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to save changes?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		alert.setTitle("The profile has been modified");
		return alert
				.showAndWait()
				.get();
	}

	private void saveLocalCopy() {

		if(file.exists()){
			try{
				String absolutePath = file.getAbsolutePath();
				final String str = "profile";
				final int indexOf = absolutePath.indexOf(str);

				if (indexOf > 0)
					absolutePath = Paths.get(System.getProperty("user.home"), "irt", "profile_backup", absolutePath.substring(indexOf + str.length())).toString();
				else
					absolutePath = Paths.get(System.getProperty("user.home"), "irt", "profile_backup", absolutePath.replace(file.toPath().getRoot().toString(), "")).toString();


				final File dest = new File(absolutePath + "." + System.currentTimeMillis());

				File folder = dest.getParentFile();
				if (!folder.exists())
					folder.mkdirs();

				Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);

			} catch (Exception ex) {
				logger.catching(ex);
			}
		}
	}

	private void moveOriginal() {

		if(file.exists()){
			try {
				// copy existing file
				String absolutePath = file.getAbsolutePath();
				final String str = "profile";
				final int indexOf = absolutePath.indexOf(str);

				if (indexOf > 0)
					absolutePath = Paths.get(file.toPath().getRoot().toString(), "profile_backup", absolutePath.substring(indexOf + str.length())).toString();


				final File dest = new File(absolutePath + "." + System.currentTimeMillis());

				File folder = dest.getParentFile();
				if (!folder.exists())
					folder.mkdirs();

				Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				file.delete();

			} catch (Exception ex) {
				logger.catching(ex);
			}
		}
	}

	@Override public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setTooltip(new Tooltip(unitAddress.toString()));
		}
	}

	public void setEraseObject(EraseObject eraseObject) {
		this.setEraseObject = eraseObject;
	}

	public void setText(String text) {
		this.text = text;
	}
}
