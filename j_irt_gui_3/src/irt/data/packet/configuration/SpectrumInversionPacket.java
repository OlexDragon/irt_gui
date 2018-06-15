package irt.data.packet.configuration;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;

public class SpectrumInversionPacket  extends PacketAbstract{

	public enum Spectrum{
		INVERTED,
		NOT_INVERTED
	}

	public SpectrumInversionPacket() {
		super((byte)0, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_CONFIGURATION_SPECTRUM_INVERSION, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_SPECTRUM_INVERSION, null, Priority.REQUEST);
	}

	public SpectrumInversionPacket(Spectrum spectrum) {
		super((byte)0, PacketImp.PACKET_TYPE_COMMAND, PacketWork.PACKET_ID_CONFIGURATION_SET_SPECTRUM_INVERSION, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_ID_CONFIGURATION_SPECTRUM_INVERSION, new byte[]{(byte) (spectrum==Spectrum.INVERTED ? 1 : 2)}, Priority.COMMAND);
	}
}
