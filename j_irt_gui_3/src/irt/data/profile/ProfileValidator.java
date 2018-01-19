package irt.data.profile;

import java.nio.CharBuffer;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;

public class ProfileValidator {

	private final Logger logger = LogManager.getLogger();
	private Boolean isFCM;
	private ProfileErrors profileError;

	public ProfileValidator(CharBuffer charBuffer) {

		final ProfileParser profileParser = new ProfileParser();

		try (Scanner scanner = new Scanner(charBuffer)) {

			while (scanner.hasNextLine()){
				final String trim = scanner.nextLine().trim();
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
		ERROR,
		DO_NOT_EXSISTS
	}
}
