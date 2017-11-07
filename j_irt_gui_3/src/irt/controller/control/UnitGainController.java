package irt.controller.control;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.data.packet.GainPacket;
import irt.data.packet.GainRangePacket;

public class UnitGainController extends UnitControllerImp{

	public UnitGainController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		super(txtGain, slider, txtStep, "att_step", new GainRangePacket(linkAddr), new GainPacket(linkAddr, null));
	}
	
}
