package irt.controller.control;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.data.packet.configuration.GainPacket;
import irt.data.packet.configuration.GainRangePacket;

public class UnitGainController extends UnitControllerImp{

	public UnitGainController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		super(txtGain, slider, txtStep, "gain_step", new GainRangePacket(linkAddr), new GainPacket(linkAddr, null));
	}
	
}
