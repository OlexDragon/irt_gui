
package irt.gui.controllers.components;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.StringData;
import irt.gui.data.packet.Payload;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.LinkedPacket.PacketErrors;
import irt.gui.data.packet.observable.device_debug.DebugInfoPacket;
import irt.gui.data.packet.observable.device_debug.DebugInfoPacket.DebugInfoCode;
import irt.gui.data.packet.observable.device_debug.RegisterIndexesPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DebugInfoController extends FieldsControllerAbstract {

	private DebugInfoPacket 		infoPacket;
	private RegisterIndexesPacket 	indexesPacket;

	@FXML private ComboBox<DebugInfoCode> commandComboBox;
	@FXML private ComboBox<Integer> parameterComboBox;
	@FXML private Button plussButton;

	@FXML private TextArea textArea;
	@FXML private ScrollPane scrollPane;

	@FXML public void initialize(){

		commandComboBox.getItems().add(DebugInfoCode.INFO);
		commandComboBox.getItems().add(DebugInfoCode.DUMP);
		commandComboBox.getSelectionModel().select(0);

		try {

			indexesPacket = new RegisterIndexesPacket();
			addLinkedPacket(indexesPacket);

		} catch (Exception e) {
			logger.catching(e);
		}
		addPacket();
	}

	@FXML public void onAction(ActionEvent event){
		doUpdate(false);
		removeLinkedPacket(infoPacket);
		addPacket();
		doUpdate(true);
	}

	@FXML public void plussButtonAction(ActionEvent event){

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/DebugInfoPanel.fxml"));  
		try {

			Parent root = (Parent) loader.load();
			DebugInfoController dic = (DebugInfoController) loader.getController();
			dic.doUpdate(true);

			Scene scene = new Scene(root);  
			Stage stage = new Stage();  
			stage.setScene(scene);  
			stage.setTitle("My Window");  
			stage.show();
			stage.addEventHandler( WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					dic.doUpdate(false);
				}});

		} catch (IOException e) {
			logger.catching(e);
		}  
	}

	@FXML public void hButtonVction(ActionEvent event){
		doUpdate(false);
		Optional
		.ofNullable(commandComboBox.getTooltip())
		.ifPresent(tt->textArea.setText(tt.getText()));
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(5);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {
		LinkedPacket p = new DebugInfoPacket(packet.getAnswer());

		if(p.getPacketHeader().getPacketErrors()!=PacketErrors.NO_ERROR){
			textArea.setText(p.getPacketHeader().getPacketErrors().toString());
			return;
		}

		final Payload payload = p.getPayloads().get(0);
		final StringData stringData = payload.getStringData();
		logger.trace(stringData);

		if(p.getPacketHeader().getPacketIdDetails().getPacketId()==RegisterIndexesPacket.PACKET_ID){

			Platform.runLater(()->commandComboBox.setTooltip(new Tooltip(stringData.toString())));
			setParameters(stringData);

		}else{
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					final double vvalue = scrollPane.getVvalue();//TODO It does not work WHY?
					textArea.setText(stringData.toString());
					scrollPane.setVvalue(vvalue);
				}
			});
		}
	}

	private void setParameters(StringData stringData) {

		Set<Integer> properties = new TreeSet<>();

		try(Scanner s = new Scanner(stringData.toString());){
			while(s.hasNextLine())
				properties.addAll(parseLine(s.nextLine()));
		}

		
		final ObservableList<Integer> items = parameterComboBox.getItems();
		if(items.isEmpty()){
			items.addAll(properties);
			parameterComboBox.getSelectionModel().select(0);
		}
		removeLinkedPacket(indexesPacket);
	}

	private List<Integer> parseLine(String line) {

		List<Integer> p = new ArrayList<>();
		int firstIndex = line.indexOf("[");

		if(firstIndex>=0){
			int endIndex = line.indexOf("]", ++firstIndex);
			if(endIndex>=0)
				p.addAll(parseSubstring(line.substring(firstIndex, endIndex)));
		}

		return p;
	}

	private Collection<? extends Integer> parseSubstring(String substring) {

		final String[] separators = new String[]{", ", " - "};
		int separatorIndex = separators.length;

		List<Integer> p = new ArrayList<>();

		for(int i=0; i<separators.length; i++)
			if(substring.contains(separators[i])){
				separatorIndex = i;
				break;
			}

		if(separatorIndex == separators.length)
			p.add(Integer.parseInt(substring.trim()));
		else{

			final Stream<Integer> splitAsStream = Pattern.compile(separators[separatorIndex]).splitAsStream(substring).map(s->Integer.parseInt(s.trim()));

			switch(separatorIndex){
			case 1:
				final List<Integer> collect = splitAsStream.collect(Collectors.toList());
				p.addAll(IntStream.range(collect.get(0), collect.get(1)+1).boxed().collect(Collectors.toList()));
				break;
			default:
				p.addAll(splitAsStream.collect(Collectors.toList()));
			}

		}

		return p;
	}

	private void addPacket() {
		try {

			final DebugInfoCode debugInfoCode = commandComboBox.getSelectionModel().getSelectedItem();
			final Object object = parameterComboBox.getSelectionModel().getSelectedItem();

			Integer parameter;
			if(object == null)
				parameter = 0;
			else if(object instanceof String) {

				try{
					parameter = Integer.parseInt(((String) object).trim());
				}catch(Exception e){
					parameter = 0;
				}

			} else
				parameter = parameterComboBox.getSelectionModel().getSelectedItem();

			infoPacket = new DebugInfoPacket(debugInfoCode, parameter);
			addLinkedPacket(infoPacket);

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	}
}
