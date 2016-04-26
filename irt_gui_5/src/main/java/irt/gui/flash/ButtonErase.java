package irt.gui.flash;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.flash.EmptyPacket;
import irt.gui.data.packet.observable.flash.ErasePacket;
import irt.gui.flash.PanelFlash.UnitAddress;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;

public class ButtonErase extends Observable implements Observer, Initializable {
	private Logger logger = LogManager.getLogger();

	@FXML private Button button;

	private Alert alert = new Alert(AlertType.CONFIRMATION);
	private final ErasePacket erasePacket = new ErasePacket();
	private final EmptyPacket emptyPacket = new EmptyPacket();
	private final LinkedPacket dataPacket = new EmptyPacket(){ @Override public byte[] toBytes() { return pagesToErase; }};

	private UnitAddress unitAddress;
	private byte[] pagesToErase;
	private int count;

	private ResourceBundle bundle;

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		alert.setTitle("Erase Flash");
		erasePacket.addObserver((o, arg)->{
			if(PanelFlash.checkAswer("Erase " + unitAddress, (LinkedPacket)o, button))
				SerialPortController.QUEUE.add(dataPacket, false);

		});

		final Observer dataObserver = (o, arg)->{
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
			}else
				Platform.runLater(()->{
					button.setText(bundle.getString("erase"));
					button.getStyleClass().remove(ButtonRead.WARNING);
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
				count = 0;
				button.setText(bundle.getString("erase.erasing"));
				addWarningClass();
				SerialPortController.QUEUE.add(erasePacket, false);
			}
		});
		
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
				pagesToErase = new byte[] { 0,
											0, 23 };
				break;
			case PROGRAM:
				pagesToErase = new byte[] { 0, 9,	// N + 1 pages are erased
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
		}

		pagesToErase = PanelFlash.addCheckSum(pagesToErase);
	}

	private void addWarningClass() {
		final ObservableList<String> styleClass = button.getStyleClass();
		if(!styleClass.contains("warning"))
			styleClass.add("warning");
	}
}
