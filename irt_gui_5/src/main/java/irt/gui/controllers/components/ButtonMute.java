
package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.configuration.MutePacket;
import irt.gui.data.packet.observable.configuration.MutePacket.MuteStatus;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class ButtonMute extends FieldsControllerAbstract implements Initializable{

	private static final String CSS_CLASS_MUTED = "muted";
	public static final String CSS_CLASS_UNMUTED = "unmuted";

	private final MutePacket mutePacket;
	private final MutePacket muteCommandPacket;
	private ResourceBundle bundle;	

	@FXML private Button button;
	private MuteStatus muteStatus;

	public ButtonMute() throws PacketParsingException{

			mutePacket = new MutePacket();
			mutePacket.addObserver(this);

			muteCommandPacket = new MutePacket(MuteStatus.MUTED);
			muteCommandPacket.addObserver(this);

			addLinkedPacket(mutePacket);
			doUpdate(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
	}

//	@FXML void initialize() {
//		button.setUserData(this);
//	}
//
	@FXML void buttonAction(ActionEvent event) {
		muteCommandPacket.setCommand(muteStatus==MuteStatus.MUTED ? MuteStatus.UNMUTED : MuteStatus.MUTED);
		SerialPortController.QUEUE.add(muteCommandPacket, true);
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(10);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {

		if(packet instanceof MutePacket){
			if(packet.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){

				MutePacket p = new MutePacket(packet.getAnswer(), true);
				MuteStatus ms = MutePacket.MuteStatus.values()[p.getPayloads().get(0).getByte()];
				if(muteStatus!=ms){
					muteStatus = ms;
					final boolean muted = muteStatus==MuteStatus.MUTED;
					final String string = bundle.getString(muted ? "unmute" : "mute");
					Platform.runLater(()->{
						button.setText(string);
						button.getTooltip().setText(string);
						addClass(muted);
					});
				}
			}
		}
	}

	private void addClass(boolean muted) {
		final ObservableList<String> styleClass = button.getStyleClass();
		String toRemove;
		String toAdd;

		if(muted){
			toRemove = CSS_CLASS_UNMUTED;
			toAdd = CSS_CLASS_MUTED;
		}else{
			toRemove = CSS_CLASS_MUTED;
			toAdd = CSS_CLASS_UNMUTED;
		}

		styleClass.remove(toRemove);
		if(!styleClass.contains(toAdd))
			styleClass.add(toAdd);
	}
}
