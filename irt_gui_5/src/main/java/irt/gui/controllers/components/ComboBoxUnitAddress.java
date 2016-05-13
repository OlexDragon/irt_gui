package irt.gui.controllers.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import irt.gui.IrtGuiProperties;
import irt.gui.data.converters.AddressIntegerStringConverter;
import irt.gui.data.listeners.AddressListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class ComboBoxUnitAddress extends Observable {

	public final static int defaultAddress = 254;

	private static final String 		KEY_ADDRESSES 	= "addresses";
	private static final String 		KEY_SELECTED_ADDR 	= "selected_addr";

	private final static Preferences 	prefs 	= Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	@FXML private ComboBox<Integer> addressComboBox;

	@FXML private void initialize() {

		setupComboBox();
		fillComboBox();
		setSelectedAddress();
	}

	@FXML private void onActionAddressComboBox() {

    	Optional
    	.ofNullable(addressComboBox.getSelectionModel().getSelectedItem())
    	.ifPresent(selectedItem->{
    		
        	setChanged();
    		notifyObservers(selectedItem);

        	savePreference();
        	saveSelected(selectedItem);
    	});
    }

	@Override
	public synchronized void addObserver(Observer o) {
		super.addObserver(o);
		onActionAddressComboBox();
	}

	private void saveSelected(Integer selectedItem) {
		prefs.putInt(KEY_SELECTED_ADDR, selectedItem);
	}

	public Integer getAddress() {
		return addressComboBox.getSelectionModel().getSelectedItem();
	}

	private void setupComboBox() {
		addressComboBox.setUserData(this);
		addressComboBox.setConverter(new AddressIntegerStringConverter());
		new AddressListener(addressComboBox.getEditor().textProperty());
	}

	private void fillComboBox() {

		final byte[] byteArray = prefs.getByteArray(KEY_ADDRESSES, new byte[]{(byte)254});
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
			prefs.putByteArray(KEY_ADDRESSES, bytes);
		}
		
	}

    private void setSelectedAddress() {
		final Integer a = prefs.getInt(KEY_SELECTED_ADDR, defaultAddress);
		if(a!=defaultAddress){
			addressComboBox.getSelectionModel().select(a);
			onActionAddressComboBox();
		}
	}
}
