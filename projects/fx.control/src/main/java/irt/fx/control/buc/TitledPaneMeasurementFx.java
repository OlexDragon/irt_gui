package irt.fx.control.buc;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
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
import irt.data.value.PacketDoubleValue;
import irt.data.value.enumes.MuteStatus;
import irt.data.value.enumes.ReferenceSource;
import irt.data.value.enumes.StatusBitsFcm;
import irt.data.value.interfaces.StatusBits;
import irt.packet.ParameterHeader;
import irt.packet.Payload;
import irt.packet.enums.ParameterHeaderCode;
import irt.packet.observable.PacketAbstract;
import irt.packet.observable.measurement.AllMeasurementsPacket;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.fx.SerialPortSelectorFx;
import irt.services.GlobalServices;
import irt.services.MyThreadFactory;
import irt.services.ToHex;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;

public class TitledPaneMeasurementFx extends TitledPane implements Runnable, Observer {
	private final Logger logger = LogManager.getLogger();

	private String key = SerialPortSelectorFx.SERIAL_PORT_SELECTOR_PREF;
																		public String getKey() { return key; }
																		public void setKey(String key) {
																			Objects.requireNonNull(key);
																			this.key = key;
																			buttonMute.setKey(key);
																		}

	private final ScheduledExecutorService services = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private ScheduledFuture<?> scheduleAtFixedRate;

	private final AllMeasurementsPacket measurementsPacket = new AllMeasurementsPacket();

	public TitledPaneMeasurementFx() {
		final URL resource = getClass().getResource("/fxml/components/buc_measurement.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource, IrtGuiProperties.BUNDLE);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML private Label lblMute;

    @FXML private Label lblInputPower;
    @FXML private Label lblOutputPower;
	@FXML private Label lblTmperatureUnit;
	@FXML private Label lblTemperatureCpu;
	@FXML private Label lbl5_5V;
	@FXML private Label lbl13_2V;
	@FXML private Label lblNeg13_2V;
    @FXML private Label lblLnbCurrent;
    @FXML private Label lblStatus;
    @FXML private Label lblAttenuation;
    @FXML private Label lblReferenceSource;

    @FXML private ButtonMuteFx buttonMute;

    @FXML private Tooltip ttStatus;


    @FXML private void initialize(){
		changeRequestTime(expandedProperty().get());
		expandedProperty().addListener(e->changeRequestTime(((BooleanProperty)e).get()));
	}

	private void changeRequestTime(boolean isExpanded) {

		Optional.ofNullable(scheduleAtFixedRate).filter(sch->!sch.isCancelled()).ifPresent(sch->sch.cancel(true));

		scheduleAtFixedRate = services.scheduleAtFixedRate(this, 0, isExpanded ? 3 : 10, TimeUnit.SECONDS);
	}

	public void send(Observer observer) {

		measurementsPacket.addObserver(observer);

		send();
	}

	public void send() {

		measurementsPacket.addObserver(this);

		Optional
		.ofNullable(GlobalServices.get(key))
		.filter(PacketsQueue.class::isInstance)
		.map(PacketsQueue.class::cast)
		.ifPresent(pq->pq.add(measurementsPacket, true));
	}


	@Override
	public void run() {
		send();
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o);

		if(!(o instanceof AllMeasurementsPacket))
			return;

		AllMeasurementsPacket ip = (AllMeasurementsPacket) o;

		if(ip.getAnswer()==null){
			logger.warn("THe Packet does not have answer. {}", ip);
			return;
		}

		try {

			final PacketAbstract p = ip.getAnswerPacket();
			logger.trace(p);

			p.getPayloads()
			.parallelStream()
			.forEach(pl->setText(pl));

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void setText(Payload payload){

		final ParameterHeader ph = payload.getParameterHeader();
		final ParameterHeaderCode phc = ph.getParameterHeaderCode();

		switch(phc){
		case M_13_2V:
			setText(lbl13_2V, 			new PacketDoubleValue(payload.getBuffer(), 1000).toString());
			break;
		case M_13_2V_NEG:
			setText(lblNeg13_2V, 		new PacketDoubleValue(payload.getBuffer(), 1000).toString());
			break;
		case M_5_5V:
			setText(lbl5_5V, 			new PacketDoubleValue(payload.getBuffer(), 1000).toString());
			break;
//TODO		case M_INPUT_POWER_BUC:
//			break;
		case M_INPUT_POWER_FCM:
			setText(lblInputPower, 		new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
		case M_OUTPUT_POWER_FCM:
			setText(lblOutputPower, 	new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
		case M_LNB_CURRENT:
			setText(lblLnbCurrent, 		new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
//		case M_MEASUREMENT_ALL_BUC:
//			break;
//		case M_OUTPUT_POWER_BUC:
//			break;
//		case M_STATUS_BUC:
//			break;

		case M_STATUS_FCM:

			setText(lblStatus, 			ToHex.bytesToHex(payload.getBuffer()));
			final StatusBits[] values = StatusBitsFcm.values(payload.getInt(0));

			setLockStatus(values);
			setMuteStatus(values);
			setStatusTooltip(values);
			break;

		case M_TEMPERATURE_UNIT:
			setText(lblTmperatureUnit, 	new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
		case M_TEMPERATURE_CPU:
			setText(lblTemperatureCpu, 	new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
		case M_ATTENUATION:
			setText(lblAttenuation, 	new PacketDoubleValue(payload.getBuffer(), 10).toString());
			break;
		case M_REFERENCE_SOURCE:
			setText(lblReferenceSource, IrtGuiProperties.BUNDLE.getString(ReferenceSource.values()[payload.getByte()&3].name()));
			break;
		default:
			logger.warn("Do not used: {}", phc);
		}
	}

	private void setLockStatus(StatusBits[] values) {
		String lockSrtatus = Arrays.stream(values)
								.filter(v -> v == StatusBitsFcm.LOCK)
								.map(v -> "status.lock." + v.name())
								.map(key -> IrtGuiProperties.BUNDLE.getString(key))
								.findAny()
								.orElse(IrtGuiProperties.BUNDLE.getString("status.lock.unlocked"));
		Platform.runLater(() -> setText(lockSrtatus));
	}
	private void setMuteStatus(final StatusBits[] values) {
		String muteSrtatus = Arrays
								.stream(values)
								.filter(v->v==StatusBitsFcm.MUTE || v==StatusBitsFcm.MUTE_TTL)
								.map(v->"status.mute." + v.name())
								.map(key->IrtGuiProperties.BUNDLE.getString(key))
								.findAny()
								.orElse(IrtGuiProperties.BUNDLE.getString("status.mute." + MuteStatus.UNMUTED.name()));
		Platform.runLater(()->lblMute.setText(muteSrtatus));
	}
	private void setStatusTooltip(final StatusBits[] values) {
		final String text = Arrays.toString(values);
		Platform.runLater(()->{
			if(ttStatus.getText().equals(text))
				return;
			ttStatus.setText(text);
		});
	}

	private void setText(final Labeled labeled, final String text){

		final String str = labeled.getText();

		Platform.runLater(()->{
			if(str.equals(text))
				return;
			labeled.setText(text);
		});
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
