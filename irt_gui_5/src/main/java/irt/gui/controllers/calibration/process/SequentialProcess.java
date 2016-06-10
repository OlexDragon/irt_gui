package irt.gui.controllers.calibration.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.calibration.PanelSignalGenerator;
import irt.gui.controllers.calibration.enums.Calibration;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.controllers.calibration.tools.enums.ToolsState;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;
import irt.gui.data.packet.observable.calibration.ToolsPacket;
import irt.gui.data.packet.observable.configuration.MutePacket;
import irt.gui.data.packet.observable.configuration.MutePacket.MuteStatus;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SequentialProcess extends Observable implements Runnable {
	private final Logger logger = LogManager.getLogger();

	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, new MyThreadFactory());
	private final Object thisObject = this;

	private Tool signalGenerator;
	private Tool powerMeter;
	private Tool buc;

	private Double 		sgPower;
	private Double 		sgFrequency;
	private Double 		powerMeterValue;
	private ToolsState 	sgOutputState;
	private Integer 	bucInputPower;
	private MuteStatus 	bucMuteStatus;

	private Sequence sequence = Sequence.CHECK_TOOLS;

	private List<Calibration> calibrations;
	private final BigDecimal step;
	private final int steps;

	private boolean notified;

	private Future<Integer> futureBucInput;
	private Future<Integer> futureBucOutput;
	private Future<Double> futurePowerMeter;

	public SequentialProcess(List<Calibration> calibrations, double step, int steps) {
		this.calibrations = calibrations;
		this.step = new BigDecimal(step).setScale(1, RoundingMode.HALF_EVEN);
		this.steps = steps;
	}

	@Override public void run() {
		isCalibating = false;

		try{
		while(sequence!=Sequence.END){

			try {

				if (notified)
					notified = false;
				else
					synchronized (thisObject) { wait(); }

				switch (sequence) {

				case CHECK_TOOLS:
					checkTools();
					break;

				case PREPARE_TOOLS:
					prepareTools();
					break;

				case CALIBRATION:
					doCalibration();
					break;

				case FINALIZE:
					endProcess();
					break;
				default:
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}
		}catch(Exception e){
			logger.catching(e);
		}
	}

	private BigDecimal valueToSend;
	private boolean isCalibating;
	private void doCalibration() {

		if(isCalibating)
			return;

		isCalibating = true;
		valueToSend = new BigDecimal(sgPower).setScale(1, RoundingMode.HALF_EVEN);
		logger.error("*****   {}   *****", valueToSend);

		IntStream
		.range(0, steps)
		.forEach(v->{

			if(sequence!=Sequence.CALIBRATION)
				return;

			synchronized (this) { try {wait(100);} catch (Exception e) {} }
			signalGenerator.set(Commands.POWER, valueToSend.doubleValue(), null);

			synchronized (this) { try { wait(100); } catch (Exception e1) { logger.catching(e1); } }

			//Commands
			calibrations
			.stream()
			.forEach(c->{
				switch(c){
				case GAIN:
					break;
				case INPUT_POWER:
					futureBucInput = EXECUTOR.submit( new AverageInteger(buc, Commands.INPUT));
					break;
				case OUTPUT_POWER:
					futureBucOutput = EXECUTOR.submit( new AverageInteger(buc, Commands.OUTPUT));
					futurePowerMeter = EXECUTOR.submit( new AverageDouble(powerMeter, Commands.GET));
				}
			});

			//Get results
			Optional
			.ofNullable(futureBucInput)
			.ifPresent(f->{
				try {

					final SimpleEntry<Double, Integer> entry = new ValueInputPower(valueToSend.doubleValue(), f.get());
					notifyObservers(entry);
				} catch (Exception e) {
					stop();
					logger.catching(e);
				}
			});
			Optional
			.ofNullable(futureBucOutput)
			.ifPresent(output->{

				Optional
				.ofNullable(futurePowerMeter)
				.ifPresent(powerMeter->{

					try {

						final SimpleEntry<Double, Integer> entry = new ValueOutputPower(powerMeter.get(), output.get());
						notifyObservers(entry);

					} catch (Exception e) {
						stop();
						logger.catching(e);
					}
				});
			});

			valueToSend = valueToSend.add(step);
		});

		stop();
//		synchronized (this) { try { wait(500); } catch (Exception e1) { logger.catching(e1); } }
	}

	private void prepareTools() {
		logger.entry(sgOutputState);
		if(sgOutputState==ToolsState.OFF)
			signalGenerator.set(Commands.OUTPUT, ToolsState.ON, null);

		if(bucMuteStatus==MuteStatus.MUTED && (calibrations.contains(Calibration.OUTPUT_POWER) || calibrations.contains(Calibration.GAIN)))
			buc.set(Commands.OUTPUT, MuteStatus.UNMUTED, bucMuteObserver);

		
		sequence = Sequence.CALIBRATION;
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	private void checkTools() {

		//check whether the received SG power value
		if(Optional
				.ofNullable(signalGenerator)
				.map(sg->sgPower==null)
				.orElse(false))
			return;

		//check whether the received SG frequency value
		if(Optional
				.ofNullable(signalGenerator)
				.map(sg->sgFrequency==null)
				.orElse(false))
			return;

		//check whether the received SG output state
		if(Optional
				.ofNullable(signalGenerator)
				.map(sg->sgOutputState==null)
				.orElse(false))
			return;

		//check whether the received BUC input power detector value
		if(Optional
				.ofNullable(buc)
				.map(sg->bucInputPower==null)
				.orElse(false))
			return;

		//check whether the Power Meter value
		if(Optional
				.ofNullable(powerMeter)
				.map(sg->powerMeterValue==null)
				.orElse(false))
			return;

		sequence = Sequence.PREPARE_TOOLS;
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	public void setSignalGenerator(Tool signalGenerator) {
		this.signalGenerator = signalGenerator;
		EXECUTOR.execute(()->{
			try{

				synchronized (this) { wait(10); }
				signalGenerator.get(Commands.POWER, sgInputPowerObserver);

				synchronized (this) { wait(100); }
				signalGenerator.get(Commands.FREQUENCY, sgFrequensyObserver);

				synchronized (this) { wait(100); }
				signalGenerator.get(Commands.OUTPUT, sgOutputObserver);

			}catch(Exception e){
				 logger.catching(e);
			}
		});

		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	public void setPowerMeter(Tool powerMeter) {
		this.powerMeter = powerMeter;
		powerMeter.get(Commands.GET, powerMeterObserver);
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	public void setBuc(Tool buc) {
		this.buc = buc;

		buc.get(Commands.INPUT, bucInputDetectorObserver);	//Input Power Detector
		buc.get(Commands.OUTPUT, bucMuteObserver);			//Mute
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	private final Observer sgInputPowerObserver = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			logger.entry(o);
			o.deleteObserver(sgInputPowerObserver);

			final byte[] answer = ((ToolsPacket)o).getAnswer();
			if(answer!=null){
				PanelSignalGenerator
				.getDouble(answer)
				.ifPresent(p->sgPower = p);
			}else
				showAlert("It is impossible to obtain the power from the Signal Generator");

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}
	};
	private final Observer sgFrequensyObserver = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			o.deleteObserver(sgFrequensyObserver);

			final byte[] answer = ((ToolsPacket)o).getAnswer();
			if(answer!=null){
				PanelSignalGenerator
				.getDouble(answer)
				.ifPresent(p->sgFrequency = p);
			}else
				showAlert("It is impossible to obtain the frequency from the Signal Generator");

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}
	};
	private final Observer bucInputDetectorObserver = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			o.deleteObserver(bucInputDetectorObserver);
			
			final PacketToSend p = (PacketToSend)o;
			final byte[] answer = p.getAnswer();
			if(answer!=null){
				final LinkedPacket newPacket = (LinkedPacket) Packet.createNewPacket(p.getClass(), answer, true);
				if(newPacket.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){
					bucInputPower = newPacket.getPayloads().get(0).getInt(2);
				}
			}else
				showAlert("It is impossible to obtain the input detector value from the BUC");

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}
	};
	private final Observer bucMuteObserver = new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			o.deleteObserver(bucInputDetectorObserver);
			
			final PacketToSend p = (PacketToSend)o;
			final byte[] answer = p.getAnswer();
			if(answer!=null){
				final LinkedPacket newPacket = (LinkedPacket) Packet.createNewPacket(p.getClass(), answer, true);
				if(newPacket.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){
					final byte index = newPacket.getPayloads().get(0).getByte();
					bucMuteStatus = MutePacket.MuteStatus.values()[index];
				}
			}else
				showAlert("It is impossible to obtain the mute status from the BUC");

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}
	};
	private final Observer sgOutputObserver= new Observer() {

		@Override
		public void update(Observable o, Object arg) {
			o.deleteObserver(sgOutputObserver);

			final PacketToSend tp = (PacketToSend) o;

			sgOutputState = Optional
					.ofNullable(tp.getAnswer())
					.map(ToolsState::valueOf)
					.orElseGet(()->{
						showAlert("It is impossible to obtain the output state from the Signal Generator");
						return null;
					});

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}	
	};
	private final Observer powerMeterObserver = new Observer() {
		
		@Override
		public void update(Observable o, Object arg) {
			o.deleteObserver(powerMeterObserver);

			final PacketToSend tp = (PacketToSend) o;

			powerMeterValue = Optional
					.ofNullable(tp.getAnswer())
					.map(String::new)
					.map(String::trim)
					.map(Double::parseDouble)
					.orElseGet(()->{
						showAlert("It is impossible to obtain the value from the Power Meter");
						return null;
					});

			synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
		}
	};


	private void showAlert(final String headerText) {
		Platform
		.runLater(()->{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Tools error");
			alert.setHeaderText(headerText);
			alert.show();
		});

		stop();
	}

	@Override public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	public void stop() {

		if(sequence == Sequence.END)
			return;

		Optional
		.ofNullable(futureBucInput)
		.ifPresent(f->f.cancel(true));

		Optional
		.ofNullable(futureBucOutput)
		.ifPresent(f->f.cancel(true));

		sequence = Sequence.FINALIZE;
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	private void endProcess() {

		signalGenerator.set(Commands.OUTPUT, ToolsState.OFF, null);
		buc.set(Commands.OUTPUT, MuteStatus.MUTED, null);
		signalGenerator.set(Commands.POWER, sgPower, null);

		sequence = Sequence.END;
		synchronized (thisObject) { notified = true; thisObject.notifyAll(); }
	}

	private enum Sequence{
		CHECK_TOOLS,
		PREPARE_TOOLS,
		CALIBRATION,
		FINALIZE,
		END
	}
}
