
package irt.serial.port.fx;

import static org.junit.Assert.assertEquals;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.serial.port.fx.ComboBoxUnitAddressFx;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class ComboBoxUnitAddressTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private final static Preferences 	prefs 	= Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private ComboBoxUnitAddressFx testNode;

	private int value;

	@Override
	public void start(Stage stage) throws Exception {
		setup();

			
        	testNode = new ComboBoxUnitAddressFx();
        	testNode.addObserver(this);
 
     		Scene scene = new Scene(testNode);
    		stage.setScene(scene);
            stage.show();

            /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
            stage.toFront(); 
	}

	public void setup(){
		Random r = new Random();
		final byte[] bytes = new byte[5];
		r.nextBytes(bytes);

		prefs.putByteArray(ComboBoxUnitAddressFx.KEY_ADDRESSES, new byte[]{});

		final int nextInt = r.nextInt(bytes.length);
		value = bytes[nextInt] & 0xFF;
		prefs.putInt(ComboBoxUnitAddressFx.KEY_SELECTED_ADDR, value);
	}

	private FutureTask<Void> ft = new FutureTask<>(()->null);
	@Test
	public void test() throws Exception {
		assertEquals(value, prefs.getInt(ComboBoxUnitAddressFx.KEY_SELECTED_ADDR, -1));

		//wait for initialize
		ft.get(1, TimeUnit.SECONDS);

		final TextField editor = testNode.getEditor();

		String text = editor.getText();
		assertEquals(Integer.toString(value), text);

		//no numeric input, letters should not be entered
		final FxRobot clickOn = clickOn(testNode);

		editor.selectAll();
		clickOn.type(KeyCode.L, 5);

		assertEquals(text, editor.getText());

		//digits input
		ft = new FutureTask<>(()->null);
		editor.selectAll();
		clickOn.type(KeyCode.DIGIT1, 5).type(KeyCode.ENTER);
		//wait for action
		ft.get(1, TimeUnit.SECONDS);

		assertEquals("11111", editor.getText());
	}

	@After
	public void after(){
		prefs.remove(ComboBoxUnitAddressFx.KEY_ADDRESSES);
		prefs.remove(ComboBoxUnitAddressFx.KEY_SELECTED_ADDR);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		Platform.runLater(ft);
	}
}
