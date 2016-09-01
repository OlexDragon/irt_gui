package irt.data.packets.interfaces;

import java.net.URL;

public interface FxmlNode {
	public final String FIELD_KEY_ID 	= "gui.node.%d.";			//gui.node.profileId. (ex. gui.node.3.)
	public final String FIELD_KEY 		= FIELD_KEY_ID + "%d.%d"; 	//gui.node.profileId.column.row (ex. gui.node.3.5.7)

	URL getLocation();
}
