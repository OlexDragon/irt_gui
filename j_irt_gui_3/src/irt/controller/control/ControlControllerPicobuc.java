package irt.controller.control;

import java.util.Optional;

import irt.data.DeviceType;
import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlControllerPicobuc extends ControlController{

	public ControlControllerPicobuc(Optional<DeviceType> deviceType, LinkHeader linkHeader, MonitorPanelAbstract panel) {
		super(deviceType, "ControlControllerPicobuc", linkHeader, panel);
	}

//	@Override
//	protected ValueChangeListener addGetterValueChangeListener() {
//		return new ValueChangeListener() {
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public void valueChanged(ValueChangeEvent valueChangeEvent) {
//				SetterAbstract pw = (SetterAbstract) getPacketWork();
//
//				if(comboBoxfreqSet!= null && valueChangeEvent.getID()==pw.getPacketId()){
//					Object source = valueChangeEvent.getSource();
//
//					PacketThreadWorker packetThread = pw.getPacketThread();
//					if(source instanceof ComboBoxModel){
//						ComboBoxModel<Object> model = (ComboBoxModel<Object>) source;
//						comboBoxfreqSet.setModel(model);
//						comboBoxfreqSet.addItemListener(itemListenerComboBox);
//
//						pw.setPacketId(PacketWork.PACKET_ID_CONFIGURATION_LO);
//						pw.setPacketParameterHeaderCode(PacketImp.PARAMETER_ID_CONFIGURATION_LO_SET);
//						packetThread.preparePacket();
//						setSend(true);
//					}else{
//						comboBoxfreqSet.setSelectedItem(new IdValueForComboBox((byte) source, null));
//						if(style==Style.CHECK_ONCE)
//							setSend(false);
//						else {
//							packetThread.getPacket().getPayload(0).setBuffer(null);
//							packetThread.getPacket().getHeader().setType(PacketImp.PACKET_TYPE_REQUEST);
//							packetThread.preparePacket();
//						}
//					}
//				}
//			}
//		};
//	}
}
