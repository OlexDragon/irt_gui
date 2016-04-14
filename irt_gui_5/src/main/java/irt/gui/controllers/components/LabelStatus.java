
package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket.AlarmSeverities;
import irt.gui.data.packet.observable.measurement.StatusPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class LabelStatus extends FieldsControllerAbstract implements Initializable{

	@FXML private Label 		muteLabel;
	@FXML private Label 		lockLabel;

	private ResourceBundle 		bundle;

	private final StatusPacket statusPacket;
	private Integer status;

	public LabelStatus() {

		StatusPacket p;

		try {
			p =  new StatusPacket();

		} catch (PacketParsingException e) {
			p = null;
			logger.catching(e);
		}

		statusPacket = p;

		if(p==null)
			return;

		addLinkedPacket(statusPacket);
		doUpdate(true);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;

		addLinkedPacket(statusPacket);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {
		if(packet instanceof StatusPacket){
			if(packet.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){
				StatusPacket p = new StatusPacket(packet.getAnswer(), true);
				final Integer status = p.getPayloads().get(0).getInt(0);
				if(status!=this.status){
					this.status = status;
					setMuteStatus();
					setLockStatus();
				}
			}
		}
	}

	private void setMuteStatus() {

		final String muted = AlarmSeverities.INFO.getStyleClass();
		final String unmuted = AlarmSeverities.NO_ALARM.getStyleClass();

		if((status & StatusByte.MUTE.ordinal()) != 0){
			Platform.runLater(()->{
				final String value = bundle.getString("mute.muted");
				muteLabel.setText(value);
			});
			setStyleClass(muteLabel, muted, unmuted);

		}else{
			Platform.runLater(()->{
				final String value = bundle.getString("mute.unmuted");
				muteLabel.setText(value);
			});
			setStyleClass(muteLabel, unmuted, muted);
		}
	}

	private void setLockStatus() {

		final String locked = AlarmSeverities.NO_ALARM.getStyleClass();
		final String notLocked = AlarmSeverities.CRITICAL.getStyleClass();

		if((status & StatusByte.LOCK.ordinal()) != 0){
			String value = bundle.getString("lock.locked");
			Platform.runLater(()->lockLabel.setText(value));
			setStyleClass(lockLabel, locked, notLocked);

		}else{
			String value = bundle.getString("lock.unlocked");
			Platform.runLater(()->lockLabel.setText(value));
			setStyleClass(lockLabel, notLocked, locked);
		}
	}

	private void setStyleClass(Label label, String styleClassToAdd, String styleClassToRemove) {
		final ObservableList<String> styleClass = label.getStyleClass();
		styleClass.remove(styleClassToRemove);
		if(!styleClass.contains(styleClassToAdd))
			styleClass.add(styleClassToAdd);
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public enum StatusByte{
		UNKNOWN,
		MUTE,
		LOCK
	}
}
