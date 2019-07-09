package irt.tools.panel.subpanel.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.control.ControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo.DeviceType;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.value.ValueDouble;
import irt.tools.label.LED;

@SuppressWarnings("serial")
public class MonitorPanelSSPA extends MonitorPanelAbstract implements Monitor {

	protected static final Logger logger = LogManager.getLogger();

	public static final byte
			IRT_SLCP_PACKET_ID_MEASUREMENT = PacketGroupIDs.MEASUREMENT.getId();

	public static final int
			MUTE = 1,
			LOCK = 2;

	protected LED ledMute;
	protected JLabel lblOutputPowerTxt;
	protected JLabel lblTemperatureTxt;
	protected JLabel lblOutputPower;
	protected JLabel lblTemperature;
	private boolean isSSPA;

	private int temperature;
	private int output;

	private int status;

	public MonitorPanelSSPA(Optional<DeviceType> deviceType, LinkHeader linkHeader) {
		super(deviceType, linkHeader, Translation.getValue(String.class, "monitor", "IrtControllPanel"), 214, 210);
		
		isSSPA = getClass().equals(MonitorPanelSSPA.class);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Mute");
		ledMute.setForeground(Color.GREEN);
		add(ledMute);

		if(deviceType!=null)
		deviceType
		.filter(dt->dt!=DeviceType.CONVERTER_L_TO_KU_OUTDOOR)
		.ifPresent(dt->{
			lblOutputPower = new JLabel(":");
			lblOutputPower.setName("Output Power");
			lblOutputPower.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOutputPower.setForeground(Color.WHITE);
			lblOutputPower.setBounds(99, 50, 100, 17);
			add(lblOutputPower);

			lblOutputPowerTxt = new JLabel();
			lblOutputPowerTxt.setName("");
			lblOutputPowerTxt.setHorizontalAlignment(SwingConstants.RIGHT);
			lblOutputPowerTxt.setForeground(new Color(153, 255, 255));
			lblOutputPowerTxt.setBounds(2, 50, 104, 17);
			add(lblOutputPowerTxt);
			new TextWorker(lblOutputPowerTxt, "output_power", "Output Power")
					.execute();
		});

		lblTemperature = new JLabel(":");
		lblTemperature.setName("Temperature");
		lblTemperature.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperature.setForeground(Color.WHITE);
		lblTemperature.setBounds(99, 78, 100, 17);
		add(lblTemperature);

		lblTemperatureTxt = new JLabel();
		lblTemperatureTxt.setName("");
		lblTemperatureTxt.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTemperatureTxt.setForeground(new Color(153, 255, 255));
		lblTemperatureTxt.setBounds(2, 78, 104, 17);
		add(lblTemperatureTxt);
		new TextWorker(lblTemperatureTxt, "temperature", "Temperature").execute();

		if(isSSPA)
			swingWorkers();
	}

	//Set 'ledMute' font 
	protected void swingWorkers() {
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				return Translation.getFont().deriveFont(Translation.getValue(Float.class, "monitor.leds.font.size", 12f))
						.deriveFont(Translation.getValue(Integer.class, "monitor.leds.font.style", Font.PLAIN));
			}
			@Override
			protected void done() {
				try {
					Font font = get();
					ledMute.setFont(font);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		//Set LABELS font
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				return Translation.getFont()
						.deriveFont(Translation.getValue(Float.class, "monitor.labels.font.size", 12f))
						.deriveFont(Translation.getValue(Integer.class, "monitor.labels.font.style", Font.PLAIN));
			}
			@Override
			protected void done() {
				try{
				Font font = get();
				deviceType
				.filter(dt->dt!=DeviceType.CONVERTER_L_TO_KU_OUTDOOR)
				.ifPresent(dt->{
					lblOutputPower.setFont(font);
					lblOutputPowerTxt.setFont(font);
				});
				lblTemperature.setFont(font);
				lblTemperatureTxt.setFont(font);
				}catch(Exception e){
					logger.catching(e);
				}
			}
		}.execute();

