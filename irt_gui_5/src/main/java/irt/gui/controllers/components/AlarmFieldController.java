package irt.gui.controllers.components;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.Logger;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.StringData;
import irt.gui.data.packet.PacketProperties;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.PacketAbstract5;
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
	public final Logger dumper = (Logger) LogManager.getLogger("dumper");
	public final Marker marker = MarkerManager.getMarker("FileWork");

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

		addPacketToSend(alarmStatus);
		addPacketToSend(alarmDescription);
		addPacketToSend(alarmName);
	}

	@Override protected void updateFields(LinkedPacket packet) throws PacketParsingException {

		LinkedPacket p = new PacketAbstract5(new PacketProperties(packet.getPacketHeader().getPacketIdDetails().getPacketId()).setHasAcknowledgment(true), packet.getAnswer()) {
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
		logger.traceEntry("{}", pl);

		removePacketToSend(alarmDescription);

		if(name==null){
			final StringData stringData = pl.getStringData();
			final String string = stringData.toString();

			Platform.runLater(()->{
				if(!string.equals(titleLabel.getText())){
					titleLabel.setText(string);
					toLog();
				}
			});
		}
	}

	private void setName(Payload pl) {
		removePacketToSend(alarmName);

		if(name==null){
			final StringData stringData = pl.getStringData();
			name = stringData.toString();

			String string;
			try {

				string = bundle.getString("alarms.name." + name);

			} catch (MissingResourceException e) {
				string = name;
				logger.catching(e);
			}

			final String s = string;
			Platform.runLater(()->{
				if(!s.equals(titleLabel.getText())){
					titleLabel.setText(s);
					toLog();
				}
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

					if(!styleClass.contains(sc)){
						styleClass.add(sc);
						toLog();
					}
				}
			});
		}
	}

	private void toLog() {
		if(name!=null && alarmSeverities!=null)
			switch(alarmSeverities){
			case CRITICAL:
				dumper.fatal(marker, toString());
				break;
			case MAJOR:
				dumper.error(marker, toString());
				break;
			case INFO:
			case NO_ALARM:
				dumper.info(marker, toString());
				break;
			case MINOR:
			case WARNING:
				dumper.warn(marker, toString());
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

	@Override
	public void doUpdate(boolean update) {
		super.doUpdate(update);
		titleLabel.getParent().setDisable(!update);
	}

	@Override
	public String toString() {
		return name + " : " + alarmSeverities;
	}
}
