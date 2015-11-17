package irt.gui.controllers.components;

import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.RangePacket;
import irt.gui.errors.PacketParsingException;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;

public class ValuePanel {

	@FXML private TitledPane valuePanel;
	@FXML private ValueController valueController;

	public void initialize(String title, RangePacket rangePacket, LinkedPacket valuePacket) throws PacketParsingException, InterruptedException{
		valuePanel.setText(title);
		valueController.setTitle(title);
		valueController.initialize(rangePacket, valuePacket);
	}
}
