package irt.controller;

import irt.data.DeviceInfo;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JOptionPane;

public class SoftReleaseChecker extends Thread {

	private DeviceInfo deviceInfo;

	public SoftReleaseChecker(){

		int priority = getPriority();
		if(priority>Thread.MIN_PRIORITY)
			setPriority(priority-1);

		start();
	}

	public void check(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
		synchronized (this) { notify(); }
	}

	@Override
	public void run() {
		synchronized (this) { try { wait(); } catch (InterruptedException e) { } }

		String string = "\\\\192.168.2.250\\Share\\4alex\\boards\\SW release\\latest\\"+(deviceInfo.getType()<100 ? "picobuc.bin" : "fcm.bin");
		File file = new File(string);
		if(file.exists()){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy, HH:mm:ss");
			Calendar calendar = simpleDateFormat.getCalendar();
			try {
				calendar.setTime(simpleDateFormat.parse(deviceInfo.getFirmwareBuildDate().toString()));
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
			}

			if(file.lastModified()>calendar.getTimeInMillis()+10000)
				deviceInfo.setError("Firmware Update is Available.", Color.RED);
		}
	}

}
