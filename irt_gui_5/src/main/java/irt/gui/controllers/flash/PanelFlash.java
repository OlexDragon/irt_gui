
package irt.gui.controllers.flash;

import static irt.gui.controllers.flash.ButtonLinkToFile.findTheFile;

import java.util.Arrays;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.flash.enums.Answer;
import irt.gui.controllers.flash.enums.UnitAddress;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.ToHex;
import irt.gui.data.packet.interfaces.LinkedPacket;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

public class PanelFlash extends Observable{

	public static final int MAX_VAR_RAM_SIZE = 256;// K Bytes
	public static byte[] LENGTH = new byte[] { (byte) (MAX_VAR_RAM_SIZE-1), (byte) (MAX_VAR_RAM_SIZE-1 ^ 0xFF) };

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private static Alert alert;

	private final  OnActionChoiceBox unitTypeChangeNotifier = new OnActionChoiceBox();
	private final ExecutorService executor = Executors.newSingleThreadExecutor(new MyThreadFactory());

	@FXML private ButtonConnect 		connectButtonController;
	@FXML private ButtonFCM 			fcmButtonController;
	@FXML private ButtonRead 			readButtonController;
	@FXML private ButtonWrite 			writeButtonController;
	@FXML private ButtonLinkToFile 		linkToFileButtonController;
	@FXML private ButtonErase 			eraseButtonController;
	@FXML private TextArea 				textArea;
	@FXML private ChoiceBox<UnitAddress> choiceBox;
	@FXML private MenuItem				menuItemEdit;

	private volatile String text;


	@FXML void initialize() {

		fcmButtonController.setConnectButton(connectButtonController);

		writeButtonController.setEraseObject(eraseButtonController);
		unitTypeChangeNotifier.addObserver(readButtonController);
		unitTypeChangeNotifier.addObserver(writeButtonController);
		unitTypeChangeNotifier.addObserver(eraseButtonController);
		unitTypeChangeNotifier.addObserver(linkToFileButtonController);

		linkToFileButtonController.setWriteButton(writeButtonController);
		linkToFileButtonController.setMenuItemEdit(menuItemEdit);

		
		final ChangeListener<? super String> listener = (observable, oldValue, newValue)->{

			if(newValue.equals(text))
				writeButtonController.setText(null);
			else
				writeButtonController.setText(textArea.getText());
		};
		textArea.textProperty().addListener(listener);

		readButtonController.addObserver((o, arg)->{
			Platform.runLater(()->{

				final StringProperty textProperty = textArea.textProperty();
				textProperty.removeListener(listener);

				if(arg instanceof String) {

					textArea.setText(textArea.getText() + arg);
					text = textArea.getText();

					if(choiceBox.getSelectionModel().getSelectedItem()!=UnitAddress.PROGRAM)
						if(!findTheFile.isFiles()){
							findTheFile.setText(text);
							executor.execute(findTheFile);
						}
				} else{
					//reset
					menuItemEdit.setDisable(true);
					textArea.setEditable(false);
					textArea.setText("");
					textArea.getStyleClass().remove("editable");
					findTheFile.reset();
					writeButtonController.setText(null);
				}

				textProperty.addListener(listener);
			});
		});

		final ObservableList<UnitAddress> observableArrayList = FXCollections.observableArrayList();
		Arrays
		.stream(UnitAddress.values())
		.forEach(observableArrayList::add);

		choiceBox.setItems(observableArrayList);
		choiceBox.setOnAction(unitTypeChangeNotifier);

		final String string = prefs.get("profile_address", UnitAddress.BIAS.name());
		final UnitAddress valueOf = UnitAddress.valueOf(string);
		choiceBox.getSelectionModel().select(valueOf);
	}

	@FXML public void onActionMemuItermEdit(){
		textArea.setEditable(true);
		final ObservableList<String> styleClass = textArea.getStyleClass();
		if(!styleClass.contains("editable"))
			styleClass.add("editable");
		menuItemEdit.setDisable(true);
	}
	//****************************   class OnActionChoiceBox   ******************************
	private class OnActionChoiceBox extends Observable implements EventHandler<ActionEvent>{
		
		@Override public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}

		@Override
		public void handle(ActionEvent event) {
			prefs.put("profile_address", choiceBox.getSelectionModel().getSelectedItem().name());
			unitTypeChangeNotifier.notifyObservers(choiceBox.getSelectionModel().getSelectedItem());
		}
	}

	//$$$$$$$$$$$$$$$$$$$$$   static methods   $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	public static byte[] addCheckSum(byte[] original) {

		byte[] result;

		if(original!=null){
			result = Arrays.copyOf(original, original.length + 1);
			result[original.length] = getCheckSum(original);
		}else
			result = null;

		return result;
	}

	private static byte getCheckSum(byte... original) {
		byte xor = 0;

		for (byte b : original)
			xor ^= b;

		return xor;
	}

	public static void showAlert(AlertType alertType, String contentText, Node node) {
		Platform.runLater(()->{
			if(alert==null || !alert.isShowing()){
				alert = new Alert(alertType);
				alert.initOwner(node.getScene().getWindow());
				alert.setContentText(contentText);
				alert.show();
			}
		});
	}

	public static boolean checkAswer(String description, LinkedPacket packet, Node node){

		boolean result = false;
		final byte[] received = Optional
								.ofNullable(packet.getAnswer())
								.orElse(new byte[]{0});

		final Optional<Answer> a = Answer.valueOf(received[0]);
		if(a.isPresent()){

			final Answer answer = a.get();
			if(answer == Answer.ACK)
				result = true;
			else
				PanelFlash.showAlert(AlertType.ERROR, description + answer, node);
		}else
			PanelFlash.showAlert(AlertType.ERROR, description + ToHex.bytesToHex(received), node);
	
		return result;
	}
}
