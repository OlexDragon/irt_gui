package irt.irt_gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.AlarmsController;
import irt.controller.DumpControllers;
import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.tools.KeyValue;
import irt.tools.fx.BaudRateSelector;
import irt.tools.fx.JavaFxFrame;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.progressBar.ProgressBar;
import irt.tools.textField.UnitAddressField;

public class IrtGui extends IrtMainFrame {
	private static final String PREF_KEY_ADDRESS = "address";

	private static final long serialVersionUID = 1611718189640547787L;

	public static final int DEFAULT_ADDRESS = 254;
	private static LoggerContext ctx = DumpControllers.setSysSerialNumber(null);//need for log file name setting
	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final String VERTION = "- 3.111";
	private boolean connected;

	protected HeadPanel headPanel;
	private JTextField txtAddress;

	protected ValueChangeListener valueChangeListener = new ValueChangeListener() {
		@Override
		public void valueChanged(ValueChangeEvent valueChangeEvent) {

			Object source = valueChangeEvent.getSource();

			switch(valueChangeEvent.getID()){

			case GuiController.ALARM:
				switch((int)source){
				case AlarmsController.ALARMS_STATUS_INFO:
				case AlarmsController.ALARMS_STATUS_NO_ALARM:
					headPanel.setAlarm(false);
					break;
				case AlarmsController.ALARMS_STATUS_WARNING:
				case AlarmsController.ALARMS_STATUS_MINOR:
					headPanel.setAlarm(true);
					headPanel.setAlarmColor(AlarmsController.WARNING_COLOR);
					break;
				case AlarmsController.ALARMS_STATUS_ALARM:
				case AlarmsController.ALARMS_STATUS_FAULT:
					headPanel.setAlarm(true);
					headPanel.setAlarmColor(Color.RED);
					break;
				}
				break;

			case GuiController.CONNECTION:
				headPanel.setPowerOn((boolean)source);
				ProgressBar.setValue(0);
				break;

			case GuiController.MUTE:
				headPanel.setMute((boolean) source);
			}
		}
	};

