package irt.serial.port.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.IrtGuiProperties;
import irt.serial.port.listener.AddressListener;
import irt.services.AddressIntegerStringConverter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ComboBoxUnitAddress extends Observable {
	private final Logger logger = LogManager.getLogger();

	public final static int defaultAddress = 254;

	public static final String 		KEY_ADDRESSES 	= "addresses";
	public static final String 		KEY_SELECTED_ADDR 	= "selected_addr";

	private final static Preferences 	prefs 	= Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	@FXML private ComboBox<Integer> addressComboBox;

	@FXML private void initialize() {

		final TextField editor = addressComboBox.getEditor();
		editor.focusedProperty().addListener((o,ald, focous)->Optional.of(focous).filter(f->f).ifPresent(f->editor.selectAll()));

		setupComboBox();
		fillComboBox();
		setSelectedAddress();
	}

	@FXML private void onActionAddressComboBox() {
		logger.traceEntry();

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
		logger.traceEntry();

		addressComboBox.setUserData(this);
		addressComboBox.setConverter(new AddressIntegerStringConverter());
		new AddressListener(addressComboBox.getEditor().textProperty());
	}

	private void fillComboBox() {
		logger.traceEntry();

		final byte[] byteArray = prefs.getByteArray(KEY_ADDRESSES, new byte[]{(byte)254});
		final ObservableList<Integer> items = addressComboBox.getItems();
		final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

		IntStream
		.generate(bais::read)
		.limit(bais.available())
		.forEachOrdered(i->items.add(i));
	}

	public void savePreference() {
		logger.traceEntry();

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
		final Integer addr = prefs.getInt(KEY_SELECTED_ADDR, defaultAddress);
		logger.entry(addr);

		if(addr!=defaultAddress){
			addressComboBox.getSelectionModel().select(addr);
			onActionAddressComboBox();
		}
	}
}
