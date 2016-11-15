
package irt.gui.data;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class GuiUtilityTest {
	Logger logger = LogManager.getLogger();

	@Test
	public void getResourcePath1() {
		logger.trace(GuiUtility.getResourcePath(null));
	}

	@Test
	public void getResourcePath2() throws MalformedURLException {
		final URL url = new File("C:\\Users\\Oleksandr\\workspace\\irt_gui_5\\target\\classes\\fxml").toURI().toURL();
		logger.trace("\n\n{}\n{}", url, GuiUtility.getResourcePath(url));
	}

}
