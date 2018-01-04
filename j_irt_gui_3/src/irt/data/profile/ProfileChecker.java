package irt.data.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;

public class ProfileChecker {

	private final Logger logger = LogManager.getLogger();
	private Boolean isFCM;
	private ProfileErrors profileError;

	public ProfileChecker(String pathToProfile) {
		Path p = Paths.get(pathToProfile);
		File f = p.toFile();

		//File does not exists
		if(!(f.exists() && f.isFile())){
			logger.info("The file does not exists ({})", f);
			profileError = ProfileErrors.FILE_DOES_NOT_EXISTS;
			return;
		}

		final StringBuffer fileContents = new StringBuffer();
		final ProfileParser profileParser = new ProfileParser();

		try (Scanner scanner = new Scanner(f)) {

			while (scanner.hasNextLine()){
				final String trim = scanner.nextLine().trim();
				fileContents.append(trim).append("\n");
				profileParser.append(trim);
			}

			final Integer deviceType = profileParser.getDeviceType();
			isFCM = DeviceType.isFCM(deviceType);

			//Return if can not get Device type
			if(isFCM==null){
				logger.info("Can not get Device type");
				profileError = ProfileErrors.CAN_NOT_GET_DEVICE_TYPE;
				return;
			}

			if(profileParser.hasError()){
				logger.info("The profile has a error");

				profileError = ProfileErrors.ERROR;
				return;
			}

		} catch (FileNotFoundException e) {
			LogManager.getLogger().catching(e);
			profileError = ProfileErrors.LODE_FILE_ERROR;
			return;
		}

		profileError = ProfileErrors.NO_ERROR;
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
		ERROR
	}
}
