
package irt.fx.control.generator;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.fx.control.prologix.PrologixFx;
import irt.serial.port.enums.SerialPortStatus;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignalGeneratorFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();

	private SignalGeneratorFx testNode;

	private PrologixFx prologixFx;

	@Override
	public void start(Stage stage) throws Exception {

		logger.debug("\n\n****************************** Start Test ***************************************");

		try{

			prologixFx = new PrologixFx();
			testNode = new SignalGeneratorFx();
			testNode.setPrologix(prologixFx);

			final VBox vbox = new VBox();
			vbox.getChildren().addAll(prologixFx, testNode);

			Scene scene = new Scene(vbox);
			stage.setScene(scene);
			stage.show();

			/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
			stage.toFront(); 

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Test
	public void test() {
		if(prologixFx.getSerialPortStatus()!=SerialPortStatus.OPEND){
			logger.error("Serial port is not open");
			return;
		}
		sleep(1000);
	}

	@Override
	public void stop() throws Exception {
		prologixFx.claseSerialPort();
	}
}
