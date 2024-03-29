package irt.irt_gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.data.Listeners;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketID;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.module.ModuleSelectFxPanel;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.wizards.serial_port.SerialPortWizard;
import javafx.embed.swing.JFXPanel;

@SuppressWarnings("serial")
public abstract class IrtMainFrame extends JFrame implements PacketListener {

	protected static Preferences prefs = Preferences.userRoot().node(GuiControllerAbstract.IRT_TECHNOLOGIES_INC);

	static{
		System.setProperty("serialNumber", "UnknownSerialNumber");
	}

	private final static Logger logger = LogManager.getLogger();

	protected GuiControllerAbstract guiController;

	private Timer timer;
	private static IrtMainFrame mainFrame; public static IrtMainFrame getMainFrame() { return mainFrame; }

	private static ModuleSelectFxPanel moduleSelectFxPanel;
	public static boolean isRedundancyController(){
		return Optional.ofNullable(moduleSelectFxPanel).map(ModuleSelectFxPanel::countButtons).map(c->c>1).orElse(false);
	}

	public IrtMainFrame(int width, int hight) {
		super(IrtPanel.PROPERTIES.getProperty("company_name"));
		ThreadWorker.runThread(()->new JFXPanel(), "Prepare JavaFX toolkit and environment"); // this will prepare JavaFX toolkit and environment
		mainFrame = this;

		Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.catching(e));


		Thread t = new ThreadWorker("IrtMainFrame.addShutdownHook()")

				.newThread(
						()->{
							Optional.ofNullable(guiController).ifPresent(GuiControllerAbstract::stop);
							LogManager.shutdown();
							prefs.putBoolean(ComPortThreadQueue.GUI_CLOSED_CORRECTLY, true);
						});
		Runtime.getRuntime().addShutdownHook(t);

		setSize(width, hight);

		if(IrtPanel.logoIcon!=null)
			setIconImage(IrtPanel.logoIcon.getImage());
 
        Color transparent = new Color(0,true);

        setUndecorated(true);
		setBackground(transparent);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(transparent);
		contentPane.setBorder(null);
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setLocationRelativeTo(null);

		ClosePanel closePanel = new ClosePanel(this);
		closePanel.setLocation(getClosePanelPosition());
		contentPane.add(closePanel);

		setIrtPanel(contentPane);

		JComboBox<String> serialPortSelection = new JComboBox<>();
		serialPortSelection.setName("Unit's Serial Port");
		serialPortSelection.addPopupMenuListener(Listeners.popupMenuListener);
		serialPortSelection.setBounds(comboBoxBounds());
		getContentPane().add(serialPortSelection);
		
		JPopupMenu popupMenu = Optional
								.ofNullable(serialPortSelection.getComponentPopupMenu())
								.orElseGet(
										()->{
											final JPopupMenu jPopupMenu = new JPopupMenu();
											serialPortSelection.setComponentPopupMenu(jPopupMenu);
											return jPopupMenu;
										});
//		IrtGui.addPopup(serialPortSelection, popupMenu);
		
		JMenuItem mntmBaudrate = new JMenuItem("Baudrate");
		mntmBaudrate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					new SerialPortWizard().setVisible(true);
				}catch(Exception ex){
					logger.catching(ex);
				}
			}
		});
		popupMenu.add(mntmBaudrate);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent arg0) {

				guiController = getNewGuiController();
			}
		});

		timer = new Timer((int) TimeUnit.SECONDS.toMillis(10), e->{

			iconBackground = null;
			SwingUtilities.invokeLater(()->Optional.ofNullable(IrtPanel.logoIcon).map(logo->logo.getImage()).ifPresent(IrtMainFrame.this::setIconImage));
		});
		timer.setRepeats(false);
		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
	}

	protected void setIrtPanel(JPanel contentPane) {
		IrtPanel irtPanel = new IrtPanel(this);
		irtPanel.setVisible(true);
		contentPane.add(irtPanel);
	}


	protected abstract Point getClosePanelPosition();
	protected abstract Rectangle comboBoxBounds();
	protected abstract GuiControllerAbstract getNewGuiController();

	private Color iconBackground;
	@Override
	public void onPacketReceived(Packet packet) {

		final Optional<Short> oPacket = Optional
		.ofNullable(packet)
		.map(Packet::getHeader)
		.map(PacketHeader::getPacketId)
		.filter(PacketID.ALARMS_SUMMARY::match);

		if(!oPacket.isPresent())
			return;

		new ThreadWorker(
				()->
				oPacket
				.map(id->PacketID.ALARMS_SUMMARY)
				.flatMap(pId->pId.valueOf(packet))
				.filter(AlarmSeverities.class::isInstance)
				.map(AlarmSeverities.class::cast)
				.map(AlarmSeverities::getBackground)
				.ifPresent(
						bg->		
						Optional
						.ofNullable(IrtPanel.logoIcon)
						.map(ImageIcon::getImage)
						.ifPresent(image->{

							if(Optional.ofNullable(iconBackground).filter(iBg->iBg.equals(bg)).isPresent()){
								timer.restart();
								return;
							}

							iconBackground = bg;
							final int width = image.getWidth(null);
							final int height = image.getHeight(null);
							if(width<=0 || height<=0)
								return;

							final Image createdImage = createImage(width, height);
							final Graphics2D g = (Graphics2D) createdImage.getGraphics();
							g.setColor(bg);
							g.fillRect(0, 0, width, height);
							g.drawImage(image, 0, 0, null);
							SwingUtilities.invokeLater(()->{
								setIconImage(createdImage);
							});
						})), "IrtMainFrame.onPacketReceived()");
	}

	public Optional<ModuleSelectFxPanel> getModuleSelectFxPanel() {
		return Optional.ofNullable(moduleSelectFxPanel);
	}

	public void setModuleSelectFxPanel(ModuleSelectFxPanel moduleSelectFxPanel) {

		final Container contentPane = getContentPane();
		if(moduleSelectFxPanel==null){
			getModuleSelectFxPanel().ifPresent(contentPane::remove);
			IrtMainFrame.moduleSelectFxPanel = null;
			return;
		}

		IrtMainFrame.moduleSelectFxPanel = moduleSelectFxPanel;

		contentPane.add(moduleSelectFxPanel);
	}
}
