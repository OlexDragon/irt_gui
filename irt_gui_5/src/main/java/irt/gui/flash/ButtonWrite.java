package irt.gui.flash;

import java.io.File;
import java.net.URL;
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
import irt.gui.data.MyThreadFactory;
import irt.gui.flash.PanelFlash.UnitAddress;
import irt.gui.flash.service.EraseObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
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
	private File file;
	private EraseObject setEraseObject;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
	}

	@FXML private void onAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("IRT Technologies BIN file", "*.bin"));
		final String key = unitAddress.name() + "_file";
		final String path = prefs.get(key, null);
		if(path!=null){
			File p = new File(path);
			fileChooser.setInitialDirectory(p.getParentFile());
			fileChooser.setInitialFileName(p.getName());
		}
		fileChooser.setTitle(bundle.getString("write.to_flash"));
		Optional
		.ofNullable(fileChooser.showOpenDialog(button.getScene().getWindow()))
		.ifPresent(f->{
			file = f;
			prefs.put(key, f.getAbsolutePath());
			button.setTooltip(new Tooltip(f.getName()));

			executor.execute(()->{
				if(setEraseObject.erase(file.length()))
					Platform.runLater(() -> {
								try {
									FXMLLoader loader = new FXMLLoader(
											getClass().getResource("/fxml/flash/PanelWriteFlash.fxml"));
									Parent root = (Parent) loader.load();

									PanelWriteFlash writeFlash = (PanelWriteFlash) loader.getController();

									Stage stage = new Stage();
									writeFlash.write(unitAddress, file);

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

	@Override public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setTooltip(new Tooltip(unitAddress.toString()));
		}
	}

	public void setEraseObject(EraseObject eraseObject) {
		this.setEraseObject = eraseObject;
	}
}
