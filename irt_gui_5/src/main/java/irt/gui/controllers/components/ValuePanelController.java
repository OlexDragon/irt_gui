package irt.gui.controllers.components;

import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class ValuePanelController implements FieldController{

	@FXML private ValueController valueController;

	public void initialize(String title, RangePacket rangePacket, LinkedPacket valuePacket) throws PacketParsingException, InterruptedException{
		valueController.setTitle(title);
		valueController.initialize(rangePacket, valuePacket);
	}

	@Override
	public void doUpdate(boolean doUpdate) {
		valueController.doUpdate(true);
	}
}
