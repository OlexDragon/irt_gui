package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.util.Optional;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import irt.tools.CheckBox.LnbReferenceSwitch;
import irt.data.DeviceType;
import irt.tools.CheckBox.LnbPowerSwitch;
import irt.tools.CheckBox.SpectrumInversionSwitch;
import irt.tools.CheckBox.SwitchBox;

@SuppressWarnings("serial")
public class ControlPanelDownConverter extends ControlPanelConverter {

	public ControlPanelDownConverter(Optional<DeviceType> deviceType) {
		super(deviceType, false);

		JLabel lblLnb = new JLabel("LNB");
		lblLnb.setName("");
		lblLnb.setHorizontalAlignment(SwingConstants.LEFT);
		lblLnb.setForeground(Color.YELLOW);
		lblLnb.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblLnb.setBounds(22, 100, 20, 14);
		add(lblLnb);

		Image imageOn = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch1.png")).getImage();
		Image imageOff = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/switch2.png")).getImage();
		SwitchBox lnbPowerSwitch = new LnbPowerSwitch(imageOff, imageOn);
		lnbPowerSwitch.setToolTipText("Power");
//		switchBox.setName("Switch LNB");
		lnbPowerSwitch.setBounds(5, 113, 27, 33);
		add(lnbPowerSwitch);

		SwitchBox lnb10MHzSwitch = new LnbReferenceSwitch(imageOff, imageOn);
		lnb10MHzSwitch.setToolTipText("10 MHz");
		lnb10MHzSwitch.setBounds(31, 113, 27, 33);
		add(lnb10MHzSwitch);
		
		JLabel lblInversion = new JLabel("INV.");
		lblInversion.setName("");
		lblInversion.setHorizontalAlignment(SwingConstants.LEFT);
		lblInversion.setForeground(Color.YELLOW);
		lblInversion.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblInversion.setBounds(73, 100, 43, 14);
		add(lblInversion);

		SwitchBox switchInvertion = new SpectrumInversionSwitch(imageOff, imageOn);
		switchInvertion.setToolTipText("Spectrum Inversion");
		switchInvertion.setBounds(70, 113, 27, 33);
		add(switchInvertion);

		JLabel lblOff = new JLabel("OFF");
		lblOff.setName("");
		lblOff.setHorizontalAlignment(SwingConstants.LEFT);
		lblOff.setForeground(Color.YELLOW);
		lblOff.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblOff.setBounds(22, 143, 20, 14);
		add(lblOff);

		remove(getLblMute());
	}

	@Override
	protected Point getConfigButtonPosition() {
		return new Point(110, 111);
	}

	@Override
	protected Point getMuteButtonPosition() {
		return new Point(150, 111);
	}
}
