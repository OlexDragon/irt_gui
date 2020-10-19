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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;
import irt.data.ThreadWorker;
import irt.tools.fx.update.profile.EditTablesMessageFx.Action;
import irt.tools.fx.update.profile.ProfileTables.Table;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class ProfileValidator {

	private final Logger logger = LogManager.getLogger();
	private Boolean isFCM;
	private Action action;

	public ProfileValidator(Profile profile) throws FileNotFoundException, NoSuchAlgorithmException, IOException {

		final ProfileParser profileParser = new ProfileParser();

		final CharBuffer asCharBuffer = profile.asCharBuffer();
		try (Scanner scanner = new Scanner(asCharBuffer)) {

			while (scanner.hasNextLine()){
				final String trim = scanner.nextLine().trim();
				profileParser.parseLine(trim);
			}

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
				return;
			}

			final List<Table> tablesWithError = ProfileTables.getTablesWithError();
			
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
		}

		logger.debug(action);
	}

	public Boolean isFCM() {
		return isFCM;
	}

	public Action getAction() {
		return action;
	}
}
