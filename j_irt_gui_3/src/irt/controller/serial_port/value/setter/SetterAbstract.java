package irt.controller.serial_port.value.setter;

import org.apache.logging.log4j.Logger;

import irt.controller.serial_port.value.getter.GetterAbstract;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

public abstract class SetterAbstract extends GetterAbstract{

	protected int valueToSend;

	public SetterAbstract(LinkHeader linkHeader, byte groupId,	byte parameterId, short packetId, Logger logger) {
		super(linkHeader, groupId, parameterId, packetId, logger);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value, byte groupId,	byte parameterId, short packetId, Logger logger) {
		super(linkHeader, groupId, parameterId, packetId, logger);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, Object value,  byte packetType, byte groupId, byte parameterId, short packetId, Logger logger) {
		super(linkHeader, packetType, groupId, parameterId, packetId, logger);
		getPacketThread().setValue(value);
	}

	public SetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte parameterId, short packetId, Logger logger) {
		super(linkHeader, packetType, groupId, parameterId, packetId, logger);
	}

	public <T> SetterAbstract(LinkHeader linkHeader, byte packetType, byte groupId, byte parameterId, short packetId, T value, Logger logger) {
		super(linkHeader, packetType, groupId, parameterId, packetId, value, logger);
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
