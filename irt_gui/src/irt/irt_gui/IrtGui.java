package irt.irt_gui;
import irt.controller.GuiController;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.UnitsContainer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class IrtGui extends IrtMainFrame {

	public static final String VERTION = "- 3.028";
	private GuiController guiController;
	protected HeadPanel headPanel;

	public IrtGui() {
		super(700, 571, 590);

		headPanel = new HeadPanel(this);
		headPanel.setSize(650, 74);
		headPanel.setLocation(0, 51);
		headPanel.setVisible(true);
		getContentPane().add(headPanel);

		setHeaderLabel(headPanel);

		JLabel lblGui = new JLabel("GUI "+VERTION);
		lblGui.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblGui.setForeground(Color.WHITE);
		lblGui.setBounds(531, 37, 107, 14);
		headPanel.add(lblGui);

		UnitsContainer unitsPanel = new UnitsContainer();
		unitsPanel.setBounds(0, 127, getWidth(), 444);
		unitsPanel.addStatusListener(headPanel.getStatusChangeListener());
		getContentPane().add(unitsPanel);
	}

	protected void setHeaderLabel(HeadPanel headPanel) {
		JLabel lblIrtTechnologies = new JLabel("IRT Technologies");
		lblIrtTechnologies.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIrtTechnologies.setForeground(Color.WHITE);
		lblIrtTechnologies.setBounds(531, 19, 107, 14);
		headPanel.add(lblIrtTechnologies);
	}

	public static void main(String[] args) {
	       // Determine what the GraphicsDevice can support.
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice gd = ge.getDefaultScreenDevice();
 //       final boolean isTranslucencySupported = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);

        //If shaped windows aren't supported, exit.
 //       if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
 //           System.err.println("Shaped windows are not supported");
 //           System.exit(0);
 //       }

        //If translucent windows aren't supported, 
        //create an opaque window.
//        if (!isTranslucencySupported) {
//            System.out.println("Translucency is not supported, creating an opaque window");
 //       }
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IrtGui frame = new IrtGui();

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
		guiController = new GuiController("Gui Controller", this);
		guiController.addChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				headPanel.setPowerOn((Boolean)valueChangeEvent.getSource());
			}
		});
		return guiController;
	}

	@Override
	protected Rectangle comboBoxBounds() {
		return new Rectangle(279, 11, 186, 28);
	}
}
