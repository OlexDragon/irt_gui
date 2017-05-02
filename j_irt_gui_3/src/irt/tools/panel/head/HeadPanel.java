package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkedPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.value.StaticComponents;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBitsBUC;
import irt.tools.fx.MonitorPanelFx.StatusBitsFCM;
import irt.tools.label.LED;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel {

	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final Color BACKGROUND_COLOR = new Color(0x3B, 0x4A, 0x8B);
	public static LED ledRx = StaticComponents.getLedRx();
	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;

	private static Properties properties;

	public HeadPanel(JFrame target) {
		super(target, (int)Translation.getValue(Integer.class, "headPanel.max_width", 650));

		Timer timer = new Timer(10000, e->{
			ledPowerOn.setOn(false);
			ledMute.setOn(false);
			ledAlarm.setOn(false);
		});
		timer.start();

		setSize(Translation.getValue(Integer.class, "headPanel.width", 650), Translation.getValue(Integer.class, "headPanel.height", 74));
		setBackground(BACKGROUND_COLOR);
		setCorner(35);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		ledPowerOn = new LED(Color.GREEN, Translation.getValue(String.class, "power_on", "POWER ON"));
		ledPowerOn.setName("Power On");
		ledPowerOn.setForeground(new Color(176, 224, 230));
		add(ledPowerOn);
		new LedController(packet->Optional
												.ofNullable(packet)
												.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
												.filter(p->p.getHeader().getPacketId()==PacketWork.PACKET_ID_DEVICE_INFO)
												.map(p->p.getPayloads())
												.filter(pls->pls.size()!=0)
												.ifPresent(pl->{
													if(!ledPowerOn.isOn())
														ledPowerOn.setOn(true);
												}));

		ledAlarm = new LED(Color.RED, Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setName("Main Alarm");
		ledAlarm.setForeground(new Color(176, 224, 230));
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setName("Main Mute");
		ledMute.setForeground(new Color(176, 224, 230));
		add(ledMute);
		new LedController(packet->Optional
												.ofNullable(packet)
												.filter(p->LogManager.getLogger().exit(p.getHeader().getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_ALL))
												.map(p->p.getPayloads())
												.flatMap(pls->pls.parallelStream().filter(pl->pl.getParameterHeader().getCode()==(isConverter(packet) ? ParameterHeaderCodeFCM.STATUS.getCode() : ParameterHeaderCodeBUC.STATUS.getCode())).findAny())
												.ifPresent(pl->{
													final int statusBits = pl.getInt(0);
													boolean isConverter = isConverter(packet);
													final boolean isMute = isConverter ? isMuteFCM(statusBits) : isMuteBUC(statusBits);
													if(isMute!=ledMute.isOn())
														ledMute.setOn(isMute);
													timer.restart();
												}));

		ledRx.setBounds(10, 29, 17, 17);Arrays.stream(new Byte[5]).parallel();
		add(ledRx);

		swingWorkers();
}

	private boolean isConverter(Packet packet) {

		if(packet instanceof LinkedPacket){

			return Optional
					.ofNullable(((LinkedPacket)packet).getLinkHeader())
					.map(lh->lh.getAddr()==0)
					.orElse(true);
		}

		return true;
	}

	private boolean isMuteFCM(final int statusBits) {
		return StatusBitsFCM.MUTE.isOn(statusBits) || StatusBitsFCM.MUTE_TTL.isOn(statusBits);
	}

	private boolean isMuteBUC(final int statusBits) {
		return StatusBitsBUC.MUTE.isOn(statusBits);
	}

	private static Properties getProperties() {
		if(properties==null){

			properties = new Properties();
			try(InputStream resourceAsStream = HeadPanel.class.getResourceAsStream("HeadPanel.properties");) {
				 
				properties.load(resourceAsStream);
			} catch (Exception e) {
				logger.catching(e);
			}
		}
		return properties;
	}

	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}
//
//	public void setPowerOn(boolean isOn) {
//		if(ledPowerOn.isOn()!=isOn){
//			ledPowerOn.setOn(isOn);
//			if(!isOn){
//				ledMute.setOn(false);
//				ledAlarm.setOn(false);
//			}
//		}
//	}
//
//	public ValueChangeListener getStatusChangeListener() {
//		return new ValueChangeListener() {
//
//			@Override
//			public void valueChanged(ValueChangeEvent valueChangeEvent) {
//
//				int id = valueChangeEvent.getID();
//				logger.trace(valueChangeEvent);
//				Object source = valueChangeEvent.getSource();
//				int status;
//
//				if(source instanceof Long){
//					status=((Long)source).intValue();
//					ledMute.setOn((status&MonitorController.MUTE)>0);
//				}else{
//					status = (int) source;
//					if(id==PacketWork.PACKET_ID_ALARMS_SUMMARY){
//						int alarmStatus = status&7;
//						if(alarmStatus>3){
//							ledAlarm.setLedColor(Color.RED);
//							ledAlarm.setOn(true);
//						}else if(alarmStatus>1){
//							ledAlarm.setLedColor(Color.YELLOW);
//							ledAlarm.setOn(true);
//						}else
//							ledAlarm.setOn(false);
//					}else{
//						ledMute.setOn((status&MonitorController.MUTE)>0);
//					}
//				}
//			}
//		};
//	}

	public void refresh() {

		new SwingWorker<String, Void>() {

			@Override
			protected String doInBackground() throws Exception {
				return Translation.getValue(String.class, "power_on", "POWER ON");
			}

			@Override
			protected void done() {
				try {
					ledPowerOn.setText(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<String, Void>() {

			@Override
			protected String doInBackground() throws Exception {
				return Translation.getValue(String.class, "alarm", "ALARM");
			}

			@Override
			protected void done() {
				try {
					ledAlarm.setText(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		new SwingWorker<String, Void>() {

			@Override
			protected String doInBackground() throws Exception {
				return Translation.getValue(String.class, "mute", "MUTE");
			}

			@Override
			protected void done() {
				try {
					ledMute.setText(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

		swingWorkers();
	}

	/**
	 * set Bounds and Font for ledPowerOn, ledAlarm, ledMute
	 */
	private void swingWorkers() {
		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				return Translation.getValue(Rectangle.class, "headPanel.led_powerOn_bounds", new Rectangle());
			}

			@Override
			protected void done() {
				try {
					ledPowerOn.setBounds(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();
		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				return Translation.getValue(Rectangle.class, "headPanel.led_alarm_bounds", new Rectangle());
			}

			@Override
			protected void done() {
				try {
					ledAlarm.setBounds(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();
		new SwingWorker<Rectangle, Void>() {

			@Override
			protected Rectangle doInBackground() throws Exception {
				return Translation.getValue(Rectangle.class, "headPanel.led_mute_bounds", new Rectangle());
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
		new SwingWorker<Font, Void>() {

			@Override
			protected Font doInBackground() throws Exception {
				logger.entry();
				return logger.exit(Translation.getFont());
			}

			@Override
			protected void done() {
				try {
					Font font = get();
					ledPowerOn.setFont(font);
					ledAlarm.setFont(font);
					ledMute.setFont(font);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();
	}

	public void setAlarm(boolean isOn) {
		ledAlarm.setOn(isOn);
	}

	public void setAlarmColor(Color color) {
		ledAlarm.setLedColor(color);
	}
//
//	public void setMute(boolean isMute) {
//		ledMute.setOn(isMute);
//	}

	private class LedController implements PacketListener{

		private final Consumer<Packet> cunsomer;

		public LedController(Consumer<Packet> cunsomer) {
			this.cunsomer = cunsomer;
			GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		}

		@Override
		public void onPacketRecived(Packet packet) {
			cunsomer.accept(packet);
		}
		
	}
}