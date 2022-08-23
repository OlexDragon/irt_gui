package irt.gui.controllers.components;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class NetworkFieldController{

	private final  SimpleObjectProperty<byte[]> objectProperty = new SimpleObjectProperty<>();
	private ChangeListener<String> changeListener = new ChangeListener<String>() {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//			System.out.println("oldValue:"+oldValue+ "; newValue:"+newValue);

			if(!oldValue.matches("\\d*") || newValue.isEmpty() || oldValue.equals(newValue) || Integer.parseInt(oldValue)>0xFF)
				return;

			if(!newValue.matches("\\d*") || Integer.parseInt(newValue)>0xFF){
				((StringProperty)observable).setValue(oldValue);
				return;
			}

			objectProperty.setValue(convertFieldsToBytes());
		}

		private byte[] convertFieldsToBytes() {

			final String text1 = textField1.getText();
			final String text2 = textField2.getText();
			final String text3 = textField3.getText();
			final String text4 = textField4.getText();

			byte[] result = new byte[4];

			if(!text1.isEmpty())
				result[0] = (byte)Integer.parseInt(text1);

			if(!text1.isEmpty())
				result[1] = (byte)Integer.parseInt(text2);

			if(!text1.isEmpty())
				result[2] = (byte)Integer.parseInt(text3);

			if(!text1.isEmpty())
				result[3] = (byte)Integer.parseInt(text4);

			return result;
		}
	};

	@FXML private TextField textField1;
	@FXML private TextField textField2;
	@FXML private TextField textField3;
	@FXML private TextField textField4;

	@FXML public void initialize(){

		textField1.textProperty().addListener(changeListener);
		textField2.textProperty().addListener(changeListener);
		textField3.textProperty().addListener(changeListener);
		textField4.textProperty().addListener(changeListener);
	}
	
	protected void setFields(byte[] ipAddress) {

		objectProperty.set(ipAddress);

		final String v1 = Integer.toString(ipAddress[0]&0xff);
		setFiel(textField1, v1);

		final String v2 = Integer.toString(ipAddress[1]&0xff);
		setFiel(textField2, v2);

		final String v3 = Integer.toString(ipAddress[2]&0xff);
		setFiel(textField3, v3);

		final String v4 = Integer.toString(ipAddress[3]&0xff);
		setFiel(textField4, v4);

	}

	private void setFiel(TextField textField, String text){
		if(!textField1.getText().equals(text))
			Platform.runLater(()-> {
				textField.textProperty().removeListener(changeListener);
				textField.setText(text);
				textField.textProperty().addListener(changeListener);
		});
	}

	public void addListener(ChangeListener<? super byte[]> listener){
		objectProperty.addListener(listener);
	}

	public void removeListener(ChangeListener<? super byte[]> listener){
		objectProperty.removeListener(listener);
	}

	public byte[] getVAlue(){
		return objectProperty.getValue();
	}

	public void setDisable(boolean disable){

		textField1.setDisable(disable);
		textField2.setDisable(disable);
		textField3.setDisable(disable);
		textField4.setDisable(disable);
	}
}
