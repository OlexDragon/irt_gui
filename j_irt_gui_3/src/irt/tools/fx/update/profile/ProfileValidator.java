package irt.tools.fx.update.profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;
import irt.tools.fx.update.profile.ProfileTables.Table;
import javafx.application.Platform;
import javafx.util.Pair;

public class ProfileValidator {

	private final Logger logger = LogManager.getLogger();
	private Boolean isFCM;
	private ProfileErrors profileError;

	public ProfileValidator(Profile profile) throws FileNotFoundException, NoSuchAlgorithmException, IOException {

		final ProfileParser profileParser = new ProfileParser();

		final Pair<String, CharBuffer> asCharBuffer = profile.asCharBufferWithMD5();
		try (Scanner scanner = new Scanner(asCharBuffer.getValue())) {

			while (scanner.hasNextLine()){
				final String trim = scanner.nextLine().trim();
				profileParser.parseLine(trim);
			}

			final DeviceType deviceType = profileParser.getDeviceType();
			isFCM = deviceType.isFCM();

			logger.trace("{}: is FCM - {}", deviceType, isFCM);

			//Return if can not get Device type
			if(isFCM==null){
				logger.info("Can not get Device type");
				profileError = ProfileErrors.CAN_NOT_GET_DEVICE_TYPE;
				return;
			}

			final List<Table> tablesWithError = ProfileTables.getTablesWithError();
			
			if(!tablesWithError.isEmpty()) {

				Callable<Void> r =
						()->{
							final EditTableMessageFx message = new EditTableMessageFx(profile, tablesWithError);
							return null;
						};
				FutureTask<Void> ft = new FutureTask<>(r);
				Platform.runLater(ft);

				try { ft.get(); } catch (InterruptedException | ExecutionException e) { logger.catching(Level.DEBUG, e); }
			}

				profileError = ProfileErrors.ERROR;
		}

//		profileError = ProfileErrors.NO_ERROR;
		logger.debug(profileError);
	}

	public Boolean isFCM() {
		return isFCM;
	}

	public ProfileErrors getProfileError() {
		return profileError;
	}

	public enum ProfileErrors{
		NO_ERROR,
		FILE_DOES_NOT_EXISTS,
		LODE_FILE_ERROR,
		CAN_NOT_GET_DEVICE_TYPE,
		ERROR,
		DO_NOT_EXSISTS
	}
}
