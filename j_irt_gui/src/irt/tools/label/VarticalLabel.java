package irt.tools.label;

import irt.tools.label.UI.VerticalLabelUI;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class VarticalLabel extends JLabel {

	public VarticalLabel(String text, boolean clockwise) {
		super(text);
		setUI(new VerticalLabelUI(clockwise));
	}
}