package irt.tools.panel.subpanel.control;

import irt.tools.CheckBox.SwitchBox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class ControlPanelDownConverter extends ControlPanelConverter {

	public ControlPanelDownConverter() {
		super(false);
		
		JLabel lblLnb = new JLabel("DC OMT");
		lblLnb.setName("");
		lblLnb.setHorizontalAlignment(SwingConstants.LEFT);
		lblLnb.setForeground(Color.YELLOW);
		lblLnb.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblLnb.setBounds(15, 100, 43, 14);
		add(lblLnb);
		
		Image imageOn = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch1.png")).getImage();
		Image imageOff = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch2.png")).getImage();
		SwitchBox switchBox = new SwitchBox(imageOff, imageOn);
		switchBox.setName("Switch LNB");
		switchBox.setBounds(23, 113, 27, 33);
		add(switchBox);
		
		JLabel lblOff = new JLabel("OFF");
		lblOff.setName("");
		lblOff.setHorizontalAlignment(SwingConstants.LEFT);
		lblOff.setForeground(Color.YELLOW);
		lblOff.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblOff.setBounds(27, 143, 20, 14);
		add(lblOff);
	}

	@Override
	protected Point setConfigButtonPosition() {
		return new Point(60, 111);
	}

	@Override
	protected Point setMuteButtonPosition() {
		return new Point(105, 111);
	}
}
