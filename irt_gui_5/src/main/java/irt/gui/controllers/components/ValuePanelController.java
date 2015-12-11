package irt.gui.controllers.components;

import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;

public class ValuePanelController implements FieldController{

	@FXML private ValuesController valuesController;

	public void initialize(String title, RangePacket rangePacket, LinkedPacket valuePacket) throws PacketParsingException, InterruptedException{
		valuesController.setTitle(title);
		valuesController.initialize(rangePacket, valuePacket);
	}

	@Override
	public void doUpdate(boolean doUpdate) {
		valuesController.doUpdate(true);
	}
}
