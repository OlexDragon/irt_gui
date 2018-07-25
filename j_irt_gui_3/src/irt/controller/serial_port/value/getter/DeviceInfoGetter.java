package irt.controller.serial_port.value.getter;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.interfaces.Packet;

public class DeviceInfoGetter extends GetterAbstract{

//	private DeviceInfo deviceInfo = new DeviceInfo();

	public DeviceInfoGetter(LinkHeader linkHeader){
		super(linkHeader, PacketGroupIDs.DEVICE_INFO.getId(), PacketImp.PARAMETER_ALL, PacketIDs.DEVICE_INFO.getId());
		getPacketThread().setPriority(1);
	}

	public DeviceInfoGetter() {
		this(null);
	}

	@Override
	public boolean set(Packet packet) {
		return false;//deviceInfo.set(packet);
	}

	@Override
	public void addVlueChangeListener(ValueChangeListener valueChangeListener) {
	}

	@Override
	public void removeVlueChangeListener(ValueChangeListener valuechangelistener) {
	}
}
