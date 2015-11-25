package irt.tools.panel.subpanel.monitor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.value.Value;
import irt.data.value.ValueBoolean;
import irt.data.value.ValueDouble;
import irt.data.value.ValueThreeState;


public class MonitorDownlinkRedundancySystem extends MonitorPanelAbstract {
	private static final long serialVersionUID = 1L;
	private JLabel lblTemperature;
	private JLabel lblWGSPosition;
	private JLabel lblLNB1Status;
	private JLabel lblLNB2Status;
	private JLabel labelPosition;
	private JLabel labelLNB1Status;
	private JLabel labelLNB2Status;

	public MonitorDownlinkRedundancySystem(int deviceType, LinkHeader linkHeader) {
		super(deviceType, linkHeader, Translation.getValue(String.class, "monitor", "IrtControllPanel"), 250, 210);
		
		lblTemperature = new JLabel(":");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.YELLOW);
		lblTemperature.setBounds(119, 38, 100, 17);
		add(lblTemperature);
		
		JLabel labelTemperature = new JLabel(Translation.getValue(String.class, "temperature", "Temperature"));
		labelTemperature.setName("temperature");
		labelTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTemperature.setForeground(new Color(153, 255, 255));
		labelTemperature.setBounds(22, 38, 104, 17);
		add(labelTemperature);
		
		lblWGSPosition = new JLabel(":");
		lblWGSPosition.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWGSPosition.setForeground(Color.WHITE);
		lblWGSPosition.setBounds(119, 74, 100, 17);
		add(lblWGSPosition);
		
		JLabel labelWGS = new JLabel("WGS");
		labelWGS.setHorizontalAlignment(SwingConstants.RIGHT);
		labelWGS.setForeground(new Color(153, 255, 255));
		labelWGS.setBounds(22, 74, 40, 17);
		add(labelWGS);
		
		labelPosition = new JLabel(Translation.getValue(String.class, "position", "Position"));
		labelPosition.setName("position");
		labelPosition.setHorizontalAlignment(SwingConstants.RIGHT);
		labelPosition.setForeground(new Color(153, 255, 255));
		labelPosition.setBounds(62, 74, 64, 17);
		add(labelPosition);
		
		lblLNB1Status = new JLabel(":");
		lblLNB1Status.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLNB1Status.setForeground(Color.WHITE);
		lblLNB1Status.setBounds(119, 110, 100, 17);
		add(lblLNB1Status);
		
		JLabel labelLNB1 = new JLabel("LNB1");
		labelLNB1.setHorizontalAlignment(SwingConstants.RIGHT);
		labelLNB1.setForeground(new Color(153, 255, 255));
		labelLNB1.setBounds(22, 110, 40, 17);
		add(labelLNB1);
		
		String txtStatus = Translation.getValue(String.class, "status", "Status");
		labelLNB1Status = new JLabel(txtStatus);
		labelLNB1Status.setName("status");
		labelLNB1Status.setHorizontalAlignment(SwingConstants.RIGHT);
		labelLNB1Status.setForeground(new Color(153, 255, 255));
		labelLNB1Status.setBounds(62, 110, 64, 17);
		add(labelLNB1Status);
		
		lblLNB2Status = new JLabel(":");
		lblLNB2Status.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLNB2Status.setForeground(Color.WHITE);
		lblLNB2Status.setBounds(119, 146, 100, 17);
		add(lblLNB2Status);
		
		JLabel labelLNB2 = new JLabel("LNB2");
		labelLNB2.setHorizontalAlignment(SwingConstants.RIGHT);
		labelLNB2.setForeground(new Color(153, 255, 255));
		labelLNB2.setBounds(22, 146, 40, 17);
		add(labelLNB2);
		
