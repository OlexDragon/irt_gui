package irt.fx.control.buc;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.packet.data.DeviceInfo;
import irt.packet.observable.InfoPacket;
import irt.packet.observable.PacketAbstract;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.fx.SerialPortSelectorFx;
import irt.services.GlobalServices;
import irt.services.MyThreadFactory;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;

public class TitledPaneBucInfoFx extends TitledPane implements Runnable, Observer {
	private final Logger logger = LogManager.getLogger();

	private String key = SerialPortSelectorFx.SERIAL_PORT_SELECTOR_PREF;
																		public String getKey() { return key; }
																		public void setKey(String key) {
																			Objects.requireNonNull(key);
																			this.key = key;
																		}

	private final ScheduledExecutorService services = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private ScheduledFuture<?> scheduleAtFixedRate;

	private final InfoPacket infoPacket = new InfoPacket();

	public TitledPaneBucInfoFx() {
		final URL resource = getClass().getResource("/fxml/components/buc_info.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource, IrtGuiProperties.BUNDLE);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

    @FXML private Label lblPN;
    @FXML private Label lblCounter;
    @FXML private Label lblBuild;
    @FXML private Label lblVersion;
    @FXML private Label lblDeviceType;
    @FXML private Label lblDeviceRevision;
    @FXML private Label lblDeviceSubtype;

    @FXML private void initialize(){
		changeRequestTime(expandedProperty().get());
		expandedProperty().addListener(e->changeRequestTime(((BooleanProperty)e).get()));
	}

	private void changeRequestTime(boolean isExpanded) {

		Optional.ofNullable(scheduleAtFixedRate).filter(sch->!sch.isCancelled()).ifPresent(sch->sch.cancel(true));

		scheduleAtFixedRate = services.scheduleAtFixedRate(this, 0, isExpanded ? 1 : 10, TimeUnit.SECONDS);
	}

	public void send(Observer observer) {

		infoPacket.addObserver(observer);

		send();
	}

	public void send() {

		infoPacket.addObserver(this);

		Optional
		.ofNullable(GlobalServices.get(key))
		.filter(PacketsQueue.class::isInstance)
		.map(PacketsQueue.class::cast)
		.ifPresent(pq->pq.add(infoPacket, true));
	}


	@Override
	public void run() {
		send();
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o);

		if(!(o instanceof InfoPacket))
			return;

		InfoPacket ip = (InfoPacket) o;
		try {

			final PacketAbstract p = ip.getAnswerPacket();
			logger.trace(p);

			p.getPacketValue().filter(v->v instanceof DeviceInfo).map(DeviceInfo.class::cast).ifPresent(di->Platform.runLater(()->{

				final String str = String.format("%s-%s", di.getSerialNumber(), di.getUnitName());
				setText(this				, str);
				setText(lblPN				, di.getUnitPartNumber().toString());
				setText(lblCounter			, calculateTime(di.getUptimeCounter()));
				setText(lblDeviceType		, di.getType().map(dt->dt.toString()).orElse("N/A"));
				setText(lblDeviceRevision	, Integer.toString(di.getRevision()));
				setText(lblDeviceSubtype	, Integer.toString(di.getSubtype()));
				setText(lblVersion			, di.getFirmwareVersion().toString());
				setText(lblBuild			, di.getFirmwareBuildDate().toString());
			}));
			logger.trace(p);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void setText(final Labeled labeled, final String text){

		final String str = labeled.getText();

		if(!str.equals(text))
			Platform.runLater(()->labeled.setText(text));
	}

	private static NumberFormat numberFormat = new DecimalFormat("00");
	public static String calculateTime(long seconds) {

		int day = (int) TimeUnit.SECONDS.toDays(seconds);
	    long hours = TimeUnit.SECONDS.toHours(seconds) 	  - TimeUnit.DAYS.toHours(day);
	    long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds));
	    long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds));

	    StringBuilder sb = new StringBuilder();

	    if(day>0){
	    	sb.append(day).append(" day");
	    	if(day==1)
	    		sb.append(", ");
	    	else
	    		sb.append("s, ");
	    }

	    return sb.append(numberFormat.format(hours)).append(":").append(numberFormat.format(minute)).append(":").append(numberFormat.format(second)).toString();
	}
}
