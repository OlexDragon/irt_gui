package irt.gui.controllers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.ComboBoxUnitAddress;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.interfaces.LinkedPacket;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import lombok.Getter;
import lombok.Setter;

public abstract class FieldsControllerAbstract extends Observable implements Observer, FieldController, Runnable  {

	protected final Logger logger = LogManager.getLogger(getClass().getName());

	private static TextInputDialog dialog;

	private static boolean error;

	protected abstract void 	updateFields(LinkedPacket packet) throws Exception;
	protected abstract Duration getPeriod();

	@Getter @Setter
	private 		String 			name;

	protected 		Observer 		observer 		= this;
	protected ScheduledFuture<?> 	scheduleAtFixedRate;

	/** update = true - start sending the packages to the device, false - stop*/
	public void doUpdate(boolean update) {
		if(update) {
			if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
				scheduleAtFixedRate = LinkedPacketsQueue.SERVICES.scheduleAtFixedRate(this, 1, getPeriod().toMillis(), TimeUnit.MILLISECONDS);

		}else if(scheduleAtFixedRate!=null)
			scheduleAtFixedRate.cancel(true);
	}

	@Override
	public void update(Observable observable, Object object) {
//		logger.error(getClass().getName());
		logger.traceEntry("{}; {}", observable, object);
		runThread(new Runnable() {

			@Override
			public void run() {
				if(observable instanceof LinkedPacket) 
					update((LinkedPacket) observable);

				if(object instanceof LinkedPacket)
					update((LinkedPacket) object);
			}

			public void update(LinkedPacket p) {
				if( p.getAnswer()!=null)
					try {

						Platform.runLater(
								()->{

//									logger.error(p);
									if(dialog==null)
										return;

									dialog.close();
									dialog = null;
								});

						updateFields(p);

						error = false;

					} catch (Exception e) {
						if(!error){	//not to repeat the same error message
							error = true;
							logger.catching(e);
						}
					}
				else if(!error){	//not to repeat the same error message
					error = true;
					logger.warn("No Answer: {}", p);

					Platform.runLater(
							()->{

								if(dialog!=null)
									return;

								dialog = new TextInputDialog(Integer.toString(p.getLinkHeader().getAddr()&0xFF));
								dialog.initModality(Modality.APPLICATION_MODAL);
								dialog.setTitle("The Unit did not Answer.");
								dialog.setHeaderText("Check M&C Connection or change Unit Address.");
								dialog.setContentText("Please enter Unit Address:");
								dialog.showAndWait()
								.ifPresent(address->{
									ComboBoxUnitAddress.setAddress(Optional.of(address).map(a->a.replaceAll("\\D", "")).map(Integer::parseInt).orElse(null));
								});
							});
				}
			}
		});
	}

	private void runThread(Runnable target) {

		final Thread thread = new Thread(target);
		int priority = thread.getPriority();

		if(priority>Thread.MIN_PRIORITY)
			thread.setPriority(--priority);

		thread.setDaemon(true);
		thread.start();
	}

	//*********************************************   Packet Sender   ****************************************************************

	private final List<LinkedPacket> 	packetsToSend = new ArrayList<>(); 

	@Getter @Setter
	private boolean send;

	public void addPacketToSend(LinkedPacket linkedPacket){

		if(!packetsToSend.contains(linkedPacket)){

			packetsToSend.add(linkedPacket);
			linkedPacket.addObserver(observer);
		}
	}

	public void removePacketToSend(LinkedPacket linkedPacket) {
		packetsToSend.remove(linkedPacket);
		linkedPacket.deleteObserver(observer);
	}

	@Override
	public void run(){

		packetsToSend
			.stream()
			.forEach(packet->SerialPortController.getQueue().add(packet, true));
	}
}
