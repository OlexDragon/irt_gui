package irt.gui.controllers.components;

import java.time.Duration;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.NetworkAddress;
import irt.gui.data.NetworkAddress.NetworkAddressType;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.configuration.NetworkAddressPacket;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;

public class PanelNetworkController extends FieldsControllerAbstract {

	private final Logger logger = LogManager.getLogger();
	
	private boolean valueChanged;
	private NetworkAddressType curentType = NetworkAddressType.UNKNOWN;

	@FXML private ComboBox<NetworkAddress.NetworkAddressType> addressType;
	@FXML private NetworkFieldController addressController;
	@FXML private NetworkFieldController maskController;
	@FXML private NetworkFieldController gatewayController;

	@FXML private Button saveButton;
	@FXML private Button canselButton;
	@FXML private Button resetButton;

	@FXML public void initialize(){
		try {

			addLinkedPacket(new NetworkAddressPacket((NetworkAddress)null));

			final NetworkAddressType[] values = NetworkAddressType.values();
			addressType.setPromptText(values[0].toString());
			addressType.getItems().add(values[1]);
			addressType.getItems().add(values[2]);

			addressController.addListener(changeListener);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@FXML public void typeChangeAction(ActionEvent event){

		final NetworkAddressType selectedItem = addressType.getSelectionModel().getSelectedItem();

		setFieldsDisable(!(selectedItem==NetworkAddressType.STATIC));

		setValueChanged(selectedItem!=curentType);
	}

	@FXML void saveAction(ActionEvent event){

		try {

			NetworkAddress networkAddress = new NetworkAddress();
			networkAddress.setType		(addressType		.getSelectionModel().getSelectedItem());
			networkAddress.setIpAddress	(addressController	.getVAlue());
			networkAddress.setMask		(maskController		.getVAlue());
			networkAddress.setGateway	(gatewayController	.getVAlue());

			final NetworkAddressPacket packet = new NetworkAddressPacket(networkAddress);
			packet.addObserver(this);
			SerialPortController.QUEUE.add(packet, true);

			setValueChanged(false);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@FXML void canselAction(ActionEvent event){
		setValueChanged(false);
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(5);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {

		LinkedPacket p = new NetworkAddressPacket(packet.getAnswer(), true);

		if(p.getPacketHeader().getPacketError()!=PacketErrors.NO_ERROR){
			logger.warn("\n\tPacket has Error:\n\t sent packet{}\n\n\t received packet{}", packet, p);
			return;
		}

		final Payload payload = p.getPayloads().get(0);
		final NetworkAddress networkAddress = new NetworkAddress(payload.getBuffer());

		final SingleSelectionModel<NetworkAddressType> selectionModel = addressType.getSelectionModel();
		curentType = networkAddress.getType();

		if(selectionModel.getSelectedItem()!=curentType)
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					selectionModel.select(curentType);
					typeChangeAction(null);
				}
			});

		setFielValue(addressController, networkAddress.getIpAddress());
		setFielValue(maskController, 	networkAddress.getMask());
		setFielValue(gatewayController, networkAddress.getGateway());
	}

	@Override
	public void doUpdate(boolean update) {
		if(!valueChanged)
			super.doUpdate(update);
	}

	private void setFielValue(NetworkFieldController fieldController, byte[] networkAddress) {
		fieldController.removeListener(changeListener);
		fieldController.setFields(networkAddress);
		fieldController.addListener(changeListener);
	}

	private final ChangeListener<byte[]> changeListener = new ChangeListener<byte[]>() {

		@Override
		public void changed(ObservableValue<? extends byte[]> observable, byte[] oldValue, byte[] newValue) {
			setValueChanged(!Arrays.equals(oldValue, newValue));
		}
	};

	private void setFieldsDisable(boolean disable) {
		addressController.setDisable(disable);
		maskController.setDisable(disable);
		gatewayController.setDisable(disable);
	}

	private void setButtonsDisable(boolean disable) {

		saveButton.setDisable(disable);
		canselButton.setDisable(disable);
	}

	private void setValueChanged(boolean vc) {

		if(vc && !valueChanged){

			super.doUpdate(false);
			setButtonsDisable(false);
			valueChanged = true;

		}else if(valueChanged && !vc){

			super.doUpdate(true);
			setButtonsDisable(true);
			valueChanged = false;
		}
	}
}
