package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Observer;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.leftside.setup.SerialPortController;
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

	@FXML
	private TitledPane alarmsPanel;
	@FXML
	private VBox vBox;

	private short[] alarms;

	@FXML
	public void initialize() {

		new AlarmIDsGetter();
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.entry(packet);
	}

	public void setTitle(String title) {
		alarmsPanel.setText(title);
	}

	// ******************************************** AlarmIDsGetter
	// *********************************************************
	private final class AlarmIDsGetter extends Thread {

		public AlarmIDsGetter() {

			int priority = getPriority();
			if (priority > Thread.MIN_PRIORITY)
				setPriority(--priority);

			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {

				LinkedPacket packet = new AlarmIDsPacket();
				packet.addObserver(new AlarmFieldCreator());

				while (alarms == null) {

					SerialPortController.QUEUE.add(packet);
					Thread.sleep(100);
				}

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	// ******************************************** AlarmIDsObserver
	// *********************************************************
	private final class AlarmFieldCreator implements Observer {

		@Override
		public void update(Observable observable, Object object) {
			logger.entry(observable);

			synchronized (this) {

				if (alarms == null && observable instanceof AlarmIDsPacket) {
					try {

						getAlarmsIDs(observable);

					} catch (Exception e) {
						logger.catching(e);
					}
				}
			}
		}

		private void getAlarmsIDs(Observable observable) throws PacketParsingException {
			AlarmIDsPacket packet = new AlarmIDsPacket(((AlarmIDsPacket) observable).getAnswer());
			if (packet.getPacketHeader().getPacketErrors() == PacketErrors.NO_ERROR) {

				final Payload payload = packet.getPayloads().get(0);
				alarms = payload.getArrayOfShort();
				logger.trace("\n\t{}", alarms);

				addAlarmViews();
			}
		}

		private void addAlarmViews() {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					try {

						for (short alarmId : alarms) {
							addAlarmView(alarmId);
						}

					} catch (Exception e) {
						logger.catching(e);
					}
				}

				private void addAlarmView(short alarmId) throws Exception {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/AlarmFieldController.fxml"));
					Parent root = (Parent) loader.load();
					AlarmFieldController alarmFieldController = loader.getController();
					alarmFieldController.initialize(alarmId);
					vBox.getChildren().add(root);
				}
			});
		}
	}
}
