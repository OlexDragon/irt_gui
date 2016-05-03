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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
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
		default://TODO More then 1 file
			
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
		}
	}

	public void setWriteButton(ButtonWrite writeButton) {
		this.writeButton = writeButton;
	}

	public void setMenuItemEdit(MenuItem menuItemEdit) {
		this.menuItemEditProfile = menuItemEdit;
	}
}
