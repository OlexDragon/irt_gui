package irt.data.packets.interfaces;

import irt.controllers.RegistersController;

public interface OtherFields extends ScheduledNode{

	public final String PROPERTY_STARTS_WITH = "gui.otherFields."; //Use for context menu

	public final String FIELD_KEY_ID 	= RegistersController.REGISTER_PROPERTIES + "otherFields.%d.";
	public final String FIELD_KEY 		= FIELD_KEY_ID + "%d.%d"; //gui.regicter.controller.otherFields.profileId.column.row (ex. gui.regicter.controller.otherFields.3.5.7)
}
