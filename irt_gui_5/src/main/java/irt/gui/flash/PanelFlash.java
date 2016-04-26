
package irt.gui.flash;

import java.util.Arrays;
import java.util.Observable;
import java.util.Optional;
import java.util.prefs.Preferences;

import irt.gui.IrtGuiProperties;
import irt.gui.data.ToHex;
import irt.gui.data.packet.interfaces.LinkedPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class PanelFlash extends Observable{

	public static final int MAX_VAR_RAM_SIZE = 256;// K Bytes
	public static byte[] LENGTH = new byte[] { (byte) (MAX_VAR_RAM_SIZE-1), (byte) (MAX_VAR_RAM_SIZE-1 ^ 0xFF) };

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private static Alert alert;

	@FXML private ButtonConnect 		connectButtonController;
	@FXML private ChoiceBox<UnitAddress> choiceBox;
	@FXML private ButtonRead 			readButtonController;
	@FXML private ButtonWrite 			writeButtonController;
	@FXML private ButtonErase 			eraseButtonController;
	@FXML private TextArea 				textArea;

	private final  OnActionChoiceBox unitTypeChangeNotifier = new OnActionChoiceBox();

	@FXML void initialize() {

		unitTypeChangeNotifier.addObserver(readButtonController);
		unitTypeChangeNotifier.addObserver(writeButtonController);
		unitTypeChangeNotifier.addObserver(eraseButtonController);
		readButtonController.addObserver((o, arg)->{
			Platform.runLater(()->{
				if(arg instanceof String)
					textArea.setText(textArea.getText() + arg);
				else
					textArea.setText("");
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

	//------------------------- enums -----------------------------------
	public enum Answer {
		UNKNOWN	((byte) -1),
		NULL	((byte) 0),
		ACK		((byte) 0x79),
		NACK	((byte) 0x1F);

		private final byte answer;

		private Answer(byte answer) {
			this.answer = answer;
		}

		public byte getAnswer() {
			return answer;
		}

		public static Optional<Answer> valueOf(byte key){
			return Arrays
					.stream(values())
					.filter(a->a.answer==key)
					.findAny();
		}

		@Override
		public String toString() {
			return name()+" (0x"+ToHex.bytesToHex(answer)+ ")";
		}
	}

	public enum UnitAddress {
		PROGRAM("PROGRAM", 0x08000000),
		CONVERTER("CONVERTER", 0x080C0000),
		BIAS("BIAS BOARD", 0x080E0000),
		HP_BIAS("HP BIAS", 0x081E0000);

		private String text;
		private int addr;

		private UnitAddress(String name, int addr) {
			this.text = name;
			this.addr = addr;
		}

		public int getAddr() {
			return addr;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Command {
		EMPTY			(new byte[] { }),
		/** 0x7F */
		CONNECT			(new byte[] { 0x7F }),
		/** byte[] { 0x00, 0xFF} */
		GET				(new byte[] { 0x00, (byte) 0xFF }),
		/** byte[] { 0x11, 0xEE} */
		READ_MEMORY		(new byte[] { 0x11, (byte) 0xEE }),
		/** byte[] { 0x31, 0xCE} */
		WRITE_MEMORY	(new byte[] { 0x31, (byte) 0xCE }),
		/** byte[] { 0x43, 0xBC} */
		ERASE			(new byte[] { 0x43, (byte) 0xBC }),
		/** byte[] { 0x44, 0xBB} */
		EXTENDED_ERASE	(new byte[] { 0x44, (byte) 0xBB });

		private byte[] command;

		private Command(byte[] command) {
			this.command = command;
		}

		public byte[] toBytes() {
			return command;
		}
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
