package irt.gui.controllers.components;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class ValueController extends FieldsControllerAbstract {

	@FXML private Label		titleLabel;
	@FXML private Label		valueLabel;
	@FXML private TextField toSetTextField;
	@FXML private Slider	toSetSlider;

	public void initialize(LinkedPacket rangePacket, LinkedPacket valuePacket) throws PacketParsingException{

		rangePacket.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object object) {
				logger.entry(observable);
				final Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(observable instanceof LinkedPacket){

							LinkedPacket lp = (LinkedPacket)observable;
							try {

								LinkedPacket p = new PacketAbstract(lp.getPacketHeader().getPacketIdDetails().getPacketId(), lp.getAnswer()) { };
								logger.trace("\n\tReceived packet: {}", p);

								if(p.getPacketHeader().getPacketErrors()==PacketErrors.NO_ERROR){
									setMinMax(p);

									packetSender.addPacketToSend(valuePacket);
									packetSender.setSend(true);
									ScheduledServices.services.scheduleAtFixedRate(packetSender, 1, 3, TimeUnit.SECONDS);

								}

							} catch (Exception e) {
								logger.catching(e);
							}
						}
					}

					private void setMinMax(LinkedPacket p) {
						Payload pl = p.getPayloads().get(0);
						final short min = pl.getShort(0);
						final short max = pl.getShort((byte)2);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								toSetSlider.setMin(min);
								toSetSlider.setMax(max);
							}
						});
					}
				});
				int priority = t.getPriority();
				if(priority>Thread.MIN_PRIORITY)
					t.setPriority(--priority);
				t.setDaemon(true);
				t.start();
			}
		});

		SerialPortController.QUEUE.add(rangePacket);

		toSetSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				toSetTextField.setText(Integer.toString(newValue.intValue()));
			}
		});

		toSetTextField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!newValue.matches("\\d*"))
					toSetTextField.setText(oldValue);
			}
		});
	}

	@FXML public void onValueChange(){
		toSetTextField.setText(Long.toString(Math.round(toSetSlider.getValue())));
	}

	@FXML public void setValue(){
		try {
			setValue(toSetTextField.getText());
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void setValue(String text) throws NumberFormatException, PacketParsingException {
		if(valueLabel.getText().equals(text))
			logger.debug("\n\t Value did not changed:\n\t{}", text);
		else{
			AttenuationPacket packet = new AttenuationPacket(Short.parseShort(text));
			packet.addObserver(this);
			SerialPortController.QUEUE.add(packet);
		}
	}

	public void setTitle(String text){
		titleLabel.setText(text);
	}
	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\t ENTRY: {}", packet);

		PacketAbstract p = new PacketAbstract(packet.getPacketHeader().getPacketIdDetails().getPacketId(), packet.getAnswer()) { };
		PacketErrors packetError = p.getPacketHeader().getPacketErrors();


		if(packetError!=PacketErrors.NO_ERROR){
			final String error = packetError.toString();

			if(!valueLabel.getText().equals(error))
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							valueLabel.setText(error);
						}
					});

			logger.error("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);

		}else{

			List<Payload> payloads = p.getPayloads();
			if(payloads==null || payloads.isEmpty())
				throw new PacketParsingException("\n\t Packet parsing error:\n\t Payload is empty\n\t Sent packet: " + packet + "\n\t Resieved packet: " + p);

			Payload payload = payloads.get(0);
			short value = payload.getShort(0);
			String valueStr = Short.toString(value);

			if(!valueLabel.getText().equals(valueStr))
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						valueLabel.setText(valueStr);
					}
				});
		}
	}
}
