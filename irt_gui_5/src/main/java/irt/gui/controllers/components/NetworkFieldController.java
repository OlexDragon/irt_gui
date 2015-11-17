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
		addTextFieldsChangeListener();
	}

	private void addTextFieldsChangeListener() {
		textField1.textProperty().addListener(changeListener);
		textField2.textProperty().addListener(changeListener);
		textField3.textProperty().addListener(changeListener);
		textField4.textProperty().addListener(changeListener);
	}

	private void removeTextFieldsChangeListener() {
		textField1.textProperty().removeListener(changeListener);
		textField2.textProperty().removeListener(changeListener);
		textField3.textProperty().removeListener(changeListener);
		textField4.textProperty().removeListener(changeListener);
	}
	
	protected void setFields(byte[] ipAddress) {

		objectProperty.set(ipAddress);

		final String v1 = Integer.toString(ipAddress[0]&0xff);
		final String v2 = Integer.toString(ipAddress[1]&0xff);
		final String v3 = Integer.toString(ipAddress[2]&0xff);
		final String v4 = Integer.toString(ipAddress[3]&0xff);

		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				removeTextFieldsChangeListener();
				textField1.setText(v1);
				textField2.setText(v2);
				textField3.setText(v3);
				textField4.setText(v4);
				addTextFieldsChangeListener();
			}
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
