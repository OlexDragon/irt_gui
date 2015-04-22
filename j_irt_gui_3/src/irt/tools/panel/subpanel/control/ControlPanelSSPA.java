package irt.tools.panel.subpanel.control;

import irt.controller.control.ControlControllerPicobuc;
import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class ControlPanelSSPA extends ControlPanel {

	private JLabel lblSave;

	public ControlPanelSSPA(int deviceType, LinkHeader linkHeader, int flags) {
		super(deviceType, linkHeader, ControlPanel.FLAG_ATTENUATION|flags);
		
		Font font = Translation.getFont().deriveFont(16f)
				.deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);

		if(deviceType<DeviceInfo.DEVICE_TYPE_L_TO_KU_OUTDOOR){

			lblSave = new JLabel(Translation.getValue(String.class, "save", "SAVE"));
			lblSave.setHorizontalAlignment(SwingConstants.LEFT);
			lblSave.setForeground(Color.YELLOW);
			lblSave.setFont(font);
			int x = Translation.getValue(Integer.class, "control.label.save.x", 153);
			int y = Translation.getValue(Integer.class, "control.label.save.y", 107);
			int width = Translation.getValue(Integer.class, "control.label.save.width", 61);
			lblSave.setBounds(x, y, width, 20);
			add(lblSave);
		}
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new ControlControllerPicobuc(deviceType, getLinkHeader(),this);
	}

	@Override
	protected Point getConfigButtonPosition() {
		int x = Translation.getValue(Integer.class, "control.button.save.x", 124);
		int y = Translation.getValue(Integer.class, "control.button.save.y", 103);
		logger.trace("x={}; y={};",x, y);
		return new Point(x, y);
	}

	@Override
	protected Point getMuteButtonPosition() {
		int x = Translation.getValue(Integer.class, "control.button.mute.x", 14);
		int y = Translation.getValue(Integer.class, "control.button.mute.y", 101);
		return logger.exit(new Point(x, y));
	}

	@Override
	public void refresh() {
		super.refresh();
		Font font = Translation.getFont().deriveFont(Translation.getValue(Float.class, "control.label.mute.font.size", 12f))
				.deriveFont(Font.BOLD);
		lblSave.setText(Translation.getValue(String.class, "save", "SAVE"));
		lblSave.setFont(font);
		int x = Translation.getValue(Integer.class, "control.label.save.x", 153);
		int y = Translation.getValue(Integer.class, "control.label.save.y", 107);
		int width = Translation.getValue(Integer.class, "control.label.save.width", 61);
		lblSave.setBounds(x, y, width, 20);
	}
}
