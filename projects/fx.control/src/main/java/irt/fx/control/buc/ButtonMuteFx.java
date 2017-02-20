
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
import irt.packet.PacketParsingException;
import irt.packet.Payload;
import irt.packet.enums.PacketErrors;
import irt.packet.observable.configuration.MutePacket;
import irt.packet.observable.configuration.MutePacket.MuteStatus;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.fx.SerialPortSelectorFx;
import irt.services.GlobalServices;
import irt.services.MyThreadFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class ButtonMuteFx extends Button implements Runnable, Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String CSS_CLASS_MUTED = "muted";
	public static final String CSS_CLASS_UNMUTED = "unmuted";
	public static final String CSS_CLASS_ERROR = "error";

	private final MutePacket getMutePacket = new MutePacket();
	private final MutePacket setMutePacket = new MutePacket(MuteStatus.MUTED);

	private int period = 10;
								public int getPeriod() { return period; }
								public void setPeriod(int period) { this.period = period; }


	private ScheduledFuture<?> scheduleAtFixedRate;
	private final ScheduledExecutorService services = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());
	private final List<Consumer<MuteStatus>> statusChangeActions = new ArrayList<>();
																						public void addStatusChangeAction(Consumer<MuteStatus> statusChangeAction) {
																							statusChangeActions.add(statusChangeAction);
																						}
																						public void removeStatusChangeAction(Consumer<MuteStatus> statusChangeAction) {
																							statusChangeActions.remove(statusChangeAction);
																						}


	private MuteStatus muteStatus = MuteStatus.UNKNOWN;
														public MuteStatus getMuteStatus() { return muteStatus; }
														public void setMuteStatus(MuteStatus muteStatus) {
															if(this.muteStatus == muteStatus)
																return;

															addClass(muteStatus);
															this.muteStatus = muteStatus;
															statusChangeActions.parallelStream().forEach(sca->sca.accept(muteStatus));
														}

	private String key = SerialPortSelectorFx.SERIAL_PORT_SELECTOR_PREF;
																		public String getKey() { return key; }
																		public void setKey(String key) {
																			Objects.requireNonNull(key);
																			this.key = key;
																		}

    @FXML  private Tooltip tooltip;

    public ButtonMuteFx() throws PacketParsingException{

    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/components/button_mute.fxml"));
    	fxmlLoader.setResources(IrtGuiProperties.BUNDLE);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	
        parentProperty().addListener((e, e1, e2)->logger.error("{} : {} : {}", e, e1, e2));
        startService();
	}


    @FXML private void onMute() {
    	logger.entry(muteStatus);

    	setMutePacket.setCommand(muteStatus==MuteStatus.MUTED ? MuteStatus.UNMUTED : MuteStatus.MUTED);

		Optional
		.ofNullable(GlobalServices.get(key))
		.filter(PacketsQueue.class::isInstance)
		.map(PacketsQueue.class::cast)
		.ifPresent(pq->{

			if(pq.add(setMutePacket, true)){
				stopService();
				startService();
			}
		});
    }

	private void addClass(MuteStatus muteStatus) {

		final ObservableList<String> styleClass = getStyleClass();

		styleClass.remove(CSS_CLASS_MUTED);
		styleClass.remove(CSS_CLASS_UNMUTED);
		styleClass.remove(CSS_CLASS_ERROR);

		switch(muteStatus){
		case MUTED:
			styleClass.add(CSS_CLASS_MUTED);
			break;
		case UNMUTED:
			styleClass.add(CSS_CLASS_UNMUTED);
			break;
		default:
			styleClass.add(CSS_CLASS_ERROR);
		}
	}

	public void send(Observer observer) {
		getMutePacket.addObserver(observer);
		send();
	}

	public void send() {
		logger.traceEntry(key);

		getMutePacket.addObserver(this);

		Optional
		.ofNullable(GlobalServices.get(key))
		.filter(PacketsQueue.class::isInstance)
		.map(PacketsQueue.class::cast)
		.ifPresent(pq->pq.add(getMutePacket, true));
	}


	@Override
	public void run() {
		send();
	}


	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o);

		if(!(o instanceof MutePacket))
			return;

		try {

				final MutePacket mp = (MutePacket)o;
				final byte[] answer = mp.getAnswer();

				if(answer==null){
					logger.warn("Packer without answer {}", mp);
					return;
				}

				final MutePacket mutePacket = new MutePacket(answer, true);

				final PacketErrors packetError = mutePacket.getPacketHeader().getPacketError();
				if(packetError!= PacketErrors.NO_ERROR){

					setMuteStatus(MuteStatus.UNKNOWN);
					logger.warn(packetError);
					final String text = IrtGuiProperties.BUNDLE.getString("error");

					Platform.runLater(()->{
						setText(text);
						tooltip.setText(packetError.toString());
					});
					return;
				}

				final Payload payload = mutePacket.getPayloads().get(0);
				final byte index = payload.getByte();
				final MuteStatus ms = MuteStatus.values()[index];

				setMuteStatus(ms);
				final String text = IrtGuiProperties.BUNDLE.getString("status.mute." + ms.name());

				Platform.runLater(()->{
					setText(text);
					tooltip.setText(text);
				});


			} catch (PacketParsingException e) {
				logger.catching(e);
			}
	}

	public void startService() {
		logger.traceEntry();

		if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
			return;

		scheduleAtFixedRate = services.scheduleAtFixedRate(this, 1, period, TimeUnit.SECONDS);
	}

	public void stopService() {
		logger.traceEntry();

		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			return;

		scheduleAtFixedRate.cancel(true);
	}
}
