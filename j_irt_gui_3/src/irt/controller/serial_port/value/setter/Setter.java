package irt.controller.serial_port.value.setter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IdValue;
import irt.data.PacketThread;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.Payload;

public class Setter extends SetterAbstract {

	private int value = Integer.MIN_VALUE;

	public Setter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, groupId, packetParameterHeaderCode, packetId,
				LogManager.getLogger());
	}

	public Setter(LinkHeader linkHeader, byte packetType, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId,
				LogManager.getLogger());
	}

	public <T> Setter(LinkHeader linkHeader, byte packetType, byte groupId,	byte packetParameterHeaderCode, short packetId, T value, Logger logger) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId, value, logger);
	}

	public Setter(byte groupId, byte packetParameterHeaderCode,	short packetId) {
		this(null, groupId, packetParameterHeaderCode, packetId);
	}

	@Override
	public void preparePacketToSend(Object value) {
		getPacketThread().preparePacket((byte)((IdValue)value).getID(), (int)((IdValue)value).getValue());
	}

	public void preparePacketToSend(byte value) throws InterruptedException {
		PacketThread packetThread = getPacketThread();
		packetThread.start();
		packetThread.join();
		packetThread.preparePacket(value);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;

		if(isAddressEquals(packet)) {

			PacketHeader cph = packet.getHeader();

			PacketThread upt = getPacketThread();
			Packet up = upt.getPacket();

			if(cph!=null && up!=null &&
					cph.getGroupId()==up.getHeader().getGroupId() &&
							cph.getPacketId()==getPacketId()){

				Object source = null;

				if(cph.getOption()>0 || cph.getPacketType()!=Packet.PACKET_TYPE_RESPONSE){
					source = new Byte((byte) -cph.getOption());
					if((byte)source==0)
						source=-20;
				}else{
					Payload pl = packet.getPayload(getPacketParameterHeaderCode());

					if(pl!=null)
						switch(pl.getParameterHeader().getSize()){
						case 4:
							source = (Integer) (source = new Integer(pl.getInt(0)));
							break;
						case 0:
							source = new Boolean(true);
						}

					if(source!=null && source.hashCode()!=value){
						fireValueChangeListener(new ValueChangeEvent(source, getPacketId()));
						value = source.hashCode();
					}

					isSet = true;
				}
			}
		}
		return isSet;
	}

}
