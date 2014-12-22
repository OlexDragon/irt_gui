package irt.irt_gui;

import irt.controller.AlarmsController;
import irt.controller.DumpControllers;
import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.value.seter.Setter;
import irt.controller.translation.Translation;
import irt.data.Listeners;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.KeyValue;
import irt.tools.panel.head.HeadPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.head.UnitsContainer;
import irt.tools.panel.subpanel.progressBar.ProgressBar;
import irt.tools.panel.wizards.address.AddressWizard;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class IrtGui extends IrtMainFrame {
	private static final long serialVersionUID = 1611718189640547787L;

	public static final int DEFAULT_ADDRESS = 254;
	private static LoggerContext ctx = DumpControllers.setSysSerialNumber(null);//need for log file name setting
	private static final Logger logger = (Logger) LogManager.getLogger();

	public static final String VERTION = "- 3.077";
	private static final Preferences prefs = GuiController.getPrefs();
	private static final AddressWizard ADDRESS_VIZARD = AddressWizard.getInstance();
	private int address;
	private Set<Integer> addressHistory =  new TreeSet<>();
	private boolean connected;

	protected HeadPanel headPanel;
	private JTextField txtAddress;
	private JMenuItem mntmSet;
	private JMenuItem mntmClear;
	private JMenuItem mntmRemove;
	private JMenuItem mntmAddressWizard;

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

		setAddressHistory(prefs.get("address_history", null));

		address = prefs.getInt("address", DEFAULT_ADDRESS);
		Set<Byte> addresses = GuiControllerAbstract.getAddresses(null);
		if(!addresses.contains(addresses)){
			address = addresses.iterator().next()&0xFF;
			prefs.putInt("address", address);
		}
		txtAddress = new JTextField();
		txtAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtAddress.setFont(new Font("Tahoma", Font.BOLD, 18));
		txtAddress.setText(""+address);
		txtAddress.setColumns(8);
		txtAddress.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent keyEvent) {

				try {
					Object[] values = addressHistory.toArray();
					int key = getKey(values);

					int length = values.length-1;

					switch (keyEvent.getExtendedKeyCode()) {
					case KeyEvent.VK_UP:
						if (key < length)
							txtAddress.setText(values[++key].toString());
						else
							txtAddress.setText(values[key = 0].toString());
						break;
					case KeyEvent.VK_DOWN:
						if (key > 0)
							txtAddress.setText(values[--key].toString());
						else
							txtAddress.setText(values[key = length].toString());
					}

					logger.trace("Key={}", key);
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}

			private int getKey(Object[] values) {
				String text = txtAddress.getText();
				int result = 0;

				for(int i=0; i<values.length; i++)
					if(values[i].toString().equals(text)){
						result = i;
						break;
					}

				return result;
			}
		});
		if(addressHistory.isEmpty())
			txtAddress.setToolTipText("Unit Address");
		else
			txtAddress.setToolTipText(addressHistory.toString());

		JPopupMenu popupMenu_1 = new JPopupMenu();
		addPopup(txtAddress, popupMenu_1);
		
		mntmClear = new JMenuItem(Translation.getValue(String.class, "clear", "Clear"));
		mntmClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAddress.setText(""+DEFAULT_ADDRESS);
				prefs.remove("address_history");
				prefs.putInt("address", DEFAULT_ADDRESS);
			}
		});
		popupMenu_1.add(mntmClear);
		
		mntmRemove = new JMenuItem(Translation.getValue(String.class, "remove", "Remove"));
		mntmRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = txtAddress.getText();
				if(text!=null && !(text = text.replaceAll("\\D", "")).isEmpty()){
					addressHistory.remove(Integer.parseInt(text));
					prefs.put("address_history", addressHistory.toString());
					if(addressHistory.isEmpty())
						txtAddress.setText(""+DEFAULT_ADDRESS);
					else
						txtAddress.setText(""+addressHistory.iterator().next());

					txtAddress.setToolTipText(addressHistory.toString());
				}
			}
		});
		popupMenu_1.add(mntmRemove);
		
		mntmSet = new JMenuItem(Translation.getValue(String.class, "set", "Set"));
		mntmSet.setVisible(false);
		mntmSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String newAddressStr = txtAddress.getText();
				logger.trace("Address = {}", newAddressStr);

				if(newAddressStr!=null && !(newAddressStr=newAddressStr.replaceAll("\\D", "")).isEmpty()){
					int newAddress = Integer.parseInt(newAddressStr);

					if(newAddress>0 && newAddress<=AddressWizard.MAX_ADDRESS){
						if(newAddress!=address){
							if(JOptionPane.showConfirmDialog(
									IrtGui.this,
									"Are you really want to change the address '"+address+"' to '"+newAddress+"'.","Address Change",
									JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){

								byte na = (byte)newAddress;
								Setter packetWork = new Setter(new LinkHeader((byte)address, (byte)0, (short) 0),
										Packet.IRT_SLCP_PACKET_TYPE_COMMAND,
										Packet.IRT_SLCP_PACKET_ID_PROTOCOL,
										Packet.IRT_SLCP_PARAMETER_PROTOCOL_ADDRESS,
										PacketWork.PACKET_ID_PROTOCOL_ADDRESS, na);
								logger.trace(packetWork);
								guiController.setAddress(na);
								GuiController.getComPortThreadQueue().add(packetWork);
								prefs.putInt("address", address);
							}else
								txtAddress.setText(""+address);
						}else
							JOptionPane.showMessageDialog(IrtGui.this, "First type the new address");
					}else
						JOptionPane.showMessageDialog(IrtGui.this, "The address of unit should be between 0 and "+(AddressWizard.MAX_ADDRESS+1));
				}else
					JOptionPane.showMessageDialog(IrtGui.this, "The address of unit should be between 0 and "+(AddressWizard.MAX_ADDRESS+1));
			}
		});
		popupMenu_1.add(mntmSet);
		txtAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = txtAddress.getText();
				logger.trace("Address={}", text);

				if(text==null || (text=text.replaceAll("\\D", "")).isEmpty() || (address=Integer.parseInt(text))<1 || address>AddressWizard.MAX_ADDRESS)
					JOptionPane.showMessageDialog(IrtGui.this, "The address of unit should be between 0 and "+(AddressWizard.MAX_ADDRESS+1));
				else{
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							try{
								logger.debug("Set Address to {}", address);
								if(addressHistory.add(address)){
									prefs.put("address_history", addressHistory.toString());
									logger.debug("address history = {}", addressHistory);
									txtAddress.setToolTipText(addressHistory.toString());
								}

								prefs.putInt("address", address);
								txtAddress.setText(""+address);
								guiController.setAddress((byte) address);
								logger.debug("Address is set to {}", address);
							}catch(Exception ex){
								logger.catching(ex);
							}
							return null;
						}
					}.execute();
				}
			}
		});
		
		ProgressBar progressBar = new ProgressBar();
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(progressBar, popupMenu);
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
				final JPopupMenu m = (JPopupMenu) popupMenuEvent.getSource();

				EventQueue.invokeLater(new Runnable() {
					
					@Override 
		            public void run() {
		            	 Component[] components = m.getComponents();
						for (Component c : components)
							if (c instanceof JComponent) { // BasicComboPopup
								c.repaint();
							}
					}
				});
			}
			
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
			@Override public void popupMenuCanceled(PopupMenuEvent e) { }
		});

		mntmAddressWizard = new JMenuItem(Translation.getValue(String.class, "address_wizard", "Address Wizard..."));
		mntmAddressWizard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				JOptionPane.showMessageDialog(IrtGui.this, "Coming soon.");
				ADDRESS_VIZARD.setVisible(true);
			}
		});
		popupMenu.add(mntmAddressWizard);
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

		ADDRESS_VIZARD.setOwner(this);

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

	private void setAddressHistory(String historyStr) {
		if(historyStr!=null){
			for(String s:historyStr.split(","))
				addressHistory.add(Integer.parseInt(s.replaceAll("\\D", "")));

			logger.debug("History={}", addressHistory);
		}
	}

	protected void setHeaderLabel(HeadPanel headPanel) throws IOException, FontFormatException {
		final JLabel lblIrtTechnologies = new JLabel(IrtPanel.PROPERTIES.getProperty("company_name"));
		lblIrtTechnologies.setForeground(Color.WHITE);
		lblIrtTechnologies.setBounds(531, 10, 107, 14);
		headPanel.add(lblIrtTechnologies);
		new SwingWorker<Font, Void>() {
			@Override
			protected Font doInBackground() throws Exception {
				try {
					return new Font(IrtPanel.PROPERTIES.getProperty("font_name"), IrtPanel.parseFontStyle(IrtPanel.PROPERTIES
							.getProperty("font_style")), 12);
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
		comboBoxLanguage.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){
					new SwingWorker<Void, Void>() {

						@Override
						protected Void doInBackground() throws Exception {
							mntmClear.setText(Translation.getValue(String.class, "clear", "Clear"));
							mntmAddressWizard.setText(Translation.getValue(String.class, "address_wizard", "Address Wizard..."));
							mntmRemove.setText(Translation.getValue(String.class, "remove", "Remove"));
							mntmSet.setText(Translation.getValue(String.class, "set", "Set"));
							ADDRESS_VIZARD.refresh();
							return null;
						}
						
					}.execute();
				}
			}
		});

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
				} catch (InterruptedException | ExecutionException e) {
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
					comboBoxLanguage.setBounds(get());
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
		mntmSet.setVisible(connected);
	}
}
