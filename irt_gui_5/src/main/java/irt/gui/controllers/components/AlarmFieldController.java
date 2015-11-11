package irt.gui.controllers.components;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.StringData;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.alarms.AlarmDescriptionPacket;
import irt.gui.data.packet.observable.alarms.AlarmNamePacket;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket;
import irt.gui.data.packet.observable.alarms.AlarmStatusPacket.AlarmSeverities;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class AlarmFieldController extends FieldsControllerAbstract {

	@FXML private Label titleLabel;
	@FXML private Label valueLabel;

	public void initialize(short alarmId) throws PacketParsingException{

		AlarmStatusPacket packet = new AlarmStatusPacket(alarmId);
		packet.addObserver(this);
		packetSender.addPacketToSend(packet);
		packetSender.setSend(true);
		ScheduledServices.services.scheduleAtFixedRate(packetSender, 1, 3, TimeUnit.SECONDS);

		new AlarmNameGetter(alarmId);
		new AlarmDescriptionGetter(alarmId);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\tENTRY: {}", packet);

		LinkedPacket p = new AlarmStatusPacket(packet.getAnswer());
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

			List<Payload> payloads = p.getPayloads();
			if(payloads==null || payloads.isEmpty())
				throw new PacketParsingException("\n\t Packet parsing error:\n\t Payload is empty\n\t Sent packet: " + packet + "\n\t Resieved packet: " + p);

			Payload payload = payloads.get(0);
			byte[] value = payload.getBuffer();
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
	}

	public void setTitle(String title){
		titleLabel.setText(title);
	}

	//****************************************   AlarmNameGetter   *********************************************
	private class AlarmNameGetter extends Thread{

		private short alarmId;

		public AlarmNameGetter(short alarmId) {
			this.alarmId = alarmId;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(--priority);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {

				AlarmNamePacket packet = new AlarmNamePacket(alarmId);

				packet.addObserver(new AlarmNameObserver());
				SerialPortController.QUEUE.add(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}


		//****************************************   AlarmNameObserver   *********************************************
		private final class AlarmNameObserver implements Observer {
			@Override
			public void update(Observable observable, Object object) {
				logger.entry(observable);

				if(observable instanceof AlarmNamePacket)
					new AlarmNameFiller((AlarmNamePacket)observable);
			}


			//****************************************   AlarmNameFiller   *********************************************
			public class AlarmNameFiller extends Thread{

				private AlarmNamePacket packet;

				public AlarmNameFiller(AlarmNamePacket packet) {
					this.packet = packet;
					int priority = getPriority();
					if(priority>Thread.MIN_PRIORITY)
						setPriority(--priority);
					setDaemon(true);
					start();
				}

				@Override
				public void run() {
					try {

						LinkedPacket p = new AlarmNamePacket(packet.getAnswer());

						final PacketErrors packetErrors = p.getPacketHeader().getPacketErrors();
						if(packetErrors==PacketErrors.NO_ERROR){

							final Payload payload = p.getPayloads().get(0);
							final StringData stringData = payload.getStringData();
							logger.debug("\n\t Result: {}", stringData);

							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									titleLabel.setText(stringData.toString());
								}
							});
						}else
							logger.warn("\n\tpacketErrors: {}\n\t sent:{}\n\treceived:{}", packetErrors, packet, p);

					} catch (Exception e) {

						logger.catching(e);
					}
				}

			}
		}
	}

	//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   AlarmDescriptionGetter   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	private class AlarmDescriptionGetter extends Thread{

		private short alarmId;

		public AlarmDescriptionGetter(short alarmId) {
			this.alarmId = alarmId;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(--priority);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			try {

				AlarmDescriptionPacket packet = new AlarmDescriptionPacket(alarmId);
				packet.addObserver(new AlarmDescriptioneObserver());
				SerialPortController.QUEUE.add(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}

		//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   AlarmDescriptioneObserver   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		public class AlarmDescriptioneObserver implements Observer {

			@Override
			public void update(Observable observable, Object object) {
				logger.entry(observable);

				if(observable instanceof AlarmDescriptionPacket)
					new AlarmDescriptionFiller((AlarmDescriptionPacket)observable);
			}

			//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   AlarmDescriptionFiller   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
			public class AlarmDescriptionFiller extends Thread{

				private AlarmDescriptionPacket packet;

				public AlarmDescriptionFiller(AlarmDescriptionPacket packet) {
					this.packet = packet;
					int priority = getPriority();
					if(priority>Thread.MIN_PRIORITY)
						setPriority(--priority);
					setDaemon(true);
					start();
				}

				@Override
				public void run() {
					try {

						LinkedPacket p = new AlarmDescriptionPacket(packet.getAnswer());

						final PacketErrors packetErrors = p.getPacketHeader().getPacketErrors();
						if(packetErrors==PacketErrors.NO_ERROR){

							final Payload payload = p.getPayloads().get(0);
							final StringData stringData = payload.getStringData();
							logger.debug("\n\t Result: {}", stringData);

							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									titleLabel.setTooltip(new Tooltip(stringData.toString()));
								}
							});
						}else
							logger.warn("\n\tpacketErrors: {}\n\t sent:{}\n\treceived:{}", packetErrors, packet, p);

					} catch (Exception e) {

						logger.catching(e);
					}
				}

			}
		}
	}
}
