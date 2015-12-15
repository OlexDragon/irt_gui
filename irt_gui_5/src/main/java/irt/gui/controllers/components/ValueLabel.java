
package irt.gui.controllers.components;

import java.lang.reflect.Constructor;
import java.util.Observable;
import java.util.Optional;

import irt.gui.IrtGuiProperties;
import irt.gui.data.GuiUtility;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.ValuePacket;
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

public class ValueLabel extends ScheduledNode {

	public static final String FXML_PATH		= "/fxml/components/ValueLabel.fxml";

	public static final String PROPERTY_STARTS_WITH = "gui.label.value.";		//Properties start with

	public static final String FIELD_KEY_ID	= RegistersController.REGISTER_PROPERTIES 		+ "value.%d.";
	public static final String FIELD_KEY 	= FIELD_KEY_ID 	+ "%d.%d";			//gui.regicter.controller.value.profikeId.column.row (ex. gui.regicter.controller.value.3.5.7)


	@FXML private BorderPane 	borderPane;
	@FXML private Label 		valueLabel;
	@FXML private Label 		titleLabel;
	@FXML private ContextMenu 	contextMenu;
	@FXML private Menu 			menuValues;

	private Class<?> clazz;

	@FXML public void initialize(){
		borderPane.setUserData(this);
		createMenuItems();
		titleLabel.setContextMenu(contextMenu);
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
			stop(true);
			
			removeAllPackets();

			//Set PAcket
			createPacket(className.get());

			//Select value menu
			final ObservableList<MenuItem> menuItems = menuValues.getItems();
			menuItems.parallelStream()
			.filter(mi->mi.getId().equals(propertiesKeyStartWith))
			.forEach(mi->Platform.runLater(()->((RadioMenuItem)mi).setSelected(true)));

			this.propertyName = propertiesKeyStartWith;

			start();
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

	@Override
	public void update(Observable observable, Object arg) {
		LinkedPacket packet = (LinkedPacket)observable;
		Optional
		.ofNullable(clazz)
		.ifPresent(c->{
			Optional
			.ofNullable(createPacket(packet))
			.ifPresent(p->{
				Value v;
				final short result = p.getPayloads().get(0).getShort(0);
				if(p instanceof ValuePacket){
					final ValuePacket valuePacket = (ValuePacket)p;
					v = new ValueDouble(result, valuePacket.getPrecision());
					v.setPrefix(valuePacket.getPrefix());
				}else
					v = new Value(result, Long.MIN_VALUE, Long.MAX_VALUE, 0);
				
				Platform
				.runLater(()->{
					valueLabel.setText(v.toString());
				});
			});
			
		});
	}

	private LinkedPacket createPacket(LinkedPacket packet){
		try {

			final Constructor<?> constructor = clazz.getConstructor(byte[].class);
			return (LinkedPacket) constructor.newInstance(packet.getAnswer());

		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
	}

	public static Class<? extends Node> getPootClass() {
		return BorderPane.class;
	}
}
