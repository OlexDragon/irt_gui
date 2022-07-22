package irt.data.packet;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketSuper.Priority;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public interface PacketWork extends Comparable<PacketWork>{

	

	/*		reserved for configuration from 170 to 179		*/
	/*		reserved for measurement from 180 to 189		*/

	public Priority 			getPriority();
	public PacketThreadWorker 	getPacketThread();
	public void 				addVlueChangeListener(ValueChangeListener valueChangeListener);
	public void 				removeVlueChangeListener(ValueChangeListener valuechangelistener);
	public boolean 				set(Packet packet);
	public void 				clear();
	public void 				removeVlueChangeListeners();
	public boolean 				isAddressEquals(Packet packet);
}
