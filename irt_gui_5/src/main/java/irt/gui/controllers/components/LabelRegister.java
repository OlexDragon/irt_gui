
package irt.gui.controllers.components;

import java.util.Observable;
import java.util.Optional;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.data.GuiUtility;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.PacketHeader;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
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
import javafx.scene.layout.Pane;

public class LabelRegister extends ScheduledNodeAbstract {

	public static final String FXML_PATH		= "/fxml/components/LabelRegister.fxml";

	public static final String PROPERTY_STARTS_WITH	= "gui.label.register.";
	public static final String PROPERTY_NAME 	= PROPERTY_STARTS_WITH + "%s.name";	//Showing name of one properties
	public static final String PROPERTY_PERIOD 	= PROPERTY_STARTS_WITH + "%s.period";

	public static final String PROPERTY_NAMEs 	= "gui.value.label.register.names";		//All property names

	public static final String FIELD_KEY_ID 		= RegistersController.REGISTER_PROPERTIES 		+ "registerLabel.%d.";
	public static final String FIELD_KEY	 		= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.textField.profileId.column.row (ex. gui.regicter.controller.textField.3.5.7)

	private	volatile		Value			value;						/*Actual value	*/							public Value getValue() { return value; }

	public static final Class<? extends Node> rootClass = BorderPane.class;
	private TooltipWorker tooltipWorker;

	@FXML private BorderPane 	borderPane;
	@FXML private Label			registerLabel;
	@FXML private Label 		titleLabel;
	@FXML private ContextMenu 	contextMenu;
	@FXML private Menu 			menuValues;

	private boolean error;

	@FXML public void initialize(){
		borderPane.setUserData(this);
		titleLabel.setContextMenu(contextMenu);
		createMenuItems();
		tooltipWorker = new TooltipWorker(registerLabel);
	}

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)borderPane.getParent()).getChildren();
		nodes.remove(borderPane);
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

	@Override public void setKeyStartWith(String keyStartWith)  throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException {

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

		final String addr = IrtGuiProperties.getProperty(keyStartWith + "addr");
		int index = Integer.parseInt(addr);	//TODO  Have to check why addr<->index
		int address = Integer.parseInt(IrtGuiProperties.getProperty(keyStartWith + "index"));//TODO

		addPacket(new RegisterPacket(keyStartWith, new RegisterValue(index, address)));
	}

	@Override public void update(Observable observable, Object arg) {

		LinkedPacketsQueue.SERVICES.execute(()->{
			LinkedPacket packet = (LinkedPacket)observable;
			final Optional<RegisterPacket> oAnswer = Optional
														.ofNullable(packet.getAnswer())
														.map(this::createPacket)
														.filter(p->p!=null);

			if(oAnswer.isPresent()){

				final RegisterPacket p = oAnswer.get();
				final PacketHeader packetHeader = p.getPacketHeader();
				final PacketErrors packetErrors = packetHeader.getPacketError();

				if(packetErrors!=PacketErrors.NO_ERROR){

					tooltipWorker.setMessage(packetErrors.toString());
					LinkedPacketsQueue.SERVICES.execute(tooltipWorker);
					return;
				}

				Payload payload = p.getPayloads().get(0);
//					RegisterValue rv = new RegisterValue(payload.getInt(0), payload.getInt(1), payload.getInt(2));
				value.setValue(payload.getInt(2)&Long.MAX_VALUE);

				Platform
				.runLater(()->registerLabel.setText(value.toString()));

			}else{
				tooltipWorker.setMessage("No answer.");
				LinkedPacketsQueue.SERVICES.execute(tooltipWorker);
			}
		});
	}

	private RegisterPacket createPacket(byte[] answer) {
		try {

			final RegisterPacket registerPacket = new RegisterPacket(answer, true);
			error = false;
			return registerPacket;

		} catch (PacketParsingException e) {
			if(!error){	//not to repeat the same error message
				error = true;
				logger.catching(e);
			}
		}
		return null;
	}

	public static Class<? extends Node> getPootClass() {
		return BorderPane.class;
	}
}
