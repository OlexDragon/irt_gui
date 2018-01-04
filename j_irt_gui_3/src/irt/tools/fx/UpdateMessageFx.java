package irt.tools.fx;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.file.FileScanner;
import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;
import irt.tools.fx.UpdateMessageFx.Message;
import irt.tools.panel.head.IrtPanel;
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

public class UpdateMessageFx extends Dialog<Message>{
	private static final String DID_NOT_SET = "did not set";

	private final Logger logger = LogManager.getLogger();

	private final TextField ipAddress;

	private final Label lblProfile;
	private final Label lblProgram;

	private final CheckBox cbProfile;
	private final CheckBox cbProgram;

	private String ipAddressStr;
	private FileScanner fileScanner;

	// ******************************* constructor UpdateMessageFx   ***************************************************
	public UpdateMessageFx(DeviceInfo deviceInfo) {

		// Search profile by serial number on the disk Z:
		new MyThreadFactory().newThread(()->{
			
			deviceInfo.getSerialNumber().map(sn->sn + ".bin").ifPresent(f->{
				
				try {

					fileScanner = new FileScanner( Paths.get(IrtPanel.PROPERTIES.getProperty("path_to_profiles")), f);
					final List<Path> paths = fileScanner.get(10, TimeUnit.SECONDS);

					if(paths.size()!=1)
						return;

					final Path path = paths.get(0);
					setProfileLabel(path);

				} catch (CancellationException e) {
					logger.info("fileScaner has been canceled.");

				} catch (Exception e) {
					logger.catching(e);
				}
			});
		})
		.start();

		setTitle("IP Address");
		setHeaderText("Type a valid IP address.");

		final EventHandler<ActionEvent> fileScannerCansel = e->fileScanner.cancel(true);
		setOnCloseRequest(e->fileScanner.cancel(true));	// Cancel fileScaneron on close

		final ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

		// Update button

		final Button updateButton = (Button) getDialogPane().lookupButton(updateButtonType);
		updateButton.setDisable(true);
		updateButton.setOnAction(fileScannerCansel);	// Cancel fileScaneron on click

		// Cancel button

		final Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
		cancelButton.setOnAction(fileScannerCansel);	// Cancel fileScaneron on click

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

		//Profile row #1

		cbProfile = new CheckBox("Profile:");
		cbProfile.selectedProperty().addListener(cbListener);
		grid.add(cbProfile, 0, 1);
		lblProfile = new Label();
		lblProfile.textProperty().addListener(textListener);
		grid.add(lblProfile, 1, 1);
		Button btnOrofileSelection = new Button("Profile Selection");
		btnOrofileSelection.setOnAction(selectProfile());
		grid.add(btnOrofileSelection, 2, 1);

		//Program row #2

		cbProgram = new CheckBox("Program:");
		cbProgram.selectedProperty().addListener(cbListener);
		grid.add(cbProgram, 0, 2);
		lblProgram = new Label();
		lblProgram.textProperty().addListener(textListener);
		grid.add(lblProgram, 1, 2);
		Button btnProgramSelection = new Button("Program Selection");
		btnProgramSelection.setOnAction(selectProgram());
		grid.add(btnProgramSelection, 2, 2);

		getDialogPane().setContent(grid);
		setResultConverter(button->{

			if(button == updateButtonType)
				return new Message(
						ipAddress.getText(),
						Optional.ofNullable(lblProfile.getTooltip())
						.map(Tooltip::getText)
						.map(txt->cbProfile.isSelected() ? txt : null)
						.orElse(cbProfile.isSelected() ? DID_NOT_SET : null),

						Optional.ofNullable(lblProgram.getTooltip())
						.map(Tooltip::getText)
						.map(txt->cbProgram.isSelected() ? txt : null)
						.orElse(cbProgram.isSelected() ? DID_NOT_SET : null));

			return null;
		});
	}

	private void enableUpdateButton(final Node updateButton) {

		final boolean addressPresent = Optional.ofNullable(ipAddress.getText()).map(String::trim).filter(value->!value.isEmpty()).isPresent();
		final boolean proflePresent = Optional.ofNullable(lblProfile.getText()).map(String::trim).filter(value->!value.isEmpty()).isPresent();
		final boolean programPresent = Optional.ofNullable(lblProgram.getText()).map(String::trim).filter(value->!value.isEmpty()).isPresent();

		final boolean profileSelected = cbProfile.isSelected();
		final boolean programSelected = cbProgram.isSelected();

		final boolean disable = !addressPresent || !(profileSelected || programSelected) || (profileSelected && !proflePresent)  || (programSelected && !programPresent);


		Platform.runLater(()->updateButton.setDisable(disable));
	}

	private void setProfileLabel(final Path path) {
		Platform.runLater(()->{

			lblProfile.setTooltip(new Tooltip(path.toString()));
			lblProfile.setText(path.getFileName().toString());
			cbProfile.setSelected(true);
		});
	}

	private void setProgramLabel(final Path path) {
		Platform.runLater(()->{

			lblProgram.setTooltip(new Tooltip(path.toString()));
			lblProgram.setText(path.getFileName().toString());
			cbProgram.setSelected(true);
		});
	}

	private EventHandler<ActionEvent> selectProfile() {
		return e->{

			FileChooser fileChooser = new FileChooser();
			final ObservableList<ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
			extensionFilters.add(new ExtensionFilter("IRT Technologies BIN file", "*.bin"));
			extensionFilters.add(new ExtensionFilter("All", "*.*"));

			final Optional<File> file = Optional.ofNullable(lblProfile.getTooltip()).map(tt->tt.getText()).map(File::new);
			if(file.isPresent()){

				final File f = file.get();
				fileChooser.setInitialDirectory(f.getParentFile());
				fileChooser.setInitialFileName(f.getName());
			}

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null){
				fileScanner.cancel(true);	// Cancel fileScaneron before the new path setting
				setProfileLabel(result.toPath());
			}
		};
	}

	private EventHandler<ActionEvent> selectProgram() {
		return e->{

			FileChooser fileChooser = new FileChooser();
			final ObservableList<ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
			extensionFilters.add(new ExtensionFilter("IRT Technologies BIN file", "*.bin"));
			extensionFilters.add(new ExtensionFilter("All", "*.*"));

			final File file = new File("Z:\\4alex\\boards\\SW release\\latest");
			if(file.exists() && file.isDirectory())
				fileChooser.setInitialDirectory(file);

			final File result = fileChooser.showOpenDialog(getOwner());
			if(result!=null)
				setProgramLabel(result.toPath());
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
		private String programPath;

		public Message(String address, String profile, String program) {
			super();
			this.address = address;
			this.profilePath = profile;
			this.programPath = program;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getProfilePath() {
			return profilePath;
		}

		public void setProfile(String profile) {
			this.profilePath = profile;
		}

		public String getProgramPath() {
			return programPath;
		}

		public void setProgram(String program) {
			this.programPath = program;
		}

		@Override
		public String toString() {
			return "Message [\n\taddress=" + address + ", \n\tprofile=" + profilePath + ", \n\tprogram=" + programPath + "]";
		}
	}
}
