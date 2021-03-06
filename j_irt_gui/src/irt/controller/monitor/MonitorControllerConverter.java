package irt.controller.monitor;

import irt.controller.MeasurementController;
import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.Getter.MeasurementGetter;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.Packet;
import irt.data.value.Value;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MonitorControllerConverter extends ControllerAbstract {

	public static final int	LOCK1	= 1,
							LOCK2	= 2,
							MUTE	= 4;

	private LED ledLock;
	private LED ledMute;
	private List<ControllerAbstract> controllerList;

	private int converterType;

	public MonitorControllerConverter(JPanel monitorPanel, int converterType) {
		super(new MeasurementGetter(), monitorPanel, Style.CHECK_ONCE);
		this.converterType = converterType;
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				int id = valueChangeEvent.getID();
				if(id==getPacketWork().getPacketThread().getPacket().getHeader().getPacketId()){

					Object source = valueChangeEvent.getSource();
					if(source instanceof Long){
						Long status = (Long)source;
						boolean isNotLock1 = (status&LOCK1)==0 && converterType!=DeviceInfo.DEVICE_TYPE_L_TO_C;
						boolean isNotLock2 = (status&LOCK2)==0;
						boolean isMute = (status&MUTE) > 0;

						if(isNotLock1 || isNotLock2){
							ledLock.setLedColor(Color.RED);
							String s = (isNotLock1 ? "PLL1; ":"")+(isNotLock2 ? "PLL2;" : "");
							ledLock.setToolTipText(s);
						}else{
							ledLock.setLedColor(Color.GREEN);
							ledLock.setToolTipText("Locked");
						}

						ledMute.setOn(isMute);
						ledMute.setToolTipText("Status flags= "+status);

						fireStatusChangeListener(new ValueChangeEvent(new Integer((isNotLock1||isNotLock2 ? 0 : MonitorController.LOCK)+(isMute ? MonitorController.MUTE : 0)), PacketWork.PACKET_ID_MEASUREMENT_STATUS));
					}else
						ledLock.setToolTipText("Error:"+source);
				}
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		boolean isSet = true;
		if(controllerList==null)
			controllerList = new ArrayList<>();

		Thread t;
		switch(component.getName()){
		case "Lock":
			ledLock = (LED) component;
			break;
		case "Mute":
			ledMute = (LED) component;
			break;
		case "Input Power":
			Value value = new ValueDouble(0, 1);
			value.setPrefix(" dBm");
			ControllerAbstract abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_FCM_MEASUREMENT_INPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_INPUT_POWER);
			t = new Thread(abstractController, "Input Power");
			t.start();
			controllerList.add(abstractController);
			break;
		case "Output Power":
			value = new ValueDouble(0, 1);
			value.setPrefix(" dBm");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_FCM_MEASUREMENT_OUTPUT_POWER, value, PacketWork.PACKET_ID_MEASUREMENT_OUTPUT_POWER);
			t = new Thread(abstractController, "Input Power");
			t.start();
			controllerList.add(abstractController);
			break;
		case "Unit Temp":
			value = new ValueDouble(0, 1);
			value.setPrefix(" C");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_TEMPERATURE, value, PacketWork.PACKET_ID_MEASUREMENT_UNIT_TEMPERATURE);
			t = new Thread(abstractController, "Unit Temp");
			t.start();
			controllerList.add(abstractController);
			break;
		case "CPU Temp":
			value = new ValueDouble(0, 1);
			value.setPrefix(" C");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_TEMPERATURE_CPU, value, PacketWork.PACKET_ID_MEASUREMENT_CPU_TEMPERATURE);
			t = new Thread(abstractController, "CPU Temp");
			t.start();
			controllerList.add(abstractController);
			break;
		case "5.5V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_5V5, value, PacketWork.PACKET_ID_MEASUREMENT_5V5);
			t = new Thread(abstractController, "5.5V");
			t.start();
			controllerList.add(abstractController);
			break;
		case "13.2V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_13V2_POS, value, PacketWork.PACKET_ID_MEASUREMENT_13V2);
			t = new Thread(abstractController, "13.2V");
			t.start();
			controllerList.add(abstractController);
			break;
		case "-13.2V":
			value = new ValueDouble(0, 3);
			value.setPrefix(" V");
			abstractController = new MeasurementController((JLabel)component, Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_MON_13V2_NEG, value, PacketWork.PACKET_ID_MEASUREMENT_13V2_NEG);
			t = new Thread(abstractController, "-13.2V");
			t.start();
			controllerList.add(abstractController);
			break;
//		default:
//			isSet = false;
//			System.out.println("Not used Monitor: "+component.getName()+" : "+component.getClass().getSimpleName());
		}

		return isSet;
	}

	@Override
	protected void clear() {
		super.clear();
		for(ControllerAbstract c:controllerList)
			c.setRun(false);
		controllerList.clear();
		controllerList = null;
		ledLock = null;
		ledMute = null;
	}

}
