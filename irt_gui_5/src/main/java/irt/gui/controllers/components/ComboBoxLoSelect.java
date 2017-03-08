package irt.gui.controllers.components;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public static final String FXML_PATH				= "/fxml/components/ComboBoxLoSelect.fxml";

	@FXML private ComboBox<String> loSelectComboBox;

	private  LinkedPacket 	packet;
	private  LoPacket 		setPacket;

	private final Map<String, Long> values = new HashMap<>();
	private boolean error;

	public ComboBoxLoSelect() {
		logger.traceEntry();
		try {

			packet = new LoFrequenciesPacket();

			setPacket = new LoPacket((byte) 0);
//			setPacket.addObserver(this);

		} catch (Exception e) {
			logger.catching(e);
		} 
	}

	@FXML public void initialize() {
		logger.traceEntry();
		addPacket(packet);
		loSelectComboBox.setUserData(this);
	}

	@FXML private void onActionLoSelectComboBox() {
		logger.traceEntry();
		final String key = loSelectComboBox.getSelectionModel().getSelectedItem();
		final Long value = values.get(key);
		setPacket.setValue(value);
		SerialPortController.getQueue().add(setPacket, true);
	}

	@FXML private void onActionRemove(){
		logger.traceEntry();
		final ObservableList<Node> nodes = ((Pane)loSelectComboBox.getParent()).getChildren();
		nodes.remove(loSelectComboBox);
	}

	@Override public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		try {
			if(o instanceof LoFrequenciesPacket){

				LoFrequenciesPacket p = new LoFrequenciesPacket(((LinkedPacket)o).getAnswer(), true);
				logger.debug(p);
				error = false;
				final PacketHeader ph = p.getPacketHeader();

				if(ph.getPacketType()==PacketType.RESPONSE && ph.getPacketError()==PacketErrors.NO_ERROR){

					stop(true);

					if(p.getLinkHeader().getAddr()==-1)
						fillComboBoxConverter(p.getPayloads().get(0));
					else
						fillComboBoxbBUC(p.getPayloads().get(0));

					removeAllPackets();
					packet = new LoPacket();
					addPacket(packet);
					start();
				}else
					logger.warn("Packet has an error: {}", p);

			}else if(o instanceof LoPacket){

				LoFrequenciesPacket p = new LoFrequenciesPacket(((LinkedPacket)o).getAnswer(), true);
				logger.debug(p);
				final PacketHeader ph = p.getPacketHeader();

				if(ph.getPacketType()==PacketType.RESPONSE && ph.getPacketError()==PacketErrors.NO_ERROR)
					if(p.getLinkHeader().getAddr()==-1)
						selectLOConverter(p);
					else
						selectLOBuc(p);
			}

		} catch (Exception e) {
			if(!error){
				error = true;
				logger.catching(e);
			}
		}
	}

	private void selectLOConverter(LoFrequenciesPacket packet) {
		logger.entry(packet);

		final Payload pl = packet.getPayloads().get(0);
		final long loID = pl.getLong();

		values.entrySet().parallelStream().filter(es->es.getValue()==loID).map(es->es.getKey()).findAny().ifPresent(key->Platform.runLater(()->loSelectComboBox.getSelectionModel().select(key)));
	}

	private void selectLOBuc(LoFrequenciesPacket packet) {
		logger.entry(packet);

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

	private void fillComboBoxConverter(Payload payload) {
		logger.entry(payload);

		final byte[] frs = payload.getBuffer();
		ByteBuffer bb = ByteBuffer.wrap(frs);
		final Set<Long> collect = IntStream.range(0, frs.length/8).parallel().mapToLong(i->bb.getLong(i*8)).boxed().collect(Collectors.toSet());
		collect.stream().forEach(l->values.put(new ValueFrequency(l, l, l).toString(), l));

		logger.trace("{}", values);
		fillComboBox();
	}

	public void fillComboBoxbBUC(Payload payload) {
		logger.entry(payload);

		final byte[] frs = payload.getBuffer();
		for(int i=0; i< frs.length; i+=8){

			Byte id = frs[i];
			long v = payload.getLong((byte)++i);
			ValueFrequency vf = new ValueFrequency(v, v, v);
			final String key = vf.toString();
			values.put(key, id.longValue());
		}

		fillComboBox();
	}

	private void fillComboBox() {
		Platform.runLater(()->{
			final ObservableList<String> items = loSelectComboBox.getItems();
			items.clear();
			final Set<String> keySet = values.keySet();
			final ArrayList<String> list = new ArrayList<>(keySet);
			Collections.sort(list);
			items.addAll(list);
		});
	}

	@Override public String getPropertyName() {
		return getClass().getName();
	}

	@Override public void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException { }
}
