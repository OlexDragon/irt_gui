package irt.controller.serial_port.value.seter;

import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public abstract class SetterAbstract extends GetterAbstract{

	protected int valueToSend;

	public SetterAbstract(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, groupId, packetParameterHeaderCode, packetId);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, groupId, packetParameterHeaderCode, packetId);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value,  byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId);
	}

	public abstract void preparePacketToSend(Object value);

	@Override
	public boolean set(Packet packet) {
		return false;
	}

	@Override
	public Integer getPriority() {
		return 1000;
	}
}
