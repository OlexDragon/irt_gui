package irt.gui.controllers.flash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.flash.enums.UnitAddress;
import irt.gui.controllers.flash.service.EraseObject;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.flash.EmptyPacket;
import irt.gui.data.packet.observable.flash.ErasePacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;

public class ButtonErase extends Observable implements Observer, Initializable, EraseObject {
	private static final int WAIT_TIME = 1000;

	private final static Logger logger = LogManager.getLogger();

	@FXML private Button button;

	private Alert alert = new Alert(AlertType.CONFIRMATION);
	private final ErasePacket erasePacket = new ErasePacket();
	private final EmptyPacket emptyPacket = new EmptyPacket();
	private final LinkedPacket dataPacket = new EmptyPacket(){ @Override public int getWaitTime() { return ButtonErase.WAIT_TIME; } @Override public byte[] toBytes() { return pagesToErase; }};

	private UnitAddress unitAddress;
	private byte[] pagesToErase;
	private int count;
	private int fileSize;
	private boolean error;

	private final ExecutorService executor = Executors.newFixedThreadPool(5, new MyThreadFactory());
	private Future<Boolean> submit;
	private Callable<Boolean> sleep = ()->{

		try{

			TimeUnit.SECONDS.sleep(30);

		}catch(Exception ex){
			return error;
		}

		return true;//error
	};


	private ResourceBundle bundle;


	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		alert.setTitle("Erase Flash");
		erasePacket.addObserver((o, arg)->{
			executor.execute(()->{

				if(PanelFlash.checkAswer("Erase " + unitAddress + ": ", (LinkedPacket)o, button))
					SerialPortController.getQueue().add(dataPacket, false);
				else{
					if(submit!=null)
						submit.cancel(true);

					Platform.runLater(()->{
						button.setText(bundle.getString("erase"));
						button.getStyleClass().remove(ButtonRead.WARNING);
					});
				}
			});
		});

		final Observer dataObserver = (o, arg)->{
			executor.execute(()->{

				logger.trace(o);
				LinkedPacket p = (LinkedPacket) o;
				if(p.getAnswer()==null && count<100){
					count++;
					try {
						Thread.sleep(WAIT_TIME);
					} catch (Exception e) {
						logger.catching(e);
					}
					SerialPortController.getQueue().add(emptyPacket, false);

				}else{
					error = !PanelFlash.checkAswer("Erase " + unitAddress + ": ", (LinkedPacket)o, button);

					 if(submit!=null)
						 submit.cancel(true);

					Platform.runLater(()->{
						button.setText(bundle.getString("erase"));
						button.getStyleClass().remove(ButtonRead.WARNING);
					});
				}
			});
		};
		emptyPacket.addObserver(dataObserver);
		dataPacket.addObserver(dataObserver);
	}

	@FXML private void onAction() {
		if(alert.isShowing())
			return;

		alert
		.showAndWait()
		.ifPresent((b)->{
			if(b == ButtonType.OK){
				erase(0);
			}
		});
	}

	public boolean erase(int fileSize) {
		logger.entry(fileSize);

		this.fileSize = fileSize;
		update(null, unitAddress);
		erase();

		submit = executor.submit(sleep);

		try {
			error = submit.get();
		} catch (Exception e) {}

		return !error;
	}

	private void erase() {
		error = false;
		count = 0;
		Platform.runLater(()->button.setText(bundle.getString("erase.erasing")));
		addWarningClass();
		SerialPortController.getQueue().add(erasePacket, false);
	}

	@Override public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setTooltip(new Tooltip(unitAddress.toString()));

			pagesToErase = PanelFlash
					.addCheckSum(
							getPagesToExtendedErase(unitAddress.getAddr(), fileSize));
			logger.debug("pagesToErase: {}", pagesToErase);
		}

	}

	public static final int KB = 1024;
	public static byte[] getPagesToExtendedErase(int startAddress, int length) {
		int[] allPages = new int[] { 	16 * KB, 16 * KB, 16 * KB, 16 * KB, 64 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB,
										16 * KB, 16 * KB, 16 * KB, 16 * KB, 64 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB, 128 * KB};


		int sum = UnitAddress.PROGRAM.getAddr();	//Start address
		int stopAddress = startAddress + (length>0 ? length : 1);

		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
			outputStream.write(0);	// The bootloader receives one half-word (two bytes) that contain N, the number of pages to be erased
			outputStream.write(0);


			for(int page = 0; page<allPages.length && sum<stopAddress; page++) {

//				logger.error("startAddress: 0x{}; stopAddress: {}; page: {}; sum: {}", startAddress, stopAddress, page, sum);

				if(sum>=startAddress){
					final byte[] bytes = toBytes((short) page);
					outputStream.write(bytes); // The bootloader receives one half-word (two bytes) that contain N, the number of pages to be erased
												//  each half-word containing a page number (coded on two bytes, MSB first).
				}

				sum += allPages[page];
			}

			final byte[] result = outputStream.toByteArray();
			final int pages = result.length/2 - 2;
			final byte[] arrayPages = toBytes((short) pages);
			result[0] = arrayPages[0]; // the number of pages to be erased –1.
			result[1] = arrayPages[1]; 

			logger.debug("startAddress: 0x{}; stopAddress: 0x{}; sum: 0x{}; length: {}; pages: {}; result.length: {}; result: 0x{}", Integer.toHexString(startAddress), Integer.toHexString(stopAddress), Integer.toHexString(sum), length, pages, result.length, DatatypeConverter.printHexBinary(result));

			return result;

		} catch (IOException e) {
			logger.catching(e);
		}

		return null;
	}

	private static byte[] toBytes(final short pages) {
		return ByteBuffer.allocate(2).putShort(pages).array();
	}

	private void addWarningClass() {
		final ObservableList<String> styleClass = button.getStyleClass();
		if(!styleClass.contains("warning"))
			styleClass.add("warning");
	}
}
