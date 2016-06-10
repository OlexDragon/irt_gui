package irt.gui.controllers.components;

import java.util.Observable;

import irt.gui.data.packet.Packet;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.value.Value;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public abstract class LabelAbstract extends StartStopAbstract {

	private Value value; 	public Value getValue() { return value; } public void setValue(Value value) { this.value = value; }

	@FXML private Label labelValue;

	@Override
	public void update(Observable o, Object arg) {
//		logger.error(o);

		if(value==null)
			return;

		final LinkedPacket p = (LinkedPacket) o;
		if(checkAnswer(p))
		try {

			LinkedPacket packet = (LinkedPacket) Packet.createNewPacket(p.getClass(), p.getAnswer(), true);
			final PacketErrors packetError = packet.getPacketHeader().getPacketError();

			String text;
			if(packetError==PacketErrors.NO_ERROR){
				text = setValue(packet);
			}else
				 text = packetError.toString();

			Platform.runLater(()->{

				if(!labelValue.getText().equals(text)){
					labelValue.setText(text);
					labelValue.setTooltip(new Tooltip(text));
				}
			});

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private boolean checkAnswer(final LinkedPacket p) {
		final String tooltipText = "No answer";
		final byte[] answer = p.getAnswer();
		if(answer==null)
			Platform.runLater(()->{
				final Tooltip tooltip = labelValue.getTooltip();
				if(tooltip==null || !tooltip.getText().equals(tooltipText))
					labelValue.setTooltip(new Tooltip(tooltipText));
		});

		return answer!=null;
	}

	private String setValue(LinkedPacket packet) {
		final Payload payload = packet.getPayloads().get(0);

		long v;
		final short size = payload.getParameterHeader().getPayloadSize().getSize();

		switch(size){
		case 12:
			v = payload.getInt(2);
			break;
		case 3:
			v = payload.getShort((byte)1);
			break;
		default:
			v = payload.getShort(0);
		}

		value.setValue(v);
		return value.toString();
	}
}
