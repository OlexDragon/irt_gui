package irt.tools.panel;

import irt.contriller.UserGuiController;
import irt.irt_gui.IrtGui;

import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class UserIrtGui extends IrtGui {
	public UserIrtGui() {
	}

	public static void main(String[] args) {
	       // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
       final boolean isTranslucencySupported = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);

        //If shaped windows aren't supported, exit.
       if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
           JOptionPane.showMessageDialog(null, "Shaped windows are not supported");
            System.exit(0);
       }

        //If translucent windows aren't supported, 
        //create an opaque window.
        if (!isTranslucencySupported) {
        	JOptionPane.showMessageDialog(null, "Translucency is not supported");
       }
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserIrtGui frame = new UserIrtGui();

	                // Set the window translucency, if supported.
//	                if (isTranslucencySupported) {
//	                	frame.setOpacity(0);
//	                }

	                frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	@Override
	protected Thread getNewGuiController() {
		return new UserGuiController(this);
	}
}
