package irt.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo;
import irt.data.MyThreadFactory;

public class SoftReleaseChecker extends FutureTask<Boolean>{

	private static Logger logger = LogManager.getLogger();

	private static SoftReleaseChecker checker;
	private static DeviceInfo deviceInfo;

	public SoftReleaseChecker() {
		super(()->check());
	}

	public Optional<Boolean> check(DeviceInfo deviceInfo) {

		SoftReleaseChecker.deviceInfo = deviceInfo;

		new MyThreadFactory().newThread(this).start();

		try {
			return Optional.ofNullable(get(30, TimeUnit.SECONDS));

		} catch (Exception e) {
			logger.catching(e);
			return Optional.empty();
		}
	}

	private static boolean check() {

			String string = "\\\\192.168.2.250\\Share\\4alex\\boards\\SW release\\latest\\" + (deviceInfo.getDeviceType().map(dt->dt.TYPE_ID).filter(i->i< 1000).map(i->"picobuc.bin").orElse("fcm.bin"));
			File file = new File(string);
			file.isDirectory();

			if (file.exists()) {

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy, HH:mm:ss");
				Calendar calendar = simpleDateFormat.getCalendar();
				try {

					calendar.setTime(simpleDateFormat.parse(deviceInfo.getFirmwareBuildDate().orElse("00 00 0000")));

					return file.lastModified() > calendar.getTimeInMillis() + 10000;

				} catch (Exception e) {
					logger.catching(e);
				}
//					deviceInfo.setError("Firmware Update is Available.", Color.RED);
			}
			return false;
	}

	public static SoftReleaseChecker getInstance(){
		if(checker==null)
			checker = new SoftReleaseChecker();
		return checker;
	}
}
