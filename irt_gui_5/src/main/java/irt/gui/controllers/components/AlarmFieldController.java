package irt.gui.controllers.components;

import java.time.Duration;
import java.util.List;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.StringData;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.alarms.AlarmDescriptionPacket;
import irt.gui.data.packet.observable.alarms.AlarmNamePacket;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket.AlarmSeverities;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AlarmFieldController extends FieldsControllerAbstract {

	@FXML private Label titleLabel;
	@FXML private Label valueLabel;


	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize(short alarmId) throws PacketParsingException{

		addLinkedPacket(new AlarmStatusPacket(alarmId));
		addLinkedPacket(new AlarmDescriptionPacket(alarmId));
		addLinkedPacket(new AlarmNamePacket(alarmId));
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\tENTRY: {}", packet);

		LinkedPacket p = new PacketAbstract(packet.getPacketHeader().getPacketIdDetails().getPacketId(), packet.getAnswer()) {
			@Override
			public PacketId getPacketId() {
				throw new UnsupportedOperationException("Auto-generated method stub");
			}};
		PacketErrors packetError = p.getPacketHeader().getPacketErrors();
		logger.trace("\n\t Received packet:{}", p);

		if(packetError!=PacketErrors.NO_ERROR){
			final String error = packetError.toString();

			if(!valueLabel.getText().equals(error))
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							valueLabel.setText(error);
						}
					});

			logger.warn("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);

		}else{

			final List<Payload> payloads = p.getPayloads();
			for(Payload pl:payloads)

				switch(pl.getParameterHeader().getParameterHeaderCode()){

				case ALARM_DESCRIPTION:
					setDescription(pl);
					break;
				case ALARM_NAME:
					setName(pl);
					break;
				case ALARM_STATUS:
					setStatus(pl);
					break;

				case ALARM_IDs:
				case ALARM_CONFIG:
				case ALARM_SUMMARY_STATUS:
				default:
				}

			
			
	
		}
	}

	private void setDescription(Payload pl) {
		final StringData stringData = pl.getStringData();
		logger.debug("\n\t Result: {}", stringData);

		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				titleLabel.setText(stringData.toString());
			}
		});
	}

	private void setName(Payload pl) {
		final StringData stringData = pl.getStringData();
		logger.debug("\n\t Result: {}", stringData);
	}

	private void setStatus(Payload pl) {

		byte[] value = pl.getBuffer();
		int v = value[5] & 7;
		String valueStr = AlarmSeverities.values()[v].toString();

		if(!valueLabel.getText().equals(valueStr))
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					valueLabel.setText(valueStr);
				}
			});
	}

	public void setTitle(String title){
		titleLabel.setText(title);
	}
}
