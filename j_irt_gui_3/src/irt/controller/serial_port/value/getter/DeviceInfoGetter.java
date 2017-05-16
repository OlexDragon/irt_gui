package irt.controller.serial_port.value.getter;

import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class DeviceInfoGetter extends GetterAbstract{

//	private DeviceInfo deviceInfo = new DeviceInfo();

	public DeviceInfoGetter(LinkHeader linkHeader){
		super(linkHeader, PacketImp.GROUP_ID_DEVICE_INFO, PacketImp.PARAMETER_ALL, PacketWork.PACKET_ID_DEVICE_INFO);
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