		labelLNB2Status = new JLabel(txtStatus);
		labelLNB2Status.setName("status");
		labelLNB2Status.setHorizontalAlignment(SwingConstants.RIGHT);
		labelLNB2Status.setForeground(new Color(153, 255, 255));
		labelLNB2Status.setBounds(62, 146, 64, 17);
		add(labelLNB2Status);
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> controllers = new ArrayList<>();
		controllers.add(getTemperaturController());
		controllers.add(getWGSController());
		controllers.add(getLNBStatusController(lblLNB1Status, PacketImp.PARAMETER_MEASUREMENT_LNB1_STATUS, PacketWork.PACKET_ID_MEASUREMENT_SNB1_STATUS));
		controllers.add(getLNBStatusController(lblLNB2Status, PacketImp.PARAMETER_MEASUREMENT_LNB2_STATUS, PacketWork.PACKET_ID_MEASUREMENT_SNB2_STATUS));
		return controllers;
	}


	private ControllerAbstract getLNBStatusController(final JLabel lblLNBStatus, byte parameterMeasurement, final short packetIdMeasurement) {

		Getter getter = new Getter(linkHeader, PacketImp.GROUP_ID_MEASUREMENT, parameterMeasurement, packetIdMeasurement, logger){

			private final Value value = new ValueBoolean(Translation.getValue(String.class, "ready.not", "Not Ready"), Translation.getValue(String.class, "ready", "Ready"));

			@Override
			public boolean set(Packet packet) {
				if(packet.getHeader().getPacketId()==packetIdMeasurement){
					
					Payload payload = packet.getPayload(0);
					if(payload!=null){
						byte v = payload.getByte();
						if(v!=value.getValue() || value.getOldValue()==null){
							value.setValue(v);
							lblLNBStatus.setText(value.toString());
							if(v==0)
								lblLNBStatus.setForeground(Color.WHITE);
							else
								lblLNBStatus.setForeground(Color.YELLOW);
						}
					}
					
				}
				return true;
			}
			
		};

		return new DefaultController(deviceType, "MEASUREMENT_LNB", getter, Style.CHECK_ALWAYS);
	}

	private ControllerAbstract getWGSController() {

		Getter getter = new Getter(linkHeader, PacketImp.GROUP_ID_MEASUREMENT, PacketImp.PARAMETER_MEASUREMENT_WGS_POSITION, PacketWork.PACKET_ID_MEASUREMENT_WGS_POSITION, logger){

			private final ValueThreeState value = new ValueThreeState("Unknown", "LNB1", "LNB2");

			@Override
			public boolean set(Packet packet) {
				if(packet.getHeader().getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_WGS_POSITION){
					
					Payload payload = packet.getPayload(0);
					if(payload!=null){
						byte v = payload.getByte();
						if(v!=value.getValue() || value.getOldValue()==null){
							value.setValue(v);
							lblWGSPosition.setText(value.toString());
							if(v==0)
								lblWGSPosition.setForeground(Color.WHITE);
							else
								lblWGSPosition.setForeground(Color.YELLOW);
						}
					}
					
				}
				return true;
			}
			
		};

		return new DefaultController(deviceType, "WGS_POSITION", getter, Style.CHECK_ALWAYS);
	}

	private ControllerAbstract getTemperaturController() {

		Getter getter = new Getter(linkHeader, PacketImp.GROUP_ID_MEASUREMENT, PacketImp.PARAMETER_MEASUREMENT_TEMPERATURE, PacketWork.PACKET_ID_MEASUREMENT_TEMPERATURE, logger){

			@Override
			public boolean set(Packet packet) {
				if(packet.getHeader().getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_TEMPERATURE){
					Payload payload = packet.getPayload(0);
					if(payload!=null){
						Value v = new ValueDouble(payload.getShort(0), 1);
						v.setPrefix(" C");
						lblTemperature.setText(v.toString());
					}
					
				}
				return true;
			}
			
		};

		return new DefaultController(deviceType, "DLRS Temperatur", getter, Style.CHECK_ALWAYS);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		labelPosition.setText(Translation.getValue(String.class, "position", "Position"));
		
		String txtStatus = Translation.getValue(String.class, "status", "Status");
		labelLNB1Status.setText(txtStatus);
		labelLNB2Status.setText(txtStatus);
	}

	@Override
	protected void packetRecived(List<Payload> payloads) {
	}
}
