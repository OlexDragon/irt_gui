package irt.gui.controllers.flash;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.flash.PanelFlash.Answer;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.flash.ConnectPacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class ButtonConnect implements Observer, Initializable {

	private final ConnectPacket packet = new ConnectPacket();

	@FXML private Button button;

	private ResourceBundle bundle;

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
		packet.addObserver(this);
	}

	@FXML private void onAction() {
		button.setText(bundle.getString("connect.connecting"));
		SerialPortController.QUEUE.add(packet, false);
	}

	@Override public void update(Observable o, Object arg) {

		LinkedPacket lp = (LinkedPacket) o;

		if (lp.getAnswer() == null){

			PanelFlash.showAlert(AlertType.ERROR, Answer.NULL.toString(), button);
			Platform.runLater(()->{
				setStyleClass("error", "connected");
				button.setText(bundle.getString("connect"));
			});

		}else{

			final Answer a = Answer
								.valueOf(lp.getAnswer()[0])
								.orElse(Answer.UNKNOWN);
			Platform.runLater(()->{
				button.setTooltip(new Tooltip(a.toString()));
			});

			if(a==Answer.ACK){
				Platform.runLater(()->{
					setStyleClass("connected", "error");
					button.setText(bundle.getString("connect.connected"));
				});
			}else{
				PanelFlash.showAlert(AlertType.ERROR, a.toString(), button);
				Platform.runLater(()->{
					setStyleClass("error", "connected");
					button.setText(bundle.getString("connect"));
				});
			}
		}
	}

	private void setStyleClass(String toAdd, String toRemove){
		final ObservableList<String> styleClass = button.getStyleClass();
		if (styleClass.contains(toAdd))
			return;

			styleClass.remove(toRemove);
			styleClass.add(toAdd);
	}
}
