
package irt.gui.controllers.calibration.enums;

import irt.gui.controllers.calibration.PanelBUC;
import irt.gui.controllers.calibration.PanelPowerMeter;
import irt.gui.controllers.calibration.PanelSignalGenerator;
import irt.gui.controllers.calibration.tools.Tool;
import javafx.scene.control.CheckBox;

@SuppressWarnings("unchecked")
public enum Calibration {
	INPUT_POWER		(PanelBUC.class, PanelSignalGenerator.class),
	OUTPUT_POWER	(PanelBUC.class, PanelSignalGenerator.class, PanelPowerMeter.class),
	GAIN			(PanelBUC.class, PanelSignalGenerator.class, PanelPowerMeter.class);

	private Class<? extends Tool>[] tools; public Class<? extends Tool>[] getTools() { return tools; }

	private Calibration(Class<? extends Tool>... tools){
		this.tools = tools;
	}

	public boolean canNotBeSelected(CheckBox checkBox){

		final Object o = checkBox.getUserData();

		if(o instanceof Calibration){
			if(this == GAIN){
				if(o == GAIN)
					return false;

			}else if(o != GAIN)
				return false;		
		}

		return true;
	}
}
