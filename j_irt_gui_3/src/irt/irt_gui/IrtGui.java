package irt.irt_gui;
import irt.controller.DumpControllers;
import irt.controller.GuiController;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.tools.KeyValue;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.progressBar.ProgressBar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

@SuppressWarnings("serial")
public class IrtGui extends IrtMainFrame {

	private static LoggerContext ctx = DumpControllers.setSysSerialNumber(null);//need for file name setting
	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final String VERTION = "- 3.050";
	private GuiController guiController;
	protected HeadPanel headPanel;

	public IrtGui() {
		super(700, 571);
		DumpControllers.setSysSerialNumber(null);
		logger.trace(ctx);

		headPanel = new HeadPanel(this);
		headPanel.setSize(650, 74);
		headPanel.setLocation(0, 51);
		headPanel.setVisible(true);
		getContentPane().add(headPanel);

		try {
			setHeaderLabel(headPanel);
		} catch (Exception e) {
			logger.catching(e);
		}

		JLabel lblGui = new JLabel("GUI "+VERTION);
		lblGui.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblGui.setForeground(Color.WHITE);
		lblGui.setBounds(531, 29, 107, 14);
		headPanel.add(lblGui);

		UnitsContainer unitsPanel = new UnitsContainer();
		unitsPanel.setBounds(0, 127, getWidth(), 444);
		unitsPanel.addStatusListener(headPanel.getStatusChangeListener());
		getContentPane().add(unitsPanel);
		
		ProgressBar progressBar = new ProgressBar();
		progressBar.setBounds(540, 0, 110, 50);
		getContentPane().add(progressBar);
	}

	protected void setHeaderLabel(HeadPanel headPanel) throws IOException, FontFormatException {
		final JLabel lblIrtTechnologies = new JLabel(IrtPanel.properties.getProperty("company_name_"+IrtPanel.companyIndex));
		lblIrtTechnologies.setForeground(Color.WHITE);
		lblIrtTechnologies.setBounds(531, 10, 107, 14);
		headPanel.add(lblIrtTechnologies);
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				Thread.currentThread().setName("irtGui.lblIrtTechnologies.setFont");
				try {
					return new Font(IrtPanel.properties.getProperty("font_name_" + IrtPanel.companyIndex), IrtPanel.parseFontStyle(IrtPanel.properties
							.getProperty("font_style_" + IrtPanel.companyIndex)), 12);
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					lblIrtTechnologies.setFont(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();

//Language ComboBox

		final JComboBox<KeyValue<String, String>> comboBoxLanguage = new JComboBox<>();
		comboBoxLanguage.setName("Language");
		headPanel.add(comboBoxLanguage);

		new SwingWorker<DefaultComboBoxModel<KeyValue<String, String>>, Void>() {
			@SuppressWarnings("unchecked")
			@Override
			protected DefaultComboBoxModel<KeyValue<String, String>> doInBackground() throws Exception {
				Thread.currentThread().setName("irtGui.comboBoxLanguage.setModel");

				try {
					String[] languagesArr = Translation.getTranslationProperties("languages").split(",");
					logger.entry((Object[]) languagesArr);

					KeyValue<?, ?>[] languages = new KeyValue[languagesArr.length];
					for (int i = 0; i < languagesArr.length; i++) {
						String[] split = languagesArr[i].split(":");
						languages[i] = new KeyValue<String, String>(split[0], split[1]);
					}
					return logger.exit(new DefaultComboBoxModel<KeyValue<String, String>>((KeyValue<String, String>[]) languages));
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					comboBoxLanguage.setModel(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
				KeyValue<String, String> keyValue = new KeyValue<>(GuiController.getPrefs().get("locale", "en_US"), null);
				comboBoxLanguage.setSelectedItem(keyValue);
			}
		}.execute();


		comboBoxLanguage.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxLanguage.setUI(new BasicComboBoxUI(){ @Override protected JButton createArrowButton() { return new JButton(){ @Override public int getWidth() { return 0;}};}});
		comboBoxLanguage.setForeground(Color.WHITE);
		comboBoxLanguage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboBoxLanguage.setBackground(HeadPanel.BACKGROUND_COLOR.darker().darker());

		new SwingWorker<Rectangle, Void>() {
			@Override
			protected Rectangle doInBackground() throws Exception {
				try{
				Thread.currentThread().setName("irtGui.comboBoxLanguage.setBounds");
				String[] bounds = Translation.getTranslationProperties("headPanel_comboBoc_bounds").toString().split(",");
				return new Rectangle(Integer.parseInt(bounds[0]),
						Integer.parseInt(bounds[1]),
						Integer.parseInt(bounds[2]),
						Integer.parseInt(bounds[3]));
				}catch(Exception e){
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					comboBoxLanguage.setBounds(get());
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();
		comboBoxLanguage.setMinimumSize(new Dimension(77, 17));

		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				Thread.currentThread().setName("irtGui.comboBoxLanguage.setFont");
				try {
					float fontSize = Translation.getValue(Float.class, "headPanel.language.comboBox.font.size", 12f);
					String fontURL = "fonts/MINGLIU.TTF";
					Font f = Translation.getSystemFont(fontURL, Font.BOLD, (int) fontSize);
					if (f == null) {
						URL resource = getClass().getResource(fontURL);// Chinese
						if (resource != null)
							f = Font.createFont(Font.TRUETYPE_FONT, resource.openStream()).deriveFont(fontSize).deriveFont(Font.BOLD);
						else
							logger.warn("Can not get the resouce font 'MINGLIU.TTF'");
					}
					return f;
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					Font f = get();
					if(f!=null)
						comboBoxLanguage.setFont(f);
				} catch (InterruptedException | ExecutionException e) {
					logger.catching(e);
				}
			}
		}.execute();
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
					logger.catching(e);
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

	@Override
	protected Point getClosePanelPosition() {
		return new Point(660, 0);
	}
}
