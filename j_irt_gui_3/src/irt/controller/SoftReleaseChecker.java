package irt.controller;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.irt_gui.IrtGui;

public class SoftReleaseChecker extends FutureTask<Boolean>{

	private static Logger logger = LogManager.getLogger();

	private static DeviceInfo deviceInfo;

	private static FileDialog dialog;

	public SoftReleaseChecker() {
		super(()->check());
	}

	public Optional<Boolean> check(DeviceInfo deviceInfo) {

		SoftReleaseChecker.deviceInfo = deviceInfo;

		ThreadWorker.runThread(this, "SoftReleaseChecker.check(DeviceInfo)");

		try {
			return Optional.ofNullable(get());

		} catch (Exception e) {
			logger.catching(e);
		}
		return Optional.empty();
	}

	private static boolean check() {

		final String type = deviceInfo.getTypeId()+"."+deviceInfo.getRevision();
		final String key = type + ".path";

		return Optional.ofNullable(IrtGui.softProperties.get(key)).map(Object::toString).map(File::new).filter(File::exists)

				.map(
						file->{

							try(	final FileReader fr = new FileReader(file);
									final BufferedReader br = new BufferedReader(fr);){

								String line = null;
								while((line = br.readLine())!=null) {
									line = line.trim();
									if(line.startsWith("FWTAG="))
										break;
								}
								final String l = line;

								return deviceInfo.getFirmwareBuildDate().filter(bd->!l.contains(bd)).map(bd->true).orElse(false);

							} catch (IOException e) {
								logger.catching(e);
							}
							return false;
						})

				.orElseGet(
						()->{
							dialog = new FileDialog((Frame)null, "Select the Firmware File related to " + type);
						    dialog.setMode(FileDialog.LOAD);
						    dialog.setVisible(true);
						    dialog.dispose();
						    Optional.ofNullable(dialog.getFile()).map(f->Paths.get(dialog.getDirectory(), f)).map(Path::toString)
						    .ifPresent(
						    		path->{

						    			final Properties properties = IrtGui.loadFlash3Properties();
						    			Object old = properties.put(key, path);

						    			if(!old.equals(path))
						    			try(OutputStream os = new FileOutputStream(IrtGui.FLASH3_PRPPERIES);) {

						    				properties.store(os, "Created by IRT GUI on " + InetAddress.getLocalHost().getHostName());

						    			} catch (IOException e) {
						    				logger.catching(e);
						    			}
						    		});
						    return false;
						});		
	}
	public void closeFileDialog() {
		Optional.ofNullable(dialog).ifPresent(FileDialog::dispose);
	}
}
