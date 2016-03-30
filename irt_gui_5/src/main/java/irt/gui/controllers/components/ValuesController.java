package irt.gui.controllers.components;

import java.time.Duration;
import java.util.List;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.listeners.NumericChecker;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class ValuesController extends FieldsControllerAbstract {

	@FXML private Label		titleLabel;
	@FXML private Label		valueLabel;
	@FXML private TextField toSetTextField;
	@FXML private Slider	toSetSlider;

	@FXML public void onValueChange(){
		toSetTextField.setText(Long.toString(Math.round(toSetSlider.getValue())));
	}

	@FXML public void setValue(){
		try {

			final String text = toSetTextField.getText();
			if(valueLabel.getText().equals(text))
				logger.debug("\n\t Value did not changed:\n\t{}", text);
			else{
				AttenuationPacket packet = new AttenuationPacket(Short.parseShort(text));
				packet.addObserver(this);
				SerialPortController.QUEUE.add(packet, true);
			}

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize(RangePacket rangePacket, LinkedPacket valuePacket) throws PacketParsingException{

		addLinkedPacket(rangePacket);
		addLinkedPacket(valuePacket);

		toSetSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						toSetTextField.setText(Integer.toString(newValue.intValue()));
					}
				});
			}
		});

		final StringProperty textProperty = toSetTextField.textProperty();
		textProperty.addListener(new NumericChecker());
	}

	public void setTitle(String text){
		titleLabel.setText(text);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\t ENTRY: {}", packet);

		PacketAbstract p = new PacketAbstract(packet.getPacketHeader().getPacketIdDetails().getPacketId(), packet.getAnswer(), true) {
			@Override
			public PacketId getPacketId() {
				return null;
			} };
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

		}else{

			List<Payload> payloads = p.getPayloads();
			for(Payload pl:payloads)

				switch(pl.getParameterHeader().getParameterHeaderCode()){
				case CONF_ATTENURATION:
					setAttenuation(pl);
					break;
				case CONF_ATTENURATION_RANGE:
					setRange(pl);
				default:
				}
		}
	}

	private void setAttenuation(Payload pl) {

		short value = pl.getShort(0);
		String valueStr = Short.toString(value);

		if(!valueLabel.getText().equals(valueStr))
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					valueLabel.setText(valueStr);
				}
			});
	}

	private void setRange(Payload pl) {

		final short[] minMax = pl.getArrayOfShort();
		if(toSetSlider.getMin()!=minMax[0] || toSetSlider.getMax()!=minMax[1])
			Platform.runLater(new Runnable() {

				@Override public void run() {
					toSetSlider.setMin(minMax[0]);
					toSetSlider.setMax(minMax[1]);
				}
			});
	}
}
