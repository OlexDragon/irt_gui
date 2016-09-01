
package irt.controllers;

import java.util.Observable;
import java.util.Optional;

import irt.IrtGuiProperties;
import irt.data.GuiUtility;
import irt.data.LinkedPacketsQueue;
import irt.data.Value;
import irt.data.ValueDouble;
import irt.data.packets.PacketParsingException;
import irt.data.packets.core.Packet;
import irt.data.packets.core.Payload;
import irt.data.packets.enums.PacketErrors;
import irt.data.packets.interfaces.LinkedPacket;
import irt.data.packets.interfaces.ValuePacket;
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

public class LabelValue extends ScheduledNodeAbstract {

	public static final String FXML_PATH		= "/fxml/components/LabelValue.fxml";

	public static final String PROPERTY_STARTS_WITH = "gui.label.value.";		//Properties start with

	public static final String FIELD_KEY_ID	= RegistersController.REGISTER_PROPERTIES 		+ "value.%d.";
	public static final String FIELD_KEY 	= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.value.profikeId.column.row (ex. gui.regicter.controller.value.3.5.7)

	public static final Class<? extends Node> rootClass = BorderPane.class;

	@FXML private BorderPane 	borderPane;
	@FXML private Label 		valueLabel;
	@FXML private Label 		titleLabel;
	@FXML private ContextMenu 	contextMenu;
	@FXML private Menu 			menuValues;

	private Class<?> clazz;
	private TooltipWorker tooltipWorker;
	private final Updater updater = new Updater();

	@FXML public void initialize(){
		borderPane.setUserData(this);
		createMenuItems();
		titleLabel.setContextMenu(contextMenu);
		tooltipWorker = new TooltipWorker(valueLabel);
	}

	@FXML private void onActionRemove(){
		final ObservableList<Node> nodes = ((Pane)borderPane.getParent()).getChildren();
		nodes.remove(borderPane);
	}

	private void createMenuItems() {
		EventHandler<ActionEvent> action = e->{
			setKey(((MenuItem)e.getSource()).getId());
		};
		GuiUtility.createMamuItems(PROPERTY_STARTS_WITH, action, menuValues.getItems());
	}

	private void setKey(String propertiesKeyStartWith) {
		try {
			setKeyStartWith(propertiesKeyStartWith);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	public void setKeyStartWith(String propertiesKeyStartWith)  throws PacketParsingException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.entry(propertiesKeyStartWith);

		final Optional<String> className = Optional.ofNullable(IrtGuiProperties.getProperty(propertiesKeyStartWith + "class"));
		if(className.isPresent()){

			removeAllPackets();

			//Set Packet
			createPacket(className.get());

			//Select value menu
			final ObservableList<MenuItem> menuItems = menuValues.getItems();
			menuItems.parallelStream()
			.filter(mi->mi.getId().equals(propertiesKeyStartWith))
			.forEach(mi->Platform.runLater(()->((RadioMenuItem)mi).setSelected(true)));

			this.propertyName = propertiesKeyStartWith;
		}
	}

	private LinkedPacket createPacket(final String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.entry(className);

		clazz = Class.forName(className);
		LinkedPacket packet = (LinkedPacket) clazz.newInstance();

		//set title
		if(packet instanceof ValuePacket){
			final String title = ((ValuePacket)packet).getTitle();
			titleLabel.setText(title);
			final Tooltip tooltip = new Tooltip(title);
			titleLabel.setTooltip(tooltip);
			valueLabel.setTooltip(tooltip);
		}

		//Period
		Optional
		.ofNullable(IrtGuiProperties.getProperty(propertyName + PERIOD))
		.ifPresent(p->setPeriod(Long.parseLong(p)));

		addPacket(packet);

		return packet;
	}

	public enum Status{
		INDEFINED,
		EQUALS,
		LESS,
		MORE
	}

	@Override public void update(Observable observable, Object arg) {
		logger.entry(observable, arg);

		updater.setPacket((LinkedPacket)observable);
		LinkedPacketsQueue.SERVICES.execute(updater);
	}

	public static Class<? extends Node> getPootClass() {
		return BorderPane.class;
	}

	private class Updater implements Runnable{

		private LinkedPacket packet;

		@Override
		public void run() {
			try{
			if(packet.getAnswer()!=null)
				Optional
				.ofNullable(clazz)
				.ifPresent(c->{
					Optional
					.ofNullable((LinkedPacket)Packet.createNewPacket(packet.getClass(), packet.getAnswer(), true))
					.ifPresent(p->{
						Value v;

						final PacketErrors packetErrors = p.getPacketHeader().getPacketError();
						if(packetErrors!=PacketErrors.NO_ERROR){
							tooltipWorker.setMessage(packetErrors.toString());
							LinkedPacketsQueue.SERVICES.execute(tooltipWorker);
							logger.warn("The Packet has error:{}", p);
							return;
						}

						final Payload payload = p.getPayloads().get(0);

						Status status = null;
						long result;
						final short size = payload.getParameterHeader().getPayloadSize().getSize();
						if(size==2)
							result = payload.getShort(0);

						else if(size==3){
							status = Status.values()[payload.getByte()&3];
							result = payload.getShort((byte)1);

						}else if(size==4){
							result = payload.getInt(0);
						}else{
							logger.error("Wrong packet: {}", p);
							return;
						}

						if(p instanceof ValuePacket){
							final ValuePacket valuePacket = (ValuePacket)p;
							v = new ValueDouble(result, valuePacket.getPrecision());
							v.setPrefix(valuePacket.getPrefix());
						}else
							v = new Value(result, Long.MIN_VALUE, Long.MAX_VALUE, 0);

						String t = "";
						if(status!=null)
							switch(status){

							case EQUALS:
								if(v.getValue()==0x8000)
									t = "N/A";
								else
									t = v.toString();
								break;

							case INDEFINED:
								t = "N/A";
								break;

							case LESS:
								t = "<" + v.toString();
								break;

							case MORE:
								t = ">" + v.toString();
								break;

							default:
								break;
							}
						else
							t = v.toString();

						final String text = t;
						Platform.runLater(()->valueLabel.setText(text));
					});
				});
			else{
				tooltipWorker.setMessage("No answer.");
				LinkedPacketsQueue.SERVICES.execute(tooltipWorker);
			}
			}catch(Exception ex){
				logger.catching(ex);
			}
		}

		public void setPacket(LinkedPacket packet) {
			this.packet = packet;
		}
		
	}
}
