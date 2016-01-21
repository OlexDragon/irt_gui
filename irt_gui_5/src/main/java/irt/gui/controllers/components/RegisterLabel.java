
package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Optional;

import irt.gui.IrtGuiProperties;
import irt.gui.data.GuiUtility;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketType;
import irt.gui.data.packet.observable.device_debug.RegisterPacket;
import irt.gui.data.value.Value;
import irt.gui.data.value.ValueDouble;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

public class RegisterLabel extends ScheduledNodeAbstract {

	public static final String FXML_PATH		= "/fxml/components/RegisterLabel.fxml";

	public static final String PROPERTY_STARTS_WITH	= "gui.register.label.";
	public static final String PROPERTY_NAME 	= PROPERTY_STARTS_WITH + "%s.name";	//Showing name of one properties
	public static final String PROPERTY_PERIOD 	= PROPERTY_STARTS_WITH + "%s.period";

	public static final String PROPERTY_NAMEs 	= "gui.value.label.register.names";		//All property names

	public static final String FIELD_KEY_ID 		= RegistersController.REGISTER_PROPERTIES 		+ "registerLabel.%d.";
	public static final String FIELD_KEY	 		= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profikeId.column.row (ex. gui.regicter.controller.textField.3.5.7)

	private	volatile		Value			value;						/*Actual value	*/							public Value getValue() { return value; }

	public static final Class<? extends Node> rootClass = BorderPane.class;

	@FXML private BorderPane 	borderPane;
	@FXML private Label			registerLabel;
	@FXML private Label 		titleLabel;
	@FXML private ContextMenu 	contextMenu;
	@FXML private Menu 			menuValues;

	@FXML public void initialize(){
		borderPane.setUserData(this);
		titleLabel.setContextMenu(contextMenu);
		createMenuItems();
	}

	private void createMenuItems(){

		EventHandler<ActionEvent> onActionRegisterSelect = e->{
			final MenuItem source = (MenuItem) e.getSource();
			try {
				setKeyStartWith(source.getId());
			} catch (Exception e1) {
				logger.catching(e1);
			}
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, onActionRegisterSelect, menuValues.getItems());
	}

	@Override
	public void setKeyStartWith(String keyStartWith)  throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		// Stop sending packets
		stop(false);

		if(keyStartWith==null || keyStartWith.isEmpty())
			return;

		this.propertyName = keyStartWith;

		setValue(keyStartWith);

		try {
			setPacket(keyStartWith);
		} catch (Exception e) {
			logger.catching(e);
		}

		setTitle(keyStartWith);

		Optional
		.ofNullable(IrtGuiProperties.getProperty(keyStartWith + "period"))
		.ifPresent(p->setPeriod( Long.parseLong(p)));

		//Select register menu
		final ObservableList<MenuItem> menuItems = menuValues.getItems();

		menuItems
		.parallelStream()
		.filter(mi->mi.getId().equals(keyStartWith))
		.forEach(mi->Platform.runLater(()->((RadioMenuItem)mi).setSelected(true)));

		start();
	}

	private void setValue(String keyStartWith) {
		int precision = Optional
							.ofNullable(IrtGuiProperties.getLong(keyStartWith + "precision"))
							.orElse(0L)
							.intValue();

		if(precision == 0)
			value = new Value(0L, Long.MIN_VALUE, Long.MAX_VALUE, 0);
		else
			value = new ValueDouble(0, Long.MIN_VALUE, Long.MAX_VALUE, precision);

		Optional
		.ofNullable(IrtGuiProperties.getProperty(keyStartWith + "prefix"))
		.ifPresent(p->value.setPrefix(p));
	}

	private void setTitle(String keyStartWith) {
		String name = IrtGuiProperties.getProperty(keyStartWith + "name");
		final Tooltip tooltip = new Tooltip(name);
		Platform.runLater(()->{
			titleLabel.setText(name);
			titleLabel.setTooltip(tooltip);
			registerLabel.setTooltip(tooltip);
		});
	}

	private void setPacket(String keyStartWith) throws PacketParsingException {

		removeAllPackets();

		int index = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "index"));
		int address = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "addr"));

		addPacket(new RegisterPacket(new RegisterValue(index, address)));
	}

	@Override
	public void update(Observable observable, Object arg) {

		LinkedPacket packet = (LinkedPacket)observable;
		Optional
		.ofNullable(packet.getAnswer())
		.ifPresent(answer->{
			Optional.ofNullable(createPacket(answer))
			.ifPresent(p->{
				final PacketHeader packetHeader = p.getPacketHeader();
				if(packetHeader.getPacketType()==PacketType.RESPONSE && packetHeader.getPacketErrors()==PacketErrors.NO_ERROR){

					Payload payload = p.getPayloads().get(0);
//					RegisterValue rv = new RegisterValue(payload.getInt(0), payload.getInt(1), payload.getInt(2));
					value.setValue(payload.getInt(2));

					Platform
					.runLater(()->registerLabel.setText(value.toString()));
				}
			});
		});
	}

	private RegisterPacket createPacket(byte[] answer) {
		try {
			return new RegisterPacket(answer);
		} catch (PacketParsingException e) {
			logger.catching(e);
		}
		return null;
	}

	public static Class<? extends Node> getPootClass() {
		return BorderPane.class;
	}
}
