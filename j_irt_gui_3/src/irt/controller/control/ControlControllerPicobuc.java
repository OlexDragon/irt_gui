package irt.controller.control;

import irt.controller.serial_port.value.setter.SetterAbstract;
import irt.data.IdValueForComboBox;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

import javax.swing.ComboBoxModel;

import org.apache.logging.log4j.Logger;

public class ControlControllerPicobuc extends ControlController{

	public ControlControllerPicobuc(int deviceType, LinkHeader linkHeader, MonitorPanelAbstract panel, Logger logger) {
		super(deviceType, "ControlControllerPicobuc", linkHeader, panel, logger);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				SetterAbstract pw = (SetterAbstract) getPacketWork();

				if(comboBoxfreqSet!= null && valueChangeEvent.getID()==pw.getPacketId()){
					Object source = valueChangeEvent.getSource();

					PacketThread packetThread = pw.getPacketThread();
					if(source instanceof ComboBoxModel){
						ComboBoxModel<Object> model = (ComboBoxModel<Object>) source;
						comboBoxfreqSet.setModel(model);
						comboBoxfreqSet.addItemListener(itemListenerComboBox);

						pw.setPacketId(PacketWork.PACKET_ID_CONFIGURATION_LO_BIAS_BOARD);
						pw.setPacketParameterHeaderCode(Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_LO_SET);
						packetThread.preparePacket();
						setSend(true);
					}else{
						comboBoxfreqSet.setSelectedItem(new IdValueForComboBox((byte) source, null));
						if(style==Style.CHECK_ONCE)
							setSend(false);
						else {
							packetThread.getPacket().getPayload(0).setBuffer(null);
							packetThread.getPacket().getHeader().setType(Packet.PACKET_TYPE_REQUEST);
							packetThread.preparePacket();
						}
					}
				}
			}
		};
	}
}
