package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class AlarmFieldController extends FieldsControllerAbstract implements Initializable {

	@FXML private Label titleLabel;
	@FXML private Label valueLabel;

	private AlarmStatusPacket alarmStatus;
	private AlarmDescriptionPacket alarmDescription;
	private AlarmNamePacket alarmName;

	private String name;
	private ResourceBundle bundle;
//	private URL location;
	private AlarmSeverities alarmSeverities;

	@Override public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
//		this.location = location;
	}

	@Override protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void build(short alarmId) throws PacketParsingException{

		alarmStatus = new AlarmStatusPacket(alarmId);
		alarmDescription = new AlarmDescriptionPacket(alarmId);
		alarmName = new AlarmNamePacket(alarmId);

		addLinkedPacket(alarmStatus);
		addLinkedPacket(alarmDescription);
		addLinkedPacket(alarmName);
	}

	@Override protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.entry( packet);

		LinkedPacket p = new PacketAbstract(packet.getPacketHeader().getPacketIdDetails().getPacketId(), packet.getAnswer(), true) {
			@Override
			public PacketId getPacketId() {
				throw new UnsupportedOperationException("Auto-generated method stub");
			}};
		PacketErrors packetError = p.getPacketHeader().getPacketError();

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
		logger.entry(pl);

		removeLinkedPacket(alarmDescription);

		if(name==null){
			final StringData stringData = pl.getStringData();
			final String string = stringData.toString();

			Platform.runLater(()->{
				if(!string.equals(titleLabel.getText()))
					titleLabel.setText(string);
			});
		}
	}

	private void setName(Payload pl) {
		removeLinkedPacket(alarmName);

		if(name==null){
			final StringData stringData = pl.getStringData();
			name = stringData.toString();
			final String string = bundle.getString("alarms.name." + name);

			Platform.runLater(()->{
				if(!string.equals(titleLabel.getText()))
					titleLabel.setText(string);
			});
		}
	}

	private void setStatus(Payload pl) {

		byte[] value = pl.getBuffer();
		int v = value[5] & 7;
		final AlarmSeverities as = AlarmSeverities.values()[v];
		if(as!=alarmSeverities){
			removeStyleClass();
			alarmSeverities = as;
			final String al = alarmSeverities.toString();
			final String string = bundle.getString("alarms." + al);

			Platform.runLater(()->{
				if(!string.equals(valueLabel.getText())){

					valueLabel.setText(string);

					final ObservableList<String> styleClass = valueLabel.getStyleClass();
					final String sc = alarmSeverities.getStyleClass();

					if(!styleClass.contains(sc))
						styleClass.add(sc);
				}
			});
		}
	}

	private void removeStyleClass() {
		if(alarmSeverities!=null){
			final String sc = alarmSeverities.getStyleClass();
			Platform.runLater(()->{
				final ObservableList<String> styleClass = valueLabel.getStyleClass();
				styleClass.remove(sc);
			});
		}
	}

	public void setTitle(String title){
		titleLabel.setText(title);
	}
}
