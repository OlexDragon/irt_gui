package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.Getter.GetterAbstract;
import irt.controller.serial_port.value.Getter.MeasurementGetter;
import irt.controller.translation.Translation;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.value.Value;

import java.awt.Component;

import javax.swing.JLabel;

public class MeasurementController extends ControllerAbstract {

	private JLabel label;
	private Value value;

	private MeasurementController(PacketWork packetWork, JLabel label, Value value) {
		super(packetWork, null, null);
		this.label = label;
		this.value = value;
	}

	/**
	 * use to get status bits
	 * @param value 
	 */
	public MeasurementController(LinkHeader linkHeader, JLabel label, Value value) {
		this(new MeasurementGetter(linkHeader), label, value);
	}

	/**
	 * 
	 * @param label - to show result
	 * @param packetPayloadParameterHeaderCode - Packet.IRT_SLCP_PARAMETER_25W_BAIS_MEASUREMENT_...
	 * @param value 
	 * @param packetId 
	 */
	public MeasurementController(JLabel label, byte packetPayloadParameterHeaderCode, Value value, short packetId) { 
		this(new MeasurementGetter(packetPayloadParameterHeaderCode, packetId), label, value);
	}

	/**
	 * 
	 * @param linkHeader - unit's address
	 * @param label - to show result
	 * @param packetPayloadParameterHeaderCode - Packet.IRT_SLCP_PARAMETER_25W_BAIS_MEASUREMENT_...
	 * @param value 
	 * @param packetId 
	 */
	public MeasurementController(LinkHeader linkHeader, JLabel label, byte packetPayloadParameterHeaderCode, Value value, short packetId) { 
		this(new MeasurementGetter(linkHeader, packetPayloadParameterHeaderCode, packetId), label, value);
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				if(valueChangeEvent.getID()==getPacketWork().getPacketThread().getPacket().getHeader().getPacketId()){
					Object source = valueChangeEvent.getSource();

					if(source instanceof Byte)	//error
						label.setText("error"+source);
					else{
						if(value!=null){
							PacketThread upt = getPacketWork().getPacketThread();
							if(upt.getValue()==null){
								value.setValue((Long)source);
								upt.setValue(value);
							}else
								value.setValue((Long)source);
						}

						long v = value.getValue();
						switch(((GetterAbstract)getPacketWork()).getPacketId()){
						case PacketWork.PACKET_ID_MEASUREMENT_BAIAS_25W_OUTPUT_POWER:
							if(v<=330){
								label.setText("<33 "+Translation.getValue(String.class, "dbm", " dBm"));
								break;
							}
						case PacketWork.PACKET_ID_MEASUREMENT_INPUT_POWER:
							if(v<=-480){
								label.setText("<-48 "+Translation.getValue(String.class, "dbm", " dBm"));
								break;
							}
						default:
							label.setText(value.toString());
						}
					}
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected void clear() {
		super.clear();
		label = null;
		value = null;
	}
}
