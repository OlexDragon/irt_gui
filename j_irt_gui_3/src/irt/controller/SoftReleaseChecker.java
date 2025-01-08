package irt.controller;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.FutureTask;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.irt_gui.IrtGui;
import irt.irt_gui.IrtMainFrame;
import javafx.application.Platform;

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

	private static boolean check() throws IOException {

		final String type = deviceInfo.getTypeId()+"."+deviceInfo.getRevision();
		final String key = type + ".path";

		final Properties properties = IrtGui.loadFlash3Properties();
		return Optional.ofNullable(properties.get(key)).map(Object::toString).map(File::new).filter(File::exists)

				.map(
						file->{

							try(	final FileReader fr = new FileReader(file);
									final BufferedReader br = new BufferedReader(fr);){

								String line = null;
								while((line = br.readLine())!=null) {
									line = line.trim();
									if(line.contains("FWTAG="))
										break;
								}

								final String l = line;

								return deviceInfo
										.getFirmwareBuildDate()
										.filter(bd->!l.contains(bd))
										.map(bd->true).orElse(false);

							} catch (IOException e) {
								logger.catching(e);
							}
							return false;
						})

				.orElseGet(
						()->{
							dialog = new FileDialog(IrtMainFrame.getMainFrame(), "Select the Firmware File related to " + type);
						    dialog.setMode(FileDialog.LOAD);
						    dialog.dispose();
						    while(true) {

						    	dialog.setVisible(true);

						    	final String file = dialog.getFile();
						    	if(file!=null) {

						    		final String path = Paths.get(dialog.getDirectory(), file).toString();
					    			Object old = properties.put(key, path);

					    			if(old==null || !old.equals(path)) {

					    				try(OutputStream os = new FileOutputStream(IrtGui.FLASH3_PRPPERIES);) {

					    					logger.info("key: {}; value: {}; added to the flash3.properties file", key, path);
					    					properties.store(os, "Created by IRT GUI on " + InetAddress.getLocalHost().getHostName());
					    					logger.info("The flash3.properties file has been resaved.");

					    				} catch (IOException e) {
											logger.catching(e);
										}
					    			}

					    			break;
						    	}else {
						    		 final int result = JOptionPane.showConfirmDialog(IrtMainFrame.getMainFrame(), "The program will not work unless you select a path.\nPress 'YES' to close the GUI", "ALERT MESSAGE", JOptionPane.YES_NO_OPTION);
						    		 if(result==0) {
						 		    	Platform.exit();
						 		    	System.exit(0);
						    		 }
						    	}
						    }

						    return false;
						});		
	}
	public void closeFileDialog() {
		Optional.ofNullable(dialog).ifPresent(FileDialog::dispose);
	}
}
