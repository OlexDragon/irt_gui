package irt.fx.control.buc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.data.value.PacketDoubleValue;
import irt.data.value.interfaces.PacketValue;
import irt.packet.observable.PacketAbstract;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.fx.SerialPortSelectorFx;
import irt.services.GlobalServices;
import irt.services.MyThreadFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class LabelMeasurementFx extends Label implements Runnable, Observer{
	private final Logger logger = LogManager.getLogger(getClass());

	private int period = 10;
							public int getPeriod() { return period; }
							public void setPeriod(int period) { this.period = period; }

	private String key = SerialPortSelectorFx.SERIAL_PORT_SELECTOR_PREF;
																		public String getKey() { return key; }
																		public void setKey(String key) {
																			Objects.requireNonNull(key);
																			this.key = key;
																		}

	private final PacketAbstract getPacket;
	private final String tooltipBundleKey;

	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService services = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final List<Consumer<PacketValue>> valueChangeActions = new ArrayList<>();
																						public void addValueChangeAction(Consumer<PacketValue> statusChangeAction) {
																							valueChangeActions.add(statusChangeAction);
																						}
																						public void removeStatusChangeAction(Consumer<String> statusChangeAction) {
																							valueChangeActions.remove(statusChangeAction);
																						}

    @FXML  private Tooltip tooltip;

	private PacketDoubleValue packetValue;
												public PacketValue getPacketValue() {
													return packetValue;
												}

	public LabelMeasurementFx(PacketAbstract getPacket, String tooltipBundleKey) {
		logger.entry(tooltipBundleKey);

		this.getPacket = getPacket;
		this.tooltipBundleKey = tooltipBundleKey;

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/label_measurement.fxml"));
    	fxmlLoader.setResources(IrtGuiProperties.BUNDLE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	
        startService();
	}

	@FXML protected void initialize() {
		logger.traceEntry();
		tooltip.setText(IrtGuiProperties.BUNDLE.getString(tooltipBundleKey));
	}

	public void send(Observer observer) {
		logger.trace(observer);

		getPacket.addObserver(observer);

		Optional
		.ofNullable(GlobalServices.get(key))
		.filter(PacketsQueue.class::isInstance)
		.map(PacketsQueue.class::cast)
		.ifPresent(pq->pq.add(getPacket, true));
	}


	@Override
	public void run() {
		send(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		
		if(!(o instanceof PacketAbstract) || ((PacketAbstract)o).getAnswer()==null){
			logger.warn("The packet is not correct or does not have answer: {}", o);
			return;
		}

		try{

			final PacketAbstract packet = ((PacketAbstract)o).getAnswerPacket();
			Optional.ofNullable(packet).ifPresent(p->setValue(p));

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void setValue(PacketAbstract p) {
		logger.error(p);

		p.getPacketValue()
		.filter(pv->!pv.equals(packetValue))
		.map(PacketDoubleValue.class::cast)
		.ifPresent(pv->{

			 this.packetValue = pv;
			 Platform.runLater(()->setText(pv.toString()));

			 valueChangeActions
			 .parallelStream()
			 .forEach(a->a.accept(pv));
		 });
	}

	public void startService() {
		if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
			return;

		scheduleAtFixedRate = services.scheduleAtFixedRate(this, 1, period, TimeUnit.SECONDS);
	}

	public void stopService() {
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			return;

		scheduleAtFixedRate.cancel(true);
	}
}
