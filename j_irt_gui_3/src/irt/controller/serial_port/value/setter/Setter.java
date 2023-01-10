package irt.controller.serial_port.value.setter;

import java.util.Optional;

import irt.data.IdValue;
import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public class Setter extends SetterAbstract {

	private int value = Integer.MIN_VALUE;

	public Setter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, PacketID packetId) {
		super(linkHeader, groupId, packetParameterHeaderCode, packetId.getId());
	}

	public Setter(LinkHeader linkHeader, byte packetType, byte groupId,	byte packetParameterHeaderCode, PacketID packetID) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetID.getId());
	}

	public <T> Setter(LinkHeader linkHeader, byte packetType, byte groupId,	byte packetParameterHeaderCode, PacketID packetID, T integer) {
		super(linkHeader, packetType, groupId, packetParameterHeaderCode, packetID.getId(), integer);
	}

	public Setter(byte groupId, byte packetParameterHeaderCode,	PacketID packetId) {
		this(null, groupId, packetParameterHeaderCode, packetId);
	}

	@Override
	public void preparePacketToSend(Object value) {
		getPacketThread().preparePacket((byte)((IdValue)value).getID(), (int)((IdValue)value).getValue());
	}

	public void preparePacketToSend(byte value) throws InterruptedException {
		PacketThreadWorker packetThread = getPacketThread();
		packetThread.start();
		packetThread.join();
		packetThread.preparePacket(value);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;

		if(isAddressEquals(packet)) {

			PacketHeader cph = packet.getHeader();

			PacketThreadWorker upt = getPacketThread();
			Packet up = upt.getPacket();

			final short intId = getPacketId();
			final PacketID[] values = PacketID.values();
			PacketID packetID = Optional.of(intId).filter(i->i>values.length).map(i->values[i]).orElse(PacketID.UNNECESSARY);
			if(cph!=null && up!=null &&
					cph.getGroupId()==up.getHeader().getGroupId() &&
							cph.getPacketId()==intId){

				Object source = null;

				if(cph.getError()>0 || cph.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE){
					source = new Byte((byte) -cph.getError());
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
						fireValueChangeListener(new ValueChangeEvent(source, packetID));
						value = source.hashCode();
					}

					isSet = true;
				}
			}
		}
		return isSet;
	}

}
