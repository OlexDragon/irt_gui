package irt.controller.control;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.data.packet.FrequencyPacket;
import irt.data.packet.FrequencyRangePacket;

public class UnitFrequencyController extends UnitControllerImp{

	public UnitFrequencyController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		super(txtGain, slider, txtStep, "fr_step", new FrequencyRangePacket(linkAddr), new FrequencyPacket(linkAddr, null));
	}
	
}
