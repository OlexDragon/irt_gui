package irt.gui.controllers.components;

import java.util.Arrays;
import java.util.Observable;

import irt.gui.controllers.components.LabelStatus.StatusByte;
import irt.gui.controllers.interfaces.OtherFields;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.enums.PacketType;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.configuration.Ref10MHzSourcePacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

public class ComboBox10MHzReferenceSource extends StartStopAbstract implements OtherFields{

	public static final Class<? extends Node> rootClass = ComboBox.class;
	public static final String FXML_PATH				= "/fxml/components/ComboBox10MHzReferenceSource.fxml";

	@FXML private ComboBox<Ref10MHzStatusBits> sourceSelectComboBox;

	private  LinkedPacket 			packet;
	private  Ref10MHzSourcePacket	setPacket;

	private boolean error;

	public ComboBox10MHzReferenceSource() {
		logger.error(logger.getName());
		try {

			packet = new Ref10MHzSourcePacket();

			setPacket = new Ref10MHzSourcePacket((byte) 0);
//			setPacket.addObserver(this);

		} catch (Exception e) {
			logger.catching(e);
		} 
	}

	@FXML public void initialize() {
		logger.traceEntry();
		addPacket(packet);
		sourceSelectComboBox.setUserData(this);
		setPacket.addObserver(this);

		//Fill loSelectComboBox
		final ObservableList<Ref10MHzStatusBits> items = sourceSelectComboBox.getItems();
		final Ref10MHzStatusBits[] values = Ref10MHzStatusBits.values();

		logger.error(Arrays.toString(values));
		Arrays.stream(values).filter(status->status!=Ref10MHzStatusBits.LOCK_SUMMARY).forEach(status->Platform.runLater(()->items.add(status)));
	}

	@FXML private void onActionSourceSelectComboBox() {	//	Select source [INTERNAL, EXTERNAL, AUTOSENCE]
		final int value = sourceSelectComboBox.getSelectionModel().getSelectedIndex();
		setPacket.setValue(value);
		logger.debug(setPacket);
		SerialPortController.getQueue().add(setPacket, true);
	}

	@FXML private void onActionRemove(){
		logger.traceEntry();
		final ObservableList<Node> nodes = ((Pane)sourceSelectComboBox.getParent()).getChildren();
		nodes.remove(sourceSelectComboBox);
	}

	@Override public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		try {
			if(o instanceof Ref10MHzSourcePacket){

				Ref10MHzSourcePacket p = new Ref10MHzSourcePacket(((LinkedPacket)o).getAnswer(), true);
				logger.debug(p);
				final PacketHeader ph = p.getPacketHeader();

				if(ph.getPacketType()==PacketType.RESPONSE && ph.getPacketError()==PacketErrors.NO_ERROR)
					p.getPayloads().stream().findAny().ifPresent(pl->{
						final byte index = pl.getByte();
						Platform.runLater(()->sourceSelectComboBox.getSelectionModel().select(index));
					});
			}

		} catch (Exception e) {
			if(!error){
				error = true;
				logger.catching(e);
			}
		}
	}

	@Override public String getPropertyName() {
		return getClass().getName();
	}

	@Override public void setKeyStartWith(String name) throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException { }

	public static final int bit5 = 32;
	public enum Ref10MHzStatusBits implements StatusByte{

		UNDEFINED 	(0),
		INTERNAL	(1),
		EXTERNAL	(2),
		AUTOSENSE	(3),

		LOCK_SUMMARY (bit5);

		private int value;

		private Ref10MHzStatusBits(int value){
			this.value = value;
		}

		@Override public boolean isOn(Integer status) {
			return (status&3) == value || (status&bit5)==value;
		}
	}
}