	public IrtGui() {
		super(700, 571);
		setMinimumSize(new Dimension(700, 571));
		DumpControllers.setSysSerialNumber(null);
		logger.trace(ctx);

		UIManager.put("ToolTip.background", Color.WHITE);

		headPanel = new HeadPanel(this);
		headPanel.setVisible(true);

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

		final UnitsContainer unitsPanel = new UnitsContainer();
		unitsPanel.setBorder(null);
		unitsPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Point location = unitsPanel.getLocation();
				Dimension size = unitsPanel.getSize();
				setSize(size.width, location.y+size.height);
			}
		});

		txtAddress = new UnitAddressField(PREF_KEY_ADDRESS);
		
		ProgressBar progressBar = new ProgressBar();
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(512)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(txtAddress, 48, 48, 48)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(28)
							.addComponent(progressBar, 110, 110, 110))))
				.addComponent(headPanel, 650, 650, 650)
				.addComponent(unitsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(11)
							.addComponent(txtAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(progressBar, 50, 50, 50))
					.addGap(1)
					.addComponent(headPanel, 74, 74, 74)
					.addGap(2)
					.addComponent(unitsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(0, 441, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			
			private boolean isPressed;
			private boolean isShowing;

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.getID() == KeyEvent.KEY_PRESSED){
					if(!isPressed && e.isControlDown() && e.isAltDown() && e.getKeyCode()==KeyEvent.VK_D){
						isPressed = true;
						guiController.showDebugPanel(isShowing = !isShowing);
					}
				}else
					isPressed = false;
				return false;
			}
		});
	}

	protected void setHeaderLabel(HeadPanel headPanel) throws IOException, FontFormatException {
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(headPanel, popupMenu);

		JMenuItem baudrateMenuItem = new JMenuItem(Translation.getValue(String.class, "baudrates", "Baud Rates"));
		baudrateMenuItem.addActionListener(new ActionListener() {
			private JavaFxFrame javaFxFrame;

			public void actionPerformed(ActionEvent e) {

				if(javaFxFrame==null){
					javaFxFrame = new JavaFxFrame(()->new BaudRateSelector());
					javaFxFrame.setSize(200, 200);
				}else
					javaFxFrame.setVisible(true);
			}
		});
		popupMenu.add(baudrateMenuItem);

		final JLabel lblIrtTechnologies = new JLabel(IrtPanel.PROPERTIES.getProperty("company_name"));
		lblIrtTechnologies.setForeground(Color.WHITE);
		lblIrtTechnologies.setBounds(531, 10, 107, 14);
		headPanel.add(lblIrtTechnologies);
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				try {
					return new Font(IrtPanel.PROPERTIES.getProperty("font_name"), IrtPanel.parseFontStyle(IrtPanel.PROPERTIES.getProperty("font_style")), 12);
				} catch (Exception e) {
					logger.catching(e);
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					lblIrtTechnologies.setFont(get());
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();

//Language ComboBox

		final JComboBox<KeyValue<String, String>> comboBoxLanguage = new JComboBox<>();
		comboBoxLanguage.setName("Language");
		headPanel.add(comboBoxLanguage);
		comboBoxLanguage.addActionListener(e->new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				baudrateMenuItem.setText(Translation.getValue(String.class, "baudrates", "Baud Rates"));
				return null;
			}
			
		}.execute());

		new SwingWorker<DefaultComboBoxModel<KeyValue<String, String>>, Void>() {
			@SuppressWarnings("unchecked")
			@Override
			protected DefaultComboBoxModel<KeyValue<String, String>> doInBackground() throws Exception {

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
				} catch (Exception e) {
					logger.catching(e);
				}
				KeyValue<String, String> keyValue = new KeyValue<>(GuiController.getPrefs().get("locale", "en_US"), null);
				comboBoxLanguage.setSelectedItem(keyValue);
			}
		}.execute();

		comboBoxLanguage.addPopupMenuListener(Listeners.popupMenuListener);
		comboBoxLanguage.setUI(
				new BasicComboBoxUI(){
					@Override
					protected JButton createArrowButton() {
						return new JButton(){
							private static final long serialVersionUID = 2896280803201789897L;

							@Override public int getWidth() {
								return 0;
							}
						};
					}
				}
		);
		comboBoxLanguage.setForeground(Color.WHITE);
		comboBoxLanguage.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboBoxLanguage.setBackground(HeadPanel.BACKGROUND_COLOR.darker().darker());

		new SwingWorker<Rectangle, Void>() {
			@Override
			protected Rectangle doInBackground(){
				try{

					String translationProperties = Translation.getTranslationProperties("headPanel_comboBoc_bounds");

					if(translationProperties==null)
						return null;

					String[] bounds = translationProperties.toString().split(",");
					return new Rectangle(Integer.parseInt(bounds[0]),
							Integer.parseInt(bounds[1]),
							Integer.parseInt(bounds[2]),
							Integer.parseInt(bounds[3]));
				}catch(Exception e){
					logger.catching(e);
					return new Rectangle(530,50,91,17);
				}
			}

			@Override
			protected void done() {
				try {
					Rectangle get = get();
					if(get!=null)
						comboBoxLanguage.setBounds(get);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		}.execute();
		comboBoxLanguage.setMinimumSize(new Dimension(77, 17));

		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {

				try {
					float fontSize = Translation.getValue(Float.class, "headPanel.language.comboBox.font.size", 12f);
					String fontURL = "fonts/MINGLIU.TTF";
					Font f = Translation.getSystemFont(fontURL, Font.BOLD, (int) fontSize);
					if (f == null) {
						URL resource = getClass().getResource(fontURL);// Chinese
						if (resource != null) {
							try( InputStream openStream = resource.openStream();){
								f = Font.createFont(Font.TRUETYPE_FONT, openStream).deriveFont(fontSize).deriveFont(Font.BOLD);
							}
						} else
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
				} catch (Exception e) {
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
	protected GuiControllerAbstract getNewGuiController() {
		GuiController guiController = new GuiController("Gui Controller", this);
		guiController.addChangeListener(valueChangeListener);
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

	public static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
