package irt.gui.controllers.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import irt.gui.controllers.interfaces.OtherFields;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.configuration.LoFrequenciesPacket;
import irt.gui.data.packet.observable.configuration.LoPacket;
import irt.gui.data.value.ValueFrequency;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.Pane;

public class ComboBoxLoSelect extends StartStopAbstract implements OtherFields{

	public static final Class<? extends Node> rootClass = ComboBox.class;
	public static final String FXML_PATH		= "/fxml/components/ComboBoxLoSelect.fxml";

	@FXML private ComboBox<String> loSelectComboBox;

	private  LinkedPacket 	packet;
	private  LoPacket 		setPacket;

	private final Map<String, Byte> values = new HashMap<>();
	private boolean error;
	

	public ComboBoxLoSelect() {
		try {

			packet = new LoFrequenciesPacket();

			setPacket = new LoPacket((byte) 0);
			setPacket.addObserver(this);

		} catch (Exception e) {
			logger.catching(e);
		} 
	}

	@FXML public void initialize() {
		addPacket(packet);
		loSelectComboBox.setUserData(this);
	}

	@FXML private void onActionLoSelectComboBox() {
		final String key = loSelectComboBox.getSelectionModel().getSelectedItem();
		final byte value = values.get(key);
		setPacket.setValue(value);
		SerialPortController.getQueue().add(setPacket, true);
	}

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)loSelectComboBox.getParent()).getChildren();
		nodes.remove(loSelectComboBox);
	}

	@Override public void update(Observable o, Object arg) {
		try {
			if(o instanceof LoFrequenciesPacket){

				LoFrequenciesPacket p = new LoFrequenciesPacket(((LinkedPacket)o).getAnswer(), true);
				error = false;
				final PacketHeader ph = p.getPacketHeader();

				if(ph.getPacketType()==PacketType.RESPONSE && ph.getPacketError()==PacketErrors.NO_ERROR){
					stop(true);
					fillComboBox(p.getPayloads().get(0));

					removeAllPackets();
					packet = new LoPacket();
					addPacket(packet);
					start();
				}

			}else if(o instanceof LoPacket){

				LoFrequenciesPacket p = new LoFrequenciesPacket(((LinkedPacket)o).getAnswer(), true);
				final PacketHeader ph = p.getPacketHeader();

				if(ph.getPacketType()==PacketType.RESPONSE && ph.getPacketError()==PacketErrors.NO_ERROR){
					selectLO(p);
				}
			}

		} catch (Exception e) {
			if(!error){
				error = true;
				logger.catching(e);
			}
		}
	}

	private void selectLO(LoFrequenciesPacket packet) {

		final Payload pl = packet.getPayloads().get(0);
		final byte loID = pl.getByte();
		final SingleSelectionModel<String> selectionModel = loSelectComboBox.getSelectionModel();

		values
		.entrySet()
		.parallelStream()
		.filter(s->s.getValue() == loID)
		.map(s->s.getKey())
		.filter(k->!k.equals(selectionModel.getSelectedItem()))
		.findAny()
		.ifPresent(k->Platform.runLater(()->{

			final EventHandler<ActionEvent> onAction = loSelectComboBox.getOnAction();
			loSelectComboBox.setOnAction(null);

			selectionModel.select(k);

			loSelectComboBox.setOnAction(onAction);
		}));

	}

	public void fillComboBox(Payload payload) {

		final byte[] frs = payload.getBuffer();
		for(int i=0; i< frs.length; i+=8){

			Byte id = frs[i];
			long v = payload.getLong((byte)++i);
			ValueFrequency vf = new ValueFrequency(v, v, v);

			final ObservableList<String> items = loSelectComboBox.getItems();
			final String key = vf.toString();
			items.add(key);
			values.put(key, id);
		}


//			packet = new LOPacket();
	}

	@Override public String getPropertyName() {
		return getClass().getName();
	}

	@Override public void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException { }
}
