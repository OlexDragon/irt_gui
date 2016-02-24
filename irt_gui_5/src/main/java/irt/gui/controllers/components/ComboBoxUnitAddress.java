package irt.gui.controllers.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observable;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import irt.gui.IrtGuiProperties;
import irt.gui.data.listeners.NumericChecker;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.util.converter.IntegerStringConverter;

public class ComboBoxUnitAddress extends Observable {

	private static final String 		KEY 	= "addresses";
	private final static Preferences 	prefs 	= Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	@FXML private ComboBox<Integer> addressComboBox;

	@FXML private void initialize() {

		setupComboBox();
		fillComboBox();
	}

    @FXML private void onActionAddressComboBox(ActionEvent event) {

    	setChanged();
    	notifyObservers(addressComboBox.getSelectionModel().getSelectedItem());

    	savePreference();
    }

	public Integer getAddress() {
		return addressComboBox.getSelectionModel().getSelectedItem();
	}

	private void setupComboBox() {
		addressComboBox.setUserData(this);
		addressComboBox.setConverter(new IntegerStringConverter());
		new NumericChecker(addressComboBox.getEditor().textProperty());
	}

	private void fillComboBox() {

		final byte[] byteArray = prefs.getByteArray(KEY, new byte[]{(byte)254});
		final ObservableList<Integer> items = addressComboBox.getItems();
		final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

		IntStream
		.generate(bais::read)
		.limit(bais.available())
		.forEachOrdered(i->items.add(i));
	}

	public void savePreference() {
		final Integer selectedItem = getAddress();
		final ObservableList<Integer> items = addressComboBox.getItems();
		boolean doSave = items
							.parallelStream()
							.filter(i->i==selectedItem)
							.map(i->true)
							.count()==0;
		if(doSave){
			items.add(selectedItem);
			final byte[] bytes = (byte[]) items
									.parallelStream()
									.sorted()
									.collect(ByteArrayOutputStream::new, (baos, i)->baos.write(i.byteValue()), (baos1, baos2) -> baos1.write(baos2.toByteArray(), 0, baos2.size()))
									.toByteArray();
			prefs.putByteArray(KEY, bytes);
		}
		
	}
}
