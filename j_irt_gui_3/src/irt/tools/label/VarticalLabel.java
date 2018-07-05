package irt.tools.label;

import irt.tools.label.UI.VerticalLabelUI;

import javax.swing.JLabel;

public class VarticalLabel extends JLabel {
	private static final long serialVersionUID = 3573618664937057134L;

	public VarticalLabel(String text, boolean clockwise) {
		super(text);
		setUI(new VerticalLabelUI(clockwise));
	}
}