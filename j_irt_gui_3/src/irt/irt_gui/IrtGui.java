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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import irt.controller.DumpControllerFull;
import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.tools.KeyValue;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.BaudRateSelectorFx;
import irt.tools.fx.JavaFxFrame;
import irt.tools.fx.JavaFxPanel;
import irt.tools.fx.MonitorPanelFx;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.progressBar.ProgressBar;
import irt.tools.textField.UnitAddressField;

public class IrtGui extends IrtMainFrame {
	private static final long serialVersionUID = 1611718189640547787L;

	private static final String PREF_KEY_ADDRESS = "address";
	public static final int DEFAULT_ADDRESS = 254;
	private static final LoggerContext ctx = DumpControllerFull.setSysSerialNumber(null);//need for log file name setting
	private static final Logger logger = LogManager.getLogger();

	public static final String VERTION = "- 3.182";

	protected HeadPanel headPanel;
	private JTextField txtAddress;

	public IrtGui() {
		super(700, 571);
		setMinimumSize(new Dimension(700, 572));
		DumpControllerFull.setSysSerialNumber(null);
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
//			private boolean isShowing;

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.getID() == KeyEvent.KEY_PRESSED){
					if(!isPressed && e.isControlDown() && e.isAltDown() && e.getKeyCode()==KeyEvent.VK_D){
						isPressed = true;
//						guiController.showDebugPanel(isShowing = !isShowing);
					}
				}else
					isPressed = false;
				return false;
			}
		});
	}

	@SuppressWarnings("unchecked")
	protected void setHeaderLabel(HeadPanel headPanel) throws IOException, FontFormatException {
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(headPanel, popupMenu);

		JMenuItem monitortMenuItem = new JMenuItem(Translation.getValue(String.class, "monitor", "Monitor"));
		monitortMenuItem.addActionListener(new ActionListener() {
			private JavaFxFrame javaFxFrame;

			public void actionPerformed(ActionEvent e) {

				if(javaFxFrame!=null){
					javaFxFrame.setVisible(true);
					return;
				}

				MonitorPanelFx monitorPanel = new MonitorPanelFx();
				final JavaFxFrame javaFxFrame = new JavaFxFrame(monitorPanel, new JMenu("Menu"));
				javaFxFrame.setSize(200, 200);

				JMenu menu = javaFxFrame.getMenu();
				menu.setText("Unit Address");
				fillMenu(monitorPanel, menu);
			}
		});
		popupMenu.add(monitortMenuItem);

		JMenuItem baudrateMenuItem = new JMenuItem(Translation.getValue(String.class, "baudrates", "Baud Rates"));
		baudrateMenuItem.addActionListener(new ActionListener() {
			private JavaFxFrame baudRateFrame;

			public void actionPerformed(ActionEvent e) {

				if(baudRateFrame==null){
					baudRateFrame = new JavaFxFrame(new BaudRateSelectorFx(), new JMenu("Menu"));
					baudRateFrame.setSize(200, 200);
				}else
					baudRateFrame.setVisible(true);
			}
		});

		JMenuItem mntmAlarms = new JMenuItem(Translation.getValue(String.class, "alarms", "AlarmsPacketIds"));
		mntmAlarms.addActionListener(new ActionListener() {

			private JavaFxFrame alarmsFrame;
			private AlarmPanelFx alarmPanelFx;

			public void actionPerformed(ActionEvent e) {

				if(alarmsFrame==null){
					alarmPanelFx = new AlarmPanelFx();
					alarmsFrame = new JavaFxFrame(alarmPanelFx, new JMenu("Menu"));
					alarmsFrame.setSize(200, 200);
					fillMenu(alarmPanelFx, alarmsFrame.getMenu());
				}else
					alarmsFrame.setVisible(true);
			}
		});
		popupMenu.add(mntmAlarms);
		popupMenu.add(baudrateMenuItem);

		final JLabel lblIrtTechnologies = new JLabel(IrtPanel.PROPERTIES.getProperty("company_name"));
		lblIrtTechnologies.setForeground(Color.WHITE);
		lblIrtTechnologies.setBounds(531, 10, 107, 14);
		headPanel.add(lblIrtTechnologies);

		Font font = new Font(IrtPanel.PROPERTIES.getProperty("font_name"), IrtPanel.parseFontStyle(IrtPanel.PROPERTIES.getProperty("font_style")), 12);
		SwingUtilities.invokeLater(()->lblIrtTechnologies.setFont(font));

//Language ComboBox

		final JComboBox<KeyValue<String, String>> comboBoxLanguage = new JComboBox<>();
		comboBoxLanguage.setName("Language");
		headPanel.add(comboBoxLanguage);

		comboBoxLanguage.addActionListener(
				e->SwingUtilities.invokeLater(
						()->{
							baudrateMenuItem.setText(Translation.getValue(String.class, "baudrates", "Baud Rates"));
							monitortMenuItem.setText(Translation.getValue(String.class, "monitor", "Monitor"));
						}));

		SwingUtilities.invokeLater(()->{

			Optional<Stream<String>> o = Optional
										.ofNullable(Translation.getTranslationProperties("languages"))
										.map(s->s.split(","))
										.map(a->Arrays.stream(a));

			DefaultComboBoxModel<KeyValue<String, String>> defaultComboBoxModel;
			if(o.isPresent()){
				final KeyValue<?, ?>[] languages = o.get()
														.map(s->s.split(":"))
														.filter(arr->arr.length>1)
														.map(arr->new KeyValue<String, String>(arr[0], arr[1]))
														.toArray(size->new KeyValue<?, ?>[size]);

				defaultComboBoxModel = new DefaultComboBoxModel<KeyValue<String, String>>((KeyValue<String, String>[]) languages);
			}else
				defaultComboBoxModel = new DefaultComboBoxModel<KeyValue<String, String>>();

			comboBoxLanguage.setModel(defaultComboBoxModel);

			final String key = Optional.ofNullable(GuiController.getPrefs().get("locale", "en_US")).orElse("en_US");
			KeyValue<String, String> keyValue = new KeyValue<>(key, null);
			comboBoxLanguage.setSelectedItem(keyValue);
		});

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
		return new GuiController("Gui UnitController", this);
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

	private void fillMenu(JavaFxPanel panel, final JMenu menu) {
		Optional.ofNullable(GuiController.getAddresses()).ifPresent(addrs->{
			IntStream
			.range(0, addrs.length)
			.map(i->(addrs[i] & 0xFF))
			.forEach(addr->{
				final JMenuItem mItem = new JMenuItem("#" + Integer.toString(addr));
				mItem.setName(Integer.toString(addr));
				mItem.addActionListener(al->{
					final JMenuItem menuItem = (JMenuItem)al.getSource();

					final String name = menuItem.getName();
					panel.setUnitAddress((byte) Integer.parseInt(name));
					menu.setText(name);
				});
				menu.add(mItem);
			});
			//select first menu item
			if(menu.getItemCount()>0){
				final String name = menu.getItem(0).getName();
				panel.setUnitAddress((byte) Integer.parseInt(name));
				menu.setText(name);
			}
		});
	}
}
