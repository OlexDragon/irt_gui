package irt.gui.controllers.flash;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.flash.enums.UnitAddress;
import irt.gui.controllers.flash.service.FindTheFile;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ButtonLinkToFile implements Initializable, Observer {

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	static FindTheFile findTheFile;

	@FXML private Button button;

	private ResourceBundle bundle;

	private UnitAddress unitAddress;

	private ButtonWrite writeButton;

	private MenuItem menuItemEditProfile;

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		findTheFile = new FindTheFile(this);
	}

	@FXML public void onAction() {
		showFileChooser();
	}

	@FXML void onActionSelectMainDirectory(){

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select profiles directory");
		File directory = findTheFile.getDefaultFolder().toFile();
		chooser.setInitialDirectory(directory);
		final File file = chooser.showDialog(button.getScene().getWindow());

		Optional
		.ofNullable(file)
		.ifPresent(f->findTheFile.setDefaultFolder(f.toPath()));
	}

	public void linck(List<Path> foundFiles) {
		switch(foundFiles.size()){
		case 0:// No files
			button.setText(bundle.getString("link.to.file"));
			break;
		case 1:
			final File f = foundFiles.get(0).toFile();
			set(unitAddress.name() + "_file", f);
			break;
		default:// More then 1 file
			Platform.runLater(()->{
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Found more than one file");
				alert.setHeaderText("Choose one of them");

				final DialogPane dialogPane = alert.getDialogPane();
				final ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
				buttonTypes.clear();	//Remove 'OK' button
				buttonTypes.add(ButtonType.CANCEL);
				VBox vb = new VBox();
				dialogPane.setContent(vb);
				foundFiles
				.stream()
				.forEach(path->{
					Button b = new Button();
					final String text = path.toString();
					b.setText(text);
					b.setTooltip(new Tooltip(text));
					b.setUserData(path);
					vb.getChildren().add(b);
					b.setOnAction(e->{
						final Node source = (Node)e.getSource();
						Path p = (Path)source.getUserData();
						set(unitAddress.name() + "_file", p.toFile());
						alert.close();
					});
				});

				alert.show();
			});
		}
	}

	private void showFileChooser(){
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
		.ifPresent(f->set(key, f));
	}

	private void set(final String key, File f) {
		prefs.put(key, f.getAbsolutePath());
		final String fileName = f.getName();
		writeButton.setFile(f);
		findTheFile.setfileName(fileName);
		Platform.runLater(()->{
			button.setText(fileName);
			button.setTooltip(new Tooltip(fileName));
			menuItemEditProfile.setDisable(false);
		});
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setText(bundle.getString("link.to.file"));
		}
	}

	public void setWriteButton(ButtonWrite writeButton) {
		this.writeButton = writeButton;
	}

	public void setMenuItemEdit(MenuItem menuItemEdit) {
		this.menuItemEditProfile = menuItemEdit;
	}
}
