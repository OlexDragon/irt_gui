package irt.tools.fx.update.profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;
import irt.data.ThreadWorker;
import irt.tools.fx.update.profile.EditTablesMessageFx.Action;
import irt.tools.fx.update.profile.table.ProfileTable;
import irt.tools.fx.update.profile.table.ProfileTables;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;

public class ProfileValidator {
	private final Logger logger = LogManager.getLogger();

	private final static ButtonData bdIgnore = ButtonData.OK_DONE;
	private final static ButtonData bdReplace = ButtonData.OTHER;
	private final static ButtonData bdSave = ButtonData.APPLY;

	private Boolean isFCM;
	private Action action;

	public ProfileValidator(Profile profile) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		ProfileTables.clear();

		AtomicReference<CharBuffer> arCharBuffer= new AtomicReference<>(profile.toCharBuffer());

		final ProfileParser profileParser = parseProfile(arCharBuffer);

		// Corrupted beginning of the profile
		if(profileParser.isCorrupted()) 
				fixProfileBeginning(profile, arCharBuffer);

		if(action==Action.CANCEL)
			return;

		//Return if can not get Device type
		if(!getDeviceType(profileParser))
			return;

		final List<ProfileTable> tablesWithError = profileParser.getTablesWithError();
			
		if(tablesWithError.isEmpty())
			action = Action.CONTINUE;

		else{

			Callable<Action> r =  EditTablesMessageFx.getMessageTask(profile, tablesWithError);
			FutureTask<Action> ft = new FutureTask<>(r);
			ThreadWorker.runThread(ft, "Start EditTableMessageFx");

			try {

				action = ft.get();

			} catch (InterruptedException | ExecutionException e) {
				logger.catching(Level.DEBUG, e);
				action = Action.CANCEL;
			}
		}

		logger.debug(action);
	}

	private boolean getDeviceType(ProfileParser profileParser) {

		//	 isFCM = true -> frequency converter; isFCM = false -> Bias board or shelf controller; isFCM = null -> unknown
		isFCM = Optional.ofNullable(profileParser.getDeviceType()).map(DeviceType::isFCM).orElse(null);

		logger.trace("{}: is FCM - {}", profileParser, isFCM);

		//Return if can not get Device type
		if(isFCM==null){
			logger.warn("Can not get Device type");

			Platform.runLater(
					()->{
						Alert alert = new Alert(AlertType.WARNING);
						alert.initModality(Modality.APPLICATION_MODAL);
						alert.setTitle("Profile error.");
						alert.setHeaderText(null);
						alert.setContentText("The profile does not have the Devuce Type property. (\"device-type\")");
						alert.showAndWait();
					});

			action = Action.CANCEL;
			return false;
		}
		return true;
	}

	public void fixProfileBeginning(Profile profile, AtomicReference<CharBuffer> arCharBuffer) {
		logger.trace("The Profile is corrupted.");

		final String beginning = Profile.getBeginning(arCharBuffer.get());
		AtomicReference<String> textAreaContent = new AtomicReference<>(beginning);

		FutureTask<Optional<ButtonType>> ft = new FutureTask<>(

				()->{

					Alert alert = new Alert(AlertType.WARNING);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.setTitle("Profile error.");
					alert.setContentText("Beginning of the profile is corrupted. ");

					// Buttons
					final ButtonType buttonTypeIgnore = new ButtonType("Ignore", bdIgnore);		// Save as is
					final ButtonType buttonTypeReplace = new ButtonType("Replace", bdReplace);	// Replace by default
					final ButtonType buttonTypeSave = new ButtonType("Save", bdSave);			// Save changes

					final ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
					buttonTypes.setAll(buttonTypeIgnore, buttonTypeReplace, ButtonType.CANCEL, buttonTypeSave);

					final DialogPane dialogPane = alert.getDialogPane();
					((Button) dialogPane.lookupButton(buttonTypeReplace)).setTooltip(new Tooltip("Replace by default"));
					final Button btnSave = (Button) dialogPane.lookupButton(buttonTypeSave);
					btnSave.setDisable(true);

					// Text Area

					TextArea textArea = new TextArea(beginning);
					dialogPane.setExpandableContent(textArea);
					textArea.textProperty().addListener(
							(o, oV, nV)->{

								textAreaContent.set(nV);

								try(	Scanner scanerNew = new Scanner(nV);
										Scanner scanerOrigin = new Scanner(beginning);){

									boolean equals = true;
									while(scanerNew.hasNextLine() && scanerOrigin.hasNextLine()) {

										if(!scanerNew.nextLine().equals(scanerOrigin.nextLine())) {
											equals = false;
											break;
										}
									}

									btnSave.setDisable(equals & !(scanerNew.hasNextLine() || scanerOrigin.hasNextLine()));
								}
							});

					return alert.showAndWait();
				});
		Platform.runLater(ft);


		try {

			ft.get().map(ButtonType::getButtonData)
			.ifPresent(
					bd->{

						try {

							if(bd==bdIgnore)
								action = Action.CONTINUE;

							else if(bd==bdReplace) {
								profile.updateAndSave(Profile.getDefaultBeginning());
								action = Action.CONTINUE;

							}else if(bd==bdSave) {
								profile.updateAndSave(textAreaContent.get());
								action = Action.CONTINUE;

							}else
								action = Action.CANCEL;

						} catch (IOException e) {
							logger.catching(e);
						}

						return;
					});

		} catch (InterruptedException | ExecutionException e) { logger.catching(Level.DEBUG, e); }
	}

	public ProfileParser parseProfile(AtomicReference<CharBuffer> arCharBuffer) {
		final ProfileParser profileParser = new ProfileParser();

		try (Scanner scanner = new Scanner(arCharBuffer.get())) {

			while (scanner.hasNextLine()){
				final String line = scanner.nextLine();
				profileParser.parseLine(line);
			}
		}

		return profileParser;
	}

	public Boolean isFCM() {
		return isFCM;
	}

	public Action getAction() {
		return action;
	}
}
