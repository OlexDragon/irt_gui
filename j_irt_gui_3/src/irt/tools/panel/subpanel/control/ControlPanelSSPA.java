package irt.tools.panel.subpanel.control;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import irt.controller.control.ControlControllerPicobuc;
import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceType;
import irt.data.packet.LinkHeader;

@SuppressWarnings("serial")
public class ControlPanelSSPA extends ControlPanelImpl {

	public ControlPanelSSPA(Optional<DeviceType> deviceType, LinkHeader linkHeader, int flags) {
		super(deviceType, linkHeader, ActionFlags.toFlags(ActionFlags.FLAG_GAIN, ActionFlags.FLAG_ATTENUATION, ActionFlags.FLAG_FREQUENCY) | flags);
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> controllers = new ArrayList<>();
		controllers.add(new ControlControllerPicobuc(deviceType, getLinkHeader(),this));
		return controllers;
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
		return logger.traceExit(new Point(x, y));
	}
}
