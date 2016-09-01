package irt.controllers;

import java.util.Observable;
import java.util.Observer;

import irt.data.packets.interfaces.LinkedPacket;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;

public class TextFieldErrorController implements Observer{

	private static final String ERROR = "error";

	private TextField textField;

	public TextFieldErrorController(TextField textField){
		this.textField = textField;
	}
	@Override
	public void update(Observable o, Object arg) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				final ObservableList<String> styleClass = textField.getStyleClass();

				if(((LinkedPacket)o).getAnswer()==null){
					if(!styleClass.contains(ERROR)){
						styleClass.add(ERROR);
					}

				}else if(styleClass.size()>0)	// if size == 0 throw  java.lang.ArrayIndexOutOfBoundsException
					styleClass.remove(ERROR);

			}
		});
	}

}
