package irt.gui.controllers.flash;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.flash.enums.UnitAddress;
import irt.gui.controllers.flash.service.EraseObject;
import irt.gui.controllers.flash.service.PagesCount;
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
	private Logger logger = LogManager.getLogger();

	@FXML private Button button;

	private Alert alert = new Alert(AlertType.CONFIRMATION);
	private final ErasePacket erasePacket = new ErasePacket();
	private final EmptyPacket emptyPacket = new EmptyPacket();
	private final LinkedPacket dataPacket = new EmptyPacket(){ @Override public byte[] toBytes() { return pagesToErase; }};

	private UnitAddress unitAddress;
	private byte[] pagesToErase;
	private int count;
	private long fileSize;
	private boolean error;

	private final ExecutorService executor = Executors.newFixedThreadPool(5, new MyThreadFactory());
	private Future<Boolean> submit;
	private Callable<Boolean> sleep = ()->{

		try{

			TimeUnit.SECONDS.sleep(10);

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
					SerialPortController.QUEUE.add(dataPacket, false);
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
				if(p.getAnswer()==null && count<1000){
					count++;
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						logger.catching(e);
					}
					SerialPortController.QUEUE.add(emptyPacket, false);

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
				fileSize = 0;
				erase();
			}
		});
	}

	public boolean erase(long fileSize) {
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
		SerialPortController.QUEUE.add(erasePacket, false);
	}

	@Override public void update(Observable o, Object arg) {
		if(arg instanceof UnitAddress){
			unitAddress = (UnitAddress)arg;
			button.setTooltip(new Tooltip(unitAddress.toString()));

			switch(unitAddress){
			case CONVERTER:
				pagesToErase = new byte[] { 0, 0,		// N + 1 pages are erased
											0, 10 };	// pages are erased
				break;
			case BIAS:
				pagesToErase = new byte[] { 0, 0,
											0, 11 };
				break;
			case HP_BIAS:
				pagesToErase = new byte[] { 0, 0,
											0, 23 };
				break;
			case PROGRAM:
				pagesToErase =
						fileSize>0
							? new PagesCount(fileSize).getPages()
							: new byte[] { 0, 9,	// N + 1 pages are erased
											0, 0,	// pages are erased
											0, 1,
											0, 2,
											0, 3,
											0, 4,
											0, 5,
											0, 6,
											0, 7,
											0, 8,
											0, 9 };
				break;
			default:
				pagesToErase = new byte[] { 0, 0,
											0, 11 };
				break;
			}
			pagesToErase = PanelFlash.addCheckSum(pagesToErase);
		}

	}

	private void addWarningClass() {
		final ObservableList<String> styleClass = button.getStyleClass();
		if(!styleClass.contains("warning"))
			styleClass.add("warning");
	}
}
