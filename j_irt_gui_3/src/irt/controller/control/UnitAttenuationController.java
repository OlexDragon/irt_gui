package irt.controller.control;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.data.packet.AttenuationPacket;
import irt.data.packet.AttenuationRangePacket;

public class UnitAttenuationController extends UnitControllerImp{

	public UnitAttenuationController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		super(txtGain, slider, txtStep, "gain_step", new AttenuationRangePacket(linkAddr), new AttenuationPacket(linkAddr, null));
	}
	
}
