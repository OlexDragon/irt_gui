package irt.gui.controllers.components;

import java.time.Duration;
import java.util.List;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.PacketAbstract;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ValueView extends FieldsControllerAbstract {

	@FXML private Label titleLabel;
	@FXML private Label valueLabel;

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize(LinkedPacket packet){
		addLinkedPacket(packet);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.trace("\n\tENTRY: {}", packet);

		LinkedPacket p = new PacketAbstract(packet.getPacketId(), packet.getAnswer(), true) {
			@Override
			public PacketId getPacketId() {
				return null;
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

			List<Payload> payloads = p.getPayloads();
			if(payloads==null || payloads.isEmpty())
				throw new PacketParsingException("\n\t Packet parsing error:\n\t Payload is empty\n\t Sent packet: " + packet + "\n\t Resieved packet: " + p);

			Payload payload = payloads.get(0);
			int value = payload.getShort(0);
			String valueStr = Integer.toString(value);

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
}