		//Set 'ledMute' Bounds
		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				return new Rectangle(
						  Translation.getValue(Integer.class, isSSPA ? "SSPA.monitor.led.mute.x" : "monitor.led.mute.x", 17)
						, Translation.getValue(Integer.class, isSSPA ? "SSPA.monitor.led.mute.y" : "monitor.led.mute.y", 138)
						, Translation.getValue(Integer.class, "monitor.led.mute.width", 100)
						, 28);
			}

			@Override
			protected void done() {
				try {
					ledMute.setBounds(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();
	}

	@Override
	public void refresh() {
		super.refresh();
		logger.traceEntry();

		titledBorder.setTitle(Translation.getValue(String.class, "monitor", "IrtControllPanel"));

		new TextWorker(ledMute, "mute", "MUTE").execute();
		new TextWorker(lblOutputPowerTxt, "output_power", "Output Power").execute();
		new TextWorker(lblTemperatureTxt, "temperature", "Temperature").execute();

		swingWorkers();

		logger.traceExit();
	}

	private static final String[] controllerNames = new String[]{"Measurement_temperature", "Measurement_InputPower","Status"};
	private static final byte[] parameters = new byte[]{PacketImp.PARAMETER_MEASUREMENT_TEMPERATURE, PacketImp.PARAMETER_MEASUREMENT_FCM_INPUT_POWER, PacketImp.PARAMETER_MEASUREMENT_FCM_STATUS};
	private static final PacketIDs[] pacetId = new PacketIDs[]{PacketIDs.MEASUREMENT_UNIT_TEMPERATURE, PacketIDs.FCM_ADC_INPUT_POWER, PacketIDs.MEASUREMENT_STATUS};
	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> controllers = new ArrayList<>();
		
		if(		deviceType
				.filter(dt->dt==DeviceType.CONVERTER_L_TO_KU_OUTDOOR)
				.isPresent()){

			for(int i=0; i<controllerNames.length; i++){
				controllers.add(
						getController(
								controllerNames[i],
								parameters[i],
								pacetId[i]));
			}
		}else{
			controllers.add(
					getController(
							"Measurement",
							PacketImp.IRT_SLCP_PARAMETER_MEASUREMENT_PICOBUC_ALL,
							PacketIDs.MEASUREMENT_ALL));
		}
		return controllers;
	}


	@Override
	protected void packetRecived(List<Payload> payloads) {
		logger.debug(payloads);
		if(payloads!=null){
			byte flags = 1;
			int value = 0;

			for(Payload pl:payloads){
				ParameterHeader parameterHeader = pl.getParameterHeader();
				switch(parameterHeader.getSize()){
				case 2:
					value = pl.getShort(0);
					break;
				case 3:
					flags = (byte) (pl.getByte()&0x03);
					value =  pl.getShort((byte)1);
					break;
				case 4:
					value = pl.getInt(0);
				}

				packetRecived(parameterHeader.getCode(), flags, value);
			}
		}
	}

	protected void packetRecived(byte parameter, byte flags, int value) {
		logger.debug("parameter={}, flags={}, value={}", parameter, flags, value);

		ValueDouble v;
		switch(parameter){
		case PacketImp.PARAMETER_MEASUREMENT_OUTPUT_POWER://or Packet.IRT_SLCP_PARAMETER_MEASUREMENT_FCM_STATUS
			if(		deviceType
					.filter(dt->dt!=DeviceType.CONVERTER_L_TO_KU_OUTDOOR)
					.isPresent()){

				int hashCode = 31*flags + value;
				if(flags==0)
					lblOutputPower.setText("N/A");
				else if (hashCode != output) {
					output = hashCode;
					v = new ValueDouble(value, 1);
					v.setPrefix(Translation.getValue(String.class, "dbm", "dBm"));
					lblOutputPower.setText(getOperator(flags)+v.toString());
				}
				logger.trace("PARAMETER_MEASUREMENT_PICOBUC_OUTPUT_POWER, flags={}, value={}", flags, value);
			}else
				setConverterStatus(value);

			break;
		case PacketImp.PARAMETER_MEASUREMENT_TEMPERATURE:
			if(value!=temperature){
				temperature = value;
				v = new ValueDouble(value, 1);
				v.setPrefix(" C");
				lblTemperature.setText(v.toString());
			}
			logger.trace("PARAMETER_MEASUREMENT_PICOBUC_OUTPUT_POWER, flags={}, value={}", flags, value);
			break;

		case PacketImp.PARAMETER_MEASUREMENT_STATUS:
				setStatus(value);
		}
	}

	protected void setStatus(int status) {

		if(this.status!=status){
			this.status = status;
			ledMute.setOn((status & MUTE) > 0);
			ledMute.setToolTipText("Status flags= 0x" + Integer.toHexString(status));
		}
	}

	protected void setConverterStatus(int status) {

		if(this.status!=status){
			this.status = status;
			ledMute.setOn((status & MonitorPanelConverter.MUTE) > 0);
			ledMute.setToolTipText("Status flags= 0x" + Integer.toHexString(status));
		}
	}

	//***********************************************************************************
	protected class TextWorker extends SwingWorker<String, Void>{
		JLabel label;
		String key;
		String defaultValue;

		public TextWorker(JLabel label, String key, String defaultValue) {
			this.label = label;
			this.key = key;
			this.defaultValue = defaultValue;
		}

		@Override
		protected String doInBackground() throws Exception {
			return Translation.getValue(String.class, key, defaultValue);
		}

		@Override
		protected void done() {
			try {
				label.setText(get());
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}
}
