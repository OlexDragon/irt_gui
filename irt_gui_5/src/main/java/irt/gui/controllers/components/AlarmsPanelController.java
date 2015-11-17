package irt.gui.controllers.components;

import java.time.Duration;
import java.util.List;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.alarms.AlarmIDsPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class AlarmsPanelController extends FieldsControllerAbstract {

	@FXML private 	TitledPane 	alarmsPanel;
	@FXML private 	VBox 		vBox;

	@FXML public 	void 		initialize() {
		addLinkedPacket(packet);
		doUpdate(true);
	}

	private short[] alarms;
	private final LinkedPacket packet;

	public AlarmsPanelController() throws PacketParsingException {
		packet = new AlarmIDsPacket();
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofMillis(100);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.entry(packet);

		LinkedPacket p = new AlarmIDsPacket(packet.getAnswer());

		if (p.getPacketHeader().getPacketErrors() == PacketErrors.NO_ERROR) {

			final List<Payload> payloads = p.getPayloads();
			for(Payload pl:payloads)

				switch(pl.getParameterHeader().getParameterHeaderCode()){

				case ALARM_IDs:
					setAlarmIDs(pl);

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

	public void setTitle(String title) {
		alarmsPanel.setText(title);
	}

	private void setAlarmIDs(Payload pl) {
		alarms = pl.getArrayOfShort();
		packet.deleteObserver(observer);
		doUpdate(false);
	}

	private void addAlarmView(short alarmId) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				try {

					FXMLLoader loader = new FXMLLoader( getClass().getResource("/fxml/components/AlarmView.fxml"));
					Parent root = (Parent) loader.load();
					AlarmFieldController alarmFieldController = loader.getController();
					alarmFieldController.initialize(alarmId);
					alarmFieldController.doUpdate(true);
					vBox.getChildren().add(root);

				} catch (Exception e) {
					logger.catching(e);
				}
			}
		});
	}
}

