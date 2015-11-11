package irt.controller.serial_port.value.setter;

import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public abstract class SetterAbstract extends GetterAbstract{

	protected int valueToSend;

	public SetterAbstract(LinkHeader linkHeader, byte groupId,	byte parameterId, short packetId) {
		super(linkHeader, groupId, parameterId, packetId);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value, byte groupId,	byte parameterId, short packetId) {
		super(linkHeader, groupId, parameterId, packetId);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value,  byte packetType, byte groupId, byte parameterId, short packetId) {
		super(linkHeader, packetType, groupId, parameterId, packetId);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte parameterId, short packetId) {
		super(linkHeader, packetType, groupId, parameterId, packetId);
	}

	public <T> SetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte parameterId, short packetId, T value) {
		super(linkHeader, packetType, groupId, parameterId, packetId, value);
	}

	public abstract void preparePacketToSend(Object value);

	@Override
	public boolean set(Packet packet) {
		return false;
	}

	@Override
	public int getPriority() {
		return 1000;
	}
}
