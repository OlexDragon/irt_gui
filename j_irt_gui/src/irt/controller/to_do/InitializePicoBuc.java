package irt.controller.to_do;

import java.awt.Component;

import javax.swing.JOptionPane;

import irt.controller.interfaces.ToDo;
import irt.data.event.ValueChangeEvent;

public class InitializePicoBuc implements ToDo {

	private Component owner;

	public InitializePicoBuc(Component owner) {
		this.owner = owner;
	}

	@Override
	public void doIt(ValueChangeEvent valueChangeEvent) {
		Object source = valueChangeEvent.getSource();
		if(source instanceof Boolean)
			JOptionPane.showMessageDialog(owner, "Initialization is completed");
	}
}
