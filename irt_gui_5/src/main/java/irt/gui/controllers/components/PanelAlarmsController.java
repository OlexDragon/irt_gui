package irt.gui.controllers.components;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import irt.gui.IrtGuiApp;
import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.UpdateController;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.alarms.AlarmIDsPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class PanelAlarmsController extends FieldsControllerAbstract {

	@FXML private 	TitledPane 	PanelAlarms;
	@FXML private 	VBox 		vBox;

	private short[] alarms;
	private final LinkedPacket packet;

	public PanelAlarmsController() throws PacketParsingException {
		packet = new AlarmIDsPacket();
	}

	@FXML public void initialize() {
		addPacketToSend(packet);
		doUpdate(true);
		UpdateController.addController(this);
	}

	@Override protected Duration getPeriod() {
		return Duration.ofSeconds(1);
	}

	@Override protected void updateFields(LinkedPacket packet) throws PacketParsingException {

		if(scheduleAtFixedRate!=null && scheduleAtFixedRate.isCancelled())
			return;

		LinkedPacket p = new AlarmIDsPacket(packet.getAnswer(), true);

		if (p.getPacketHeader().getPacketError() == PacketErrors.NO_ERROR) {

			final Payload pl = p.getPayloads().get(0);

			switch (pl.getParameterHeader().getParameterHeaderCode()) {

			case ALARM_IDs:
				if(setAlarmIDs(pl))
					for (short alarmId : alarms)
						addAlarmView(alarmId);
				break;

			case ALARM_CONFIG:
			case ALARM_DESCRIPTION:
			case ALARM_NAME:
			case ALARM_STATUS:
			case ALARM_SUMMARY_STATUS:
			default:
			}

		}else
			logger.warn("\n\t This Packet has error: {}", p);
	}

	@Override public void doUpdate(boolean update) {

		if(update)
			packet.addObserver(this);
		else
			packet.deleteObservers();

		super.doUpdate(update);
	}

	public void setTitle(String title) {
		PanelAlarms.setText(title);
	}

	private synchronized boolean setAlarmIDs(Payload pl) {

		doUpdate(false);
		final short[] arrayOfShort = pl.getArrayOfShort();

		if(alarms!=null && (Arrays.equals(alarms, arrayOfShort) || alarms.length != vBox.getChildren().size())) //return if already set
			return false;

			alarms = arrayOfShort;

			Platform.runLater(()->vBox.getChildren().clear());

		return true;
	}

	private void addAlarmView(short alarmId) {
		Platform.runLater(()->{
				try {

					FXMLLoader loader = new FXMLLoader( getClass().getResource("/fxml/components/AlarmView.fxml"));
					loader.setResources(ResourceBundle.getBundle(IrtGuiApp.BUNDLE));
					Parent root = (Parent) loader.load();
					AlarmFieldController alarmFieldController = loader.getController();
					alarmFieldController.build(alarmId);
					vBox.getChildren().add(root);

					final Optional<String> senderId = Optional
														.ofNullable(UpdateController.getSenderId())
														.filter(id->id.equals("flashTab"));

					alarmFieldController.doUpdate(!senderId.isPresent());

					UpdateController.addController(alarmFieldController);

				} catch (Exception e) {
					logger.catching(e);
				}
		});
	}
}

