package irt.controller.serial_port.value.getter;

import org.apache.logging.log4j.LogManager;

import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public class DeviceInfoGetter extends GetterAbstract{

//	private DeviceInfo deviceInfo = new DeviceInfo();

	public DeviceInfoGetter(LinkHeader linkHeader){
		super(linkHeader, Packet.GROUP_ID_DEVICE_INFO, Packet.IRT_SLCP_PARAMETER_ALL, PacketWork.PACKET_DEVICE_INFO,
				LogManager.getLogger());
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
