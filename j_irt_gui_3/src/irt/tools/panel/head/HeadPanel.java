package irt.tools.panel.head;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketSuper;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeBUC;
import irt.tools.fx.MonitorPanelFx.ParameterHeaderCodeFCM;
import irt.tools.fx.MonitorPanelFx.StatusBitsBUC;
import irt.tools.fx.MonitorPanelFx.StatusBitsFCM;
import irt.tools.label.LED;

@SuppressWarnings("serial")
public class HeadPanel extends MainPanel implements PacketListener {

	private static final Logger logger = LogManager.getLogger();

	public static final Color BACKGROUND_COLOR = new Color(0x3B, 0x4A, 0x8B);
	public static LED ledRx = new LED(Color.GREEN, "");
	private LED ledPowerOn;
	private LED ledMute;
	private LED ledAlarm;

	private Timer timer;

	private static Properties properties;

	public HeadPanel(JFrame target) {
		super(target, (int)Translation.getValue(Integer.class, "headPanel.max_width", 650));
		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {
			}
			public void ancestorMoved(AncestorEvent event) {
			}
			public void ancestorRemoved(AncestorEvent event) {
				timer.stop();
			}
		});

		timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), e->{
			ledPowerOn.setOn(false);
			ledMute.setOn(false);
			ledAlarm.setOn(false);
		});
		timer.setRepeats(false);
		timer.start();

		setSize(Translation.getValue(Integer.class, "headPanel.width", 650), Translation.getValue(Integer.class, "headPanel.height", 74));
		setBackground(BACKGROUND_COLOR);
		setCorner(35);
		setArcStart(-40);
		setArcStep(155);
		setArcWidth(80);

		ledPowerOn = new LED(Color.GREEN, Translation.getValue(String.class, "power_on", "POWER ON"));
		ledPowerOn.setForeground(new Color(176, 224, 230));
		add(ledPowerOn);

		ledAlarm = new LED(Color.RED, Translation.getValue(String.class, "alarm", "ALARM"));
		ledAlarm.setForeground(new Color(176, 224, 230));
		add(ledAlarm);

		ledMute = new LED(Color.YELLOW, Translation.getValue(String.class, "mute", "MUTE"));
		ledMute.setForeground(new Color(176, 224, 230));
		add(ledMute);

		ledRx.setBounds(10, 29, 17, 17);Arrays.stream(new Byte[5]).parallel();
		add(ledRx);

		swingWorkers();
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
	}

	private boolean isConverter(Packet packet) {

		return Optional
				.of(packet)
				.filter(LinkedPacket.class::isInstance)
				.map(LinkedPacket.class::cast)
				.map(LinkedPacket::getLinkHeader)
				.filter(lh->lh!=null)
				.map(lh->lh.getAddr()==0)
				.orElse(true);
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
				logger.traceEntry();
				return logger.traceExit(Translation.getFont());
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

	@Override
	public void onPacketRecived(Packet packet) {

		new MyThreadFactory(()->{

			final Optional<Packet> oPacket = Optional
					.ofNullable(packet)
					.map(p->p.getHeader())
					.filter(h->h!=null)
					.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE).map(h->packet);

			//Power Status
			oPacket.ifPresent(
					pl->{
						if(!ledPowerOn.isOn())
							ledPowerOn.setOn(true);// Has answer so unit is on
						timer.restart();
					});

			if(!oPacket.isPresent()){
				ledRx.setLedColor(Color.RED);
				ledRx.blink();
				return;
			}

			ledRx.setLedColor(Color.GREEN);
			ledRx.blink();

			//Mute Status
			oPacket
			.filter(p->PacketIDs.MEASUREMENT_ALL.match(p.getHeader().getPacketId()))
			.map(p->p.getPayloads())
			.map(pls->pls.parallelStream())
			.orElse(Stream.empty())
			.filter(pl->pl.getParameterHeader().getCode()==getStatusCode(packet))
			.findAny()
			.ifPresent(
					pl->{
						final int statusBits = pl.getInt(0);
						boolean isConverter = isConverter(packet);
						final boolean isMute = isConverter ? isMuteFCM(statusBits) : isMuteBUC(statusBits);
						if(isMute!=ledMute.isOn())
							ledMute.setOn(isMute);
						timer.restart();
					});

			//Alarm Status
			oPacket
			.filter(p->PacketIDs.ALARMS_SUMMARY.match(p.getHeader().getPacketId()))
			.flatMap(Packets::cast)
			.map(PacketSuper::getValue)
			.filter(AlarmSeverities.class::isInstance)
			.map(AlarmSeverities.class::cast)
			.ifPresent(
					as->{

						if(as==AlarmSeverities.NO_ALARM || as==AlarmSeverities.INFO){
							if(ledAlarm.isOn())
								ledAlarm.setOn(false);
							return;
						}

						Color background = as.getBackground();
						if(!ledAlarm.isOn() || !ledAlarm.getLedColor().equals(background)){

							ledAlarm.setLedColor(background);
							ledAlarm.setOn(true);
						}
					});
		});
	}

	private byte getStatusCode(Packet packet) {
		return isConverter(packet) ? ParameterHeaderCodeFCM.STATUS.getCode() : ParameterHeaderCodeBUC.STATUS.getCode();
	}
}