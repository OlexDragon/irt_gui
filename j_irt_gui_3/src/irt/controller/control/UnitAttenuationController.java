package irt.controller.control;

import javax.swing.JSlider;
import javax.swing.JTextField;

import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.AttenuationRangePacket;

public class UnitAttenuationController extends UnitControllerImp{

	public UnitAttenuationController(Byte linkAddr, JTextField txtGain, JSlider slider, JTextField txtStep) {
		super(txtGain, slider, txtStep, "att_step", new AttenuationRangePacket(linkAddr), new AttenuationPacket(linkAddr, null));
	}
}
