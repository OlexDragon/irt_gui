package irt.tools.fx;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import irt.data.DeviceInfo;
import irt.tools.fx.UpdateMessagePkgFx.Message;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class UpdateMessagePkgFx extends Dialog<Message>{
	private static final String DID_NOT_SET = "did not set";

//	private final Logger logger = LogManager.getLogger();

	private final TextField ipAddress;

	private final Label lblProfile;

	private final CheckBox cbProfile;

	private String ipAddressStr;

	// ******************************* constructor UpdateMessageFx   ***************************************************
	public UpdateMessagePkgFx(DeviceInfo deviceInfo) {

		setTitle("IP Address");
		setHeaderText("Type a valid IP address.");

		final ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		// Update button

		final Button updateButton = (Button) getDialogPane().lookupButton(updateButtonType);
		updateButton.setDisable(true);

		// Cancel button

//		final Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		//IP Address row #0

		grid.add(new Label("IP Address:"), 0, 0);
		ipAddress = new TextField();
		final ChangeListener<? super String> textListener = (o, oV, nV)->enableUpdateButton(updateButton);
		final ChangeListener<? super Boolean> cbListener = (o, oV, nV)->enableUpdateButton(updateButton);
		ipAddress.textProperty().addListener(textListener);
		grid.add(ipAddress, 1, 0);

		ipAddress.setPromptText("192.168.0.1");
		ipAddress.textProperty().addListener(getListener(updateButton));

		//Package selection

		cbProfile = new CheckBox("Package:");
		cbProfile.selectedProperty().addListener(cbListener);
		grid.add(cbProfile, 0, 1);
		lblProfile = new Label();
		lblProfile.textProperty().addListener(textListener);
		grid.add(lblProfile, 1, 1);
		Button btnPackageSelection = new Button("Package Selection");
		btnPackageSelection.setOnAction(selectPackage());
		grid.add(btnPackageSelection, 2, 1);

		getDialogPane().setContent(grid);
		setResultConverter(button->{

			if(button == updateButtonType)
				return new Message(
						ipAddress.getText(),
						Optional.ofNullable(lblProfile.getTooltip())
						.map(Tooltip::getText)
						.map(txt->cbProfile.isSelected() ? txt : null)
						.orElse(cbProfile.isSelected() ? DID_NOT_SET : null));

			return null;
		});
	}

	private void enableUpdateButton(final Node updateButton) {

		final boolean addressPresent = Optional.ofNullable(ipAddress.getText()).map(String::trim).filter(value->!value.isEmpty()).isPresent();
		final boolean proflePresent = Optional.ofNullable(lblProfile.getText()).map(String::trim).filter(value->!value.isEmpty()).isPresent();

		final boolean profileSelected = cbProfile.isSelected();

		final boolean disable = !addressPresent || !profileSelected || (profileSelected && !proflePresent);


		Platform.runLater(()->updateButton.setDisable(disable));
	}

	private void setProfileLabel(final Path path) {
		Platform.runLater(()->{

			lblProfile.setTooltip(new Tooltip(path.toString()));
			lblProfile.setText(path.getFileName().toString());
			cbProfile.setSelected(true);
		});
	}

	private EventHandler<ActionEvent> selectPackage() {
		return e->{

			FileChooser fileChooser = new FileChooser();
			final ObservableList<ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
			extensionFilters.add(new ExtensionFilter("IRT Technologies package file", "*.pkg"));
			extensionFilters.add(new ExtensionFilter("All", "*.*"));

			final Optional<File> file = Optional.ofNullable(lblProfile.getTooltip()).map(tt->tt.getText()).map(File::new);
			if(file.isPresent()){

				final File f = file.get();
				fileChooser.setInitialDirectory(f.getParentFile());
				fileChooser.setInitialFileName(f.getName());
			}

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null)
				setProfileLabel(result.toPath());
		};
	}

	private ChangeListener<? super String> getListener(final Node button) {
		return (o, oV, nV)->{

			final List<String> addr = Optional.ofNullable(nV).filter(a->!a.isEmpty()).map(a->a.split("\\D")).map(Arrays::stream).orElse(Stream.empty()).filter(s->!s.isEmpty()).collect(Collectors.toList());

			if(addr.size()!=4){
				button.setDisable(true);
				return;
			}

			ipAddressStr = addr.stream().collect(Collectors.joining("."));
			button.setDisable(false);
		};
	}

	public void setIpAddress(String addrStr) {
		ipAddress.setText(addrStr);
	}

	public String getIpAddress() {
		return ipAddressStr;
	}

	//****************** class Message *****************************
	public class Message{

		private String address;
		private String profilePath;

		public Message(String address, String profile) {
			super();
			this.address = address;
			this.profilePath = profile;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getPacksgePath() {
			return profilePath;
		}

		public void setProfile(String profile) {
			this.profilePath = profile;
		}

		@Override
		public String toString() {
			return "Message [\n\taddress=" + address + ", \n\tprofile=" + profilePath  + "]";
		}
	}
}
