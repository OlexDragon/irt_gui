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

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.data.Listeners;
import irt.data.listener.PacketListener;
import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.Packets;
import irt.data.packet.alarm.AlarmStatusPacket.AlarmSeverities;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.fx.module.ModuleSelectFxPanel;
import irt.tools.panel.head.ClosePanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.wizards.serial_port.SerialPortWizard;

@SuppressWarnings("serial")
public abstract class IrtMainFrame extends JFrame implements PacketListener {
	static{
		System.setProperty("serialNumber", "UnknownSerialNumber");
	}

	private final static Logger logger = LogManager.getLogger();

	protected GuiControllerAbstract guiController;

	private Object alarmSeverities;

	private Timer timer;

	private ModuleSelectFxPanel moduleSelectFxPanel;

	public IrtMainFrame(int width, int hight) {
		super(IrtPanel.PROPERTIES.getProperty("company_name"));


		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run(){
//		    	logger.error("addShutdownHook");
		    	guiController.stop();
		    	LogManager.shutdown();
		    }
		});

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
			new SwingWorker<Void, Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					Optional.ofNullable(IrtPanel.logoIcon).map(logo->logo.getImage()).ifPresent(IrtMainFrame.this::setIconImage);
					return null;
				}
			}.execute();
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

	@Override
	public void onPacketRecived(Packet packet) {

		Optional
		.ofNullable(packet)
		.filter(p->p.getHeader().getGroupId()==PacketImp.GROUP_ID_ALARM)
		.filter(p->p.getHeader().getPacketId()==PacketWork.PACKET_ID_ALARMS_SUMMARY)
		.flatMap(Packets::cast)
		.map(PacketAbstract::getValue)
		.filter(AlarmSeverities.class::isInstance)
		.map(AlarmSeverities.class::cast)
		.ifPresent(as->{

			timer.restart();

			if(alarmSeverities!=null && alarmSeverities == as)
				return;

			alarmSeverities = as;

			final ImageIcon logoIcon = IrtPanel.logoIcon;
			if(logoIcon!=null) {
				Color background = as.getBackground();
				final Image image = logoIcon.getImage();
				final int iconWidth = logoIcon.getIconWidth();
				final int iconHeight = logoIcon.getIconHeight();
				if(iconWidth<=0 || iconHeight<=0)
					return;
				final Image createdImage = createImage(iconWidth, iconHeight);
				final Graphics2D g = (Graphics2D) createdImage.getGraphics();
				g.setColor(background);
				g.fillRect(0, 0, iconWidth, iconHeight);
				g.drawImage(image, 0, 0, null);
				setIconImage(createdImage);
			}
		});
	}

	public Optional<ModuleSelectFxPanel> getModuleSelectFxPanel() {
		return Optional.ofNullable(moduleSelectFxPanel);
	}

	public void setModuleSelectFxPanel(ModuleSelectFxPanel moduleSelectFxPanel) {

		final Container contentPane = getContentPane();
		if(moduleSelectFxPanel==null){
			getModuleSelectFxPanel().ifPresent(panel->{
				contentPane.remove(panel);
				panel.stop();
			});
			this.moduleSelectFxPanel = null;
			return;
		}

		this.moduleSelectFxPanel = moduleSelectFxPanel;

		contentPane.add(moduleSelectFxPanel);
	}
}
